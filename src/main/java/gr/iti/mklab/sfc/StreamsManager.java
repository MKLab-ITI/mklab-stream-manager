package gr.iti.mklab.sfc;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import gr.iti.mklab.framework.common.domain.collections.Collection;
import gr.iti.mklab.framework.common.domain.config.Configuration;
import gr.iti.mklab.framework.common.domain.feeds.Feed;
import gr.iti.mklab.sfc.input.CollectionsManager;
import gr.iti.mklab.sfc.management.StorageHandler;
import gr.iti.mklab.sfc.streams.Stream;
import gr.iti.mklab.sfc.streams.StreamException;
import gr.iti.mklab.sfc.streams.StreamsManagerConfiguration;
import gr.iti.mklab.sfc.streams.monitors.ItemsMonitor;
import gr.iti.mklab.sfc.streams.monitors.StreamsMonitor;
import gr.iti.mklab.sfc.subscribers.Subscriber;

/**
 * Class for retrieving content according to  keywords - user - location feeds from social networks.
 * Currently 7 social networks are supported (Twitter,Youtube,Facebook,Flickr,Instagram,Tumblr,GooglePlus)
 * 
 * @author Manos Schinas - manosetro@iti.gr
 * 
 */
public class StreamsManager implements Runnable {
	
	public final Logger logger = LogManager.getLogger(StreamsManager.class);
	
	enum ManagerState {
		OPEN, CLOSE
	}

	private String redisHost = null;

	private Map<String, Stream> streams = null;
	private Map<String, Subscriber> subscribers = null;
	
	private StreamsManagerConfiguration config = null;
	private StorageHandler storageHandler;
	
	private StreamsMonitor monitor = null;
	
	private ManagerState state = ManagerState.CLOSE;

	private BlockingQueue<Pair<Collection, String>> collectionsQueue = new LinkedBlockingQueue<Pair<Collection, String>>();
	private BlockingQueue<Pair<Pair<String, String>, String>> itemsQueue = new LinkedBlockingQueue<Pair<Pair<String, String>, String>>();
	
	private CollectionsManager collectionsManager;
	
	private Map<Feed, Set<String>> feeds = new HashMap<Feed, Set<String>>();
	private Map<String, Collection> collectionsUnderMonitoring = new HashMap<String, Collection>();
	
	private RedisSubscriber jedisPubSub;

	private ItemsMonitor itemsMonitor = new ItemsMonitor(itemsQueue);

	private Thread thisThread = null;
	
	public StreamsManager(StreamsManagerConfiguration config) throws StreamException {

		if (config == null) {
			logger.error("Config file in null.");
			throw new StreamException("Manager's configuration must be specified");
		}
		
		//Set the configuration files
		this.config = config;
		
		//Set up the Subscribers
		initSubscribers();
		
		//Set up the Streams
		initStreams();
	}
	
	/**
	 * Opens Manager by starting the auxiliary modules and setting up
	 * the database for reading/storing
	 * 
	 * @throws StreamException Stream Exception
	 */
	public synchronized void open() throws StreamException {
		
		if (state == ManagerState.OPEN) {
			logger.error("Stream manager is already open.");
			return;
		}
		
		state = ManagerState.OPEN;
		logger.info("StreamsManager is open.");
		try {
			Configuration inputConfig = config.getInputConfig();
			
			redisHost = inputConfig.getParameter("redis.host", "127.0.0.1");
			
			//Start stream handler 
			storageHandler = new StorageHandler(config);
			storageHandler.start();	
			logger.info("Storage Manager is ready to store.");
			
			collectionsManager = new CollectionsManager(inputConfig);
			
			//Start the Subscribers
			if(subscribers != null) {
				Map<String, Set<Feed>> feedsPerSource =  collectionsManager.createFeedsPerSource();
				for(String subscriberId : subscribers.keySet()) {
					logger.info("Stream Manager - Start Subscriber : " + subscriberId);
					Configuration srconfig = config.getSubscriberConfig(subscriberId);
					Subscriber subscriber = subscribers.get(subscriberId);
				
					subscriber.setHandler(storageHandler);
					subscriber.open(srconfig);
				
					Set<Feed> sourceFeed = feedsPerSource.get(subscriberId);
					subscriber.subscribe(sourceFeed);
				}
			}
			
			//Start the Streams
			//If there are Streams to monitor start the StreamsMonitor
			if(streams != null && !streams.isEmpty()) {
				monitor = new StreamsMonitor(streams.size());
				for (String streamId : streams.keySet()) {
					logger.info("Start Stream : " + streamId);
					
					Configuration sconfig = config.getStreamConfig(streamId);
					Stream stream = streams.get(streamId);
					stream.setHandler(storageHandler);
					stream.open(sconfig);
				
					monitor.addStream(stream);
				}
				monitor.start();
			}
			else {
				logger.error("There are no streams to open.");
			}
			
			if(monitor != null) {
				itemsMonitor.addFetchTasks(monitor.getStreamFetchTasks());
				itemsMonitor.start();
			}
			
			jedisPubSub = new RedisSubscriber(collectionsQueue, itemsQueue, redisHost);
			jedisPubSub.start();
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new StreamException("Error during streams open", e);
		}
	}
	
	/**
	 * Closes Manager and its auxiliary modules
	 * 
	 * @throws StreamException Stream Exception
	 */
	public synchronized void close() throws StreamException {
		
		if (state == ManagerState.CLOSE) {
			logger.info("StreamManager is already closed.");
			return;
		}
		
		try {
			for (Stream stream : streams.values()) {
				logger.info("Close " + stream);
				stream.close();
			}
			
			itemsMonitor.stop();
			
			if (storageHandler != null) {
				storageHandler.stop();
			}
			
			jedisPubSub.close();
			
			state = ManagerState.CLOSE;
		}
		catch(Exception e) {
			throw new StreamException("Error during streams close", e);
		}
	}
	
	/**
	 * Initializes the streams apis that are going to be searched for relevant content
	 * 
	 * @throws StreamException Stream Exception
	 */
	private void initStreams() throws StreamException {
		streams = new HashMap<String, Stream>();
		try {
			for (String streamId : config.getStreamIds()) {
				Configuration sconfig = config.getStreamConfig(streamId);
				Stream stream = (Stream)Class.forName(sconfig.getParameter(Configuration.CLASS_PATH)).newInstance();
				logger.info("Init " + streamId + ". Max Requests: " +stream.getMaxRequests() + " per " + stream.getTimeWindow());
				streams.put(streamId, stream);
			}
		}catch(Exception e) {
			e.printStackTrace();
			throw new StreamException("Error during streams initialization", e);
		}
	}
	
	/**
	 * Initializes the streams apis, that implement subscriber channels, that are going to be searched for relevant content
	 * 
	 * @throws StreamException Stream Exception
	 */
	private void initSubscribers() throws StreamException {
		subscribers = new HashMap<String, Subscriber>();
		try {
			for (String subscriberId : config.getSubscriberIds()) {
				Configuration sconfig = config.getSubscriberConfig(subscriberId);
				Subscriber subscriber = (Subscriber) Class.forName(sconfig.getParameter(Configuration.CLASS_PATH)).newInstance();
				subscribers.put(subscriberId, subscriber);
			}
		} 
		catch(Exception e) {
			e.printStackTrace();
			throw new StreamException("Error during Subscribers initialization", e);
		}
	}
	
	@Override
	public void run() {

		if(state != ManagerState.OPEN) {
			logger.error("Streams Manager is not open!");
			return;
		}
		
		Map<String, Collection> collections = collectionsManager.getActiveCollections();
		logger.info(collections.size() + " active collections in db.");
		for(Collection collection : collections.values()) {
			try {
				if(!collectionsUnderMonitoring.containsKey(collection.getId())) {
					collectionsQueue.put(Pair.of(collection, "collections:new"));
				}
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
		
		logger.info("Start to monitor for updates on collections.");
		while(state == ManagerState.OPEN) {
			try {
				
				Pair<Collection, String> actionPair = collectionsQueue.take();
				if(actionPair == null) {
					logger.error("Received action pair is null.");
					continue;
				}
				
				Collection collection = actionPair.getKey();
				String cId = collection.getId();
				String action = actionPair.getRight();
				logger.info("Action: " + action + " - collection: " + collection.getId() + " from user " + collection.getOwnertId());
				
				if(monitor == null) {
					logger.error("Monitor has not been initialized. Cannot monitor any feed.");
				}
				
				List<Feed> feedsToInsert;
				List<Feed> feedsToDelete;
				switch (action) {
    				case "collections:new":
    				
    					if(collectionsUnderMonitoring.containsKey(cId)) {
    						logger.error("Collection " + cId + " is already under monitoring!");
    						continue;
    					}
    					
    					collectionsUnderMonitoring.put(cId, collection);
    					
    					feedsToInsert = collection.getFeeds();
    					logger.info(feedsToInsert.size() + " feeds to insert from collection " + cId);
    					
    					insertFeeds(collection, feedsToInsert);
    					
    					continue;
    					
    				case "collections:stop":
    				case "collections:delete":
    					
    					if(collectionsUnderMonitoring.containsKey(cId)) {
    						collectionsUnderMonitoring.remove(cId);	
    					}
    					else {
    						logger.error("Collection " + cId + " is not under monitoring! Cannot remove it.");
    					}
    					
    					feedsToDelete = collection.getFeeds();
    					logger.info(feedsToDelete.size() + " feeds to stop/delete from collection " + cId);
    					
    					deleteFeeds(collection, feedsToDelete);
    					
    					continue;
    					
    				case "collections:edit":
    					
    					if(!collectionsUnderMonitoring.containsKey(cId)) {
    						logger.error("Collection " + cId + " is not under monitoring. Cannot update.");	
    						collectionsQueue.add(Pair.of(collection, "collections:new"));
    						
    						continue;
    					}
    					
    					Collection previousCollection = collectionsUnderMonitoring.get(cId);
    					List<Feed> previousFeeds = previousCollection.getFeeds();
    					List<Feed> newFeeds = collection.getFeeds();
    					
    					// remove deleted feeds
    					feedsToDelete = new ArrayList<Feed>(previousFeeds);
    					feedsToDelete.removeAll(newFeeds);
    					
    					logger.info(feedsToDelete.size() + " feeds to stop/delete from collection " + cId);
    					deleteFeeds(collection, feedsToDelete);
    					
    					// insert new ones
    					feedsToInsert = new ArrayList<Feed>(newFeeds);
    					feedsToInsert.removeAll(previousFeeds);
    					logger.info(feedsToInsert.size() + " feeds to insert from collection " + cId);
    					
    					insertFeeds(collection, feedsToInsert);
    					
    					continue;
    					
    				default:
    					logger.error("Unrecognized action: " + action);
				}
			} catch (InterruptedException e) {
				logger.error("InterruptedException => " + e.getMessage());
			} catch (Exception e) {
				logger.error("Exception => " + e.getMessage());
			}
		}
		logger.info("Exit from stream manager's run loop.");
	}
	
	// Insert feeds associated with a collection under monitoring
	private void insertFeeds(Collection collection, List<Feed> feedsToInsert) {
		for(Feed feed : feedsToInsert) {
			
			logger.info("Insert: " + feed);
			String streamId = feed.getSource();
			Stream stream = monitor.getStream(streamId);
			
			if(stream == null) {
				logger.error("Stream " + streamId + " has not initialized. Feed " + feed + " cannot be added.");
			}
			else {
				Set<String> collections = feeds.get(feed);
				if(collections != null) {
					collections.add(collection.getId());
					logger.info("Feed " + feed + " is already under monitoring. Increase priority: " + collections.size());
					
					if(!monitor.feedExists(streamId, feed)) {
						logger.info("Feed " + feed + " is missing from monitor!");
						monitor.addFeed(streamId, feed);
					}
				}
				else {
					// Add to monitors
					logger.info("Add " + feed + " to " + streamId);
					collections = new HashSet<String>();
					collections.add(collection.getId());
					
					feeds.put(feed, collections);
					monitor.addFeed(streamId, feed);
				}
			}	
		}
	}
	
	// Delete feeds associated with a collection from monitoring
	private void deleteFeeds(Collection collection, List<Feed> feedsToDelete) {
		for(Feed feed : feedsToDelete) {
			String streamId = feed.getSource();
			Stream stream = monitor.getStream(streamId);
			if(stream == null) {
				logger.error("Stream " + streamId + " has not initialized. Feed " + feed + " cannot be removed!");
			}
			else {
				Set<String> feedCollections = feeds.get(feed);
				if(feedCollections != null) {
					feedCollections.remove(collection.getId());
					if(feedCollections.isEmpty()) {
						// Remove from monitors
						logger.info("Remove " + feed + " from " + streamId);
						feeds.remove(feed);
						monitor.removeFeed(streamId, feed);
					}
					else {
						logger.info("Feed " + feed + " priority decreased to " + feedCollections.size());
					}
				}
				else {
					logger.info("Feed " + feed + " does not exist. Cannot remove it");
					
					if(monitor.feedExists(streamId, feed)) {
						logger.error("Feed " + feed + " exists in monitor but not in feeds list.");
					}
				}
			}
		}
	}
	
	public void start() {
		try {
			thisThread = new Thread(this);
			thisThread.start();
		}
		catch(Exception e) {
			logger.error("Cannot start stream manager thread!");
		}
	}
	
	public boolean isRunning() {
		if(thisThread == null) {
			return false;
		}
		
		if(!thisThread.isAlive()) {
			return false;
		}
		
		return true;
	}
	
	public boolean isOpen() {
		return state == ManagerState.OPEN;
	}
	
	public void recheck() {
		
		ThreadContext.put("id", UUID.randomUUID().toString());
		ThreadContext.put("date", new Date().toString());
		
		Set<String> collectionIds = new HashSet<String>();
		List<String> fIds = new ArrayList<String>();
		for(Entry<Feed, Set<String>> entry : feeds.entrySet()) {
			
			Feed feed = entry.getKey();
			fIds.add(feed.getId());
			Set<String> collections = entry.getValue();
			collectionIds.addAll(collections);

			long until = feed.getUntilDate();
			
			for(String cId : collections) {
				Collection collection = collectionsUnderMonitoring.get(cId);
				if(collection.getLastRunningTime() == null || collection.getLastRunningTime() < until) {
					collection.setLastRunningTime(until);
				}
			}
			logger.info("Feed [" + feed.getId() + "] is under supervision - Collections: " + collections);
		}
		logger.info(fIds.size() + " active feeds: " + fIds + ". Timestamp: " + new Date());
		
		if(monitor != null && monitor.getAllFeeds().size() != fIds.size()) {
			logger.error("Number of feeds under monitoring (" + monitor.getAllFeeds().size() + ") differs from feeds associated with collections ("
					+ fIds.size() + ").");
			
			final List<String> missingFeeds = ListUtils.removeAll(fIds, monitor.getAllFeeds());
			List<Feed> missingFeedsList = new ArrayList<Feed>(feeds.keySet());
			missingFeedsList = ListUtils.predicatedList(missingFeedsList, new Predicate<Feed>() {
				@Override
				public boolean evaluate(Feed feed) {
					return missingFeeds.contains(feed.getId());
				}
			});
			
			logger.error("Missing Feeds: " + missingFeedsList);
		}
		
		if(collectionIds.size() != collectionsUnderMonitoring.size()) {
			logger.error("Number of collections under monitoring (" + collectionsUnderMonitoring.size() + ") differs from collections associated with feeds ("
					+ collectionIds.size() + ").");
		}
		
		// Check stored collection that are missing from monitoring
		Map<String, Collection> storedRunningCollections = collectionsManager.getActiveCollections();			
		Set<String> cIds = new HashSet<String>(storedRunningCollections.keySet());
		cIds.removeAll(collectionsUnderMonitoring.keySet());
		
		logger.info("Monitoring check: " + cIds.size() + " collections are missing (" + cIds + "). Re-insert for monitoring.");
		for(String cId : cIds) {
			try {
				Collection collection = storedRunningCollections.get(cId);
				collectionsQueue.put(Pair.of(collection, "collections:new"));
			} catch (InterruptedException e) {
				logger.error(e);
			}
			
		}
		
		// remove stopped collections that are still running
		cIds.clear();
		cIds.addAll(collectionsUnderMonitoring.keySet());
		cIds.removeAll(storedRunningCollections.keySet());
		for(String cId : cIds) {
			try {
				Collection collection = collectionsUnderMonitoring.get(cId);
				collectionsQueue.put(Pair.of(collection, "collections:delete"));
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
		
		ThreadContext.clearAll();
	}
	
}
