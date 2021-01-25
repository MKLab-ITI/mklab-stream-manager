package gr.iti.mklab.sfc.streams.monitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gr.iti.mklab.framework.common.domain.feeds.Feed;
import gr.iti.mklab.sfc.streams.Stream;


/**
 * Thread-safe class for monitoring the streams that correspond to each social network
 * Currently 7 social networks are supported (Twitter, Youtube, Flickr, Instagram, Tumblr, Facebook, GooglePlus)
 * 
 * @author Manos Schinas - manosetro@iti.gr
 */
public class StreamsMonitor implements Runnable {
	
	public final Logger logger = LogManager.getLogger(StreamsMonitor.class);

	private ExecutorService executor;
	
	private Map<String, Stream> streams = new HashMap<String, Stream>();
	
	private Map<String, StreamFetchTask> streamsFetchTasks = new HashMap<String, StreamFetchTask>();
	
	boolean isFinished = false;
	
	public StreamsMonitor(int numberOfStreams) {
		logger.info("Initialize Execution Service with " + numberOfStreams + " threads.");
		executor = Executors.newFixedThreadPool(numberOfStreams + 1);
	}
	
	public int getNumberOfStreamFetchTasks() {
		return streamsFetchTasks.size();
	}
	
	public Map<String, StreamFetchTask> getStreamFetchTasks() {
		return streamsFetchTasks;
	}
	
	public void addStreams(List<Stream> streams) {
		for(Stream stream : streams) {
			addStream(stream);
		}
	}

	public void addStream(Stream stream) {
		
		String streamId = stream.getName(); 
		
		logger.info("Add " + streamId + " stream to monitor");
		this.streams.put(streamId, stream);
		
		try {
			logger.info("Add fetch task for " + streamId);
			StreamFetchTask streamTask = new StreamFetchTask(stream);
			
			streamsFetchTasks.put(streamId, streamTask);
		} catch (Exception e) {
			logger.error(e);
		}
		
	}
	
	public Stream getStream(String streamId) {
		return streams.get(streamId);
	}
	
	public void addFeed(String streamId, Feed feed) {
		StreamFetchTask fetchTask = streamsFetchTasks.get(streamId);
		if(fetchTask != null) {	
			fetchTask.addFeed(feed);
		}
		else {
			logger.warn("Cannot add feed to " + streamId + ". There is no initialized fetch task.");
		}
	}
	
	public boolean feedExists(String streamId, Feed feed) {
		StreamFetchTask fetchTask = streamsFetchTasks.get(streamId);
		if(fetchTask == null) {	
			return false;
		}
		else {
			return fetchTask.feedExists(feed);
		}
	}
	
	public void removeFeed(String streamId, Feed feed) {
		StreamFetchTask fetchTask = streamsFetchTasks.get(streamId);
		if(fetchTask != null) {
			fetchTask.removeFeed(feed);
		}
		else {
			logger.warn("Cannot remove feed from " + streamId + ". There is no initialized fetch task.");
		}
	}
	
	public void addFeeds(String streamId, List<Feed> feeds) {
		StreamFetchTask fetchTask = streamsFetchTasks.get(streamId);
		if(fetchTask != null) {
			fetchTask.addFeeds(feeds);
		}
	}
	
	public void addFeeds(List<Feed> feeds) {
		for(StreamFetchTask fetchTask : streamsFetchTasks.values()) {
			fetchTask.addFeeds(feeds);
		}
	}
	
	public List<String> getAllFeeds() {
		Set<String> fIds = new HashSet<String>();
		
		for(StreamFetchTask fetchTask : streamsFetchTasks.values()) {
			fIds.addAll(fetchTask.getFeeds());
		}
		
		return new ArrayList<String>(fIds);
	}

	public void start() {
		executor.submit(this);
	}
	
	/**
	 * Stops the monitor - waits for all streams to shutdown
	 */
	public void stop() {
		isFinished = true;
		executor.shutdown();
        while (!executor.isTerminated()) {
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error(e);
			}
        	logger.info("Waiting for StreamsMonitor to shutdown");
        }
        logger.info("Streams Monitor stopped");
	}

	@Override
	public void run() {

		logger.info("NumberOfStreamFetchTasks: " + this.getNumberOfStreamFetchTasks());

		for(String streamId : streamsFetchTasks.keySet()) {
			StreamFetchTask task = streamsFetchTasks.get(streamId);
			logger.info("Submit fetch task for " + streamId + " for execution.");
			executor.execute(task);
		}
		
		while(!isFinished) {
			// print statistics every 10 minutes
			for(String streamId : streamsFetchTasks.keySet()) {
				StreamFetchTask task = streamsFetchTasks.get(streamId);
				if(task != null) {
					logger.info("Fetch task for " + streamId + " has fetched " + task.getTotalRetrievedItems() + " items. Last execution time: " + task.getLastExecutionTime() + " for feed [" + task.getLastExecutionFeed() + "]");
				}
			}
			
			try {
				Thread.sleep(600000);
			} catch (InterruptedException e) {
				logger.error(e);
				return;
			}
		}
	}
}