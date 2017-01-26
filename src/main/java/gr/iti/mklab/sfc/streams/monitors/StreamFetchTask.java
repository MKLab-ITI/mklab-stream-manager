package gr.iti.mklab.sfc.streams.monitors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.ItemState;
import gr.iti.mklab.framework.common.domain.feeds.Feed;
import gr.iti.mklab.framework.retrievers.Response;
import gr.iti.mklab.sfc.management.StorageHandler;
import gr.iti.mklab.sfc.streams.Stream;
import gr.iti.mklab.sfc.streams.StreamException;


/**
 * Class for handling a Stream Task that is responsible for retrieving
 * content for the stream it is assigned to.
 * 
 * @author Manos Schinas - manosetro@iti.gr
 * 
 */
public class StreamFetchTask implements  Callable<Integer>, Runnable {
	
	private final Logger logger = LogManager.getLogger(StreamFetchTask.class);
	
	private Random rand = new Random();
	
	private Stream stream;
	
	private Map<String, FeedFetch> feeds = Collections.synchronizedMap(new HashMap<String, FeedFetch>());
	private LinkedBlockingQueue<Feed> feedsQueue = new LinkedBlockingQueue<Feed>();
	
	private Map<String, Pair<Long, Integer>> itemsToMonitor = Collections.synchronizedMap(new HashMap<String, Pair<Long, Integer>>());
	
	private int maxRequests;
	private long timeWindow;
	
	private long executionPeriod = 30 * 60000l; // execute each feed every 30 minutes 
	
	private long totalRetrievedItems = 0L;

	private AtomicInteger requests = new AtomicInteger(0);
	private long lastResetTime = 0l;
	
	private long lastExecutionTime = 0l;
	private String lastExecutionFeed = null;
	
	private boolean running = true;
	
	public StreamFetchTask(Stream stream) throws Exception {
		this.stream = stream;
		
		this.maxRequests = stream.getMaxRequests();
		this.timeWindow = stream.getTimeWindow() * 60000l;	
	}
	
	public void addFeed(Feed feed) {
		FeedFetch feedFetch = new FeedFetch(feed);
		this.feeds.put(feed.getId(), feedFetch);
		this.feedsQueue.offer(feed);
	}
	
	public void addFeeds(List<Feed> feeds) {
		for(Feed feed : feeds) {
			addFeed(feed);
		}
	}
	
	public boolean feedExists(Feed feed) {
		return feeds.containsKey(feed.getId());
	}
	
	public void addItem(String id) {
		if(!itemsToMonitor.containsKey(id)) {
			logger.info("Add item with id: " + id + " for extensive monitoring.");
			
			int p = rand.nextInt(4);
			long timeToBeChecked = System.currentTimeMillis() + (p+1) * executionPeriod;
			
			Pair<Long, Integer> value = Pair.of(timeToBeChecked, 1);
			itemsToMonitor.put(id, value);
		}
		else {
			logger.error("Item with id: " + id + " is already under monitoring.");
			
			Pair<Long, Integer> value = itemsToMonitor.get(id);
			value.setValue(value.getValue() + 1);
			itemsToMonitor.put(id, value);
		}
	}
	
	public void removeItem(String id) {
		Pair<Long, Integer> value = itemsToMonitor.get(id);
		if(value == null) {
			logger.error("Cannot remove item with id: " + id);
		}
		else {
			Integer count = value.getRight();
			if(count > 1) {
				Pair.of(--count, value.getRight());
				itemsToMonitor.put(id, value);
			}
			else {
				itemsToMonitor.remove(id);
			}
		}
	}
	
	public void removeFeed(Feed feed) {
		this.feeds.remove(feed.getId());
		this.feedsQueue.remove(feed);
	}

	public void removeFeeds(List<Feed> feeds) {
		for(Feed feed : feeds) {
			removeFeed(feed);
		}
	}
	
	public long getTotalRetrievedItems() {
		return totalRetrievedItems;
	}

	public void setTotalRetrievedItems(long totalRetrievedItems) {
		this.totalRetrievedItems = totalRetrievedItems;
	}
	
	public Date getLastExecutionTime() {
		return new Date(lastExecutionTime);
	}

	public String getLastExecutionFeed() {
		return lastExecutionFeed;
	}
	
	public List<Feed> getFeedsToPoll() {
		List<FeedFetch> fetchTasks = new ArrayList<FeedFetch>();
		long currentTime = System.currentTimeMillis();
		
		// Check for feeds not executed in the last period
		for(FeedFetch feedFetch : feeds.values()) {
			// each feed can run one time in each period
			if((currentTime - feedFetch.getLastExecutionTime()) > executionPeriod) { 
				fetchTasks.add(feedFetch);
			}
		}
		
		sortFeeds(fetchTasks);
		
		List<Feed> feedsToPoll = new ArrayList<Feed>();
		for(FeedFetch ff : fetchTasks) {
			feedsToPoll.add(ff.getFeed());
		}
		return feedsToPoll;
	}
	
	private void sortFeeds(List<FeedFetch> fetchTasks) {
		// sort by execution time - 
		Collections.sort(fetchTasks, new Comparator<FeedFetch>() {
			@Override
			public int compare(FeedFetch ff1, FeedFetch ff2) {
				long lastExecutionTime1 = executionPeriod * (ff1.getLastExecutionTime() / executionPeriod);
				long lastExecutionTime2 = executionPeriod * (ff2.getLastExecutionTime() / executionPeriod);
				
				if(lastExecutionTime1 == lastExecutionTime2) {
					if(ff1.getItemsPerSecond() == ff2.getItemsPerSecond())
						return 0;
					
					return ff1.getItemsPerSecond() < ff2.getItemsPerSecond() ? 1 : -1;
				}
				
				return lastExecutionTime1 < lastExecutionTime2 ? 1 : -1;
			}	
		});
	}
	
	public Set<String> getFeeds() {
		return feeds.keySet();
	}
	
	/*
	 * Retrieves content using the feeds assigned to the task
	 * making rest calls to stream's API. 
	 */
	@Override
	public Integer call() throws Exception {
		int totalItems = 0;
		try {
			long currentTime = System.currentTimeMillis();
			if((currentTime -  lastResetTime) > executionPeriod) {
				logger.info("Reset available requests for " + stream.getName());
				
				requests.set(0);	// reset performed requests
				lastResetTime = currentTime;
			}

			// get feeds ready for polling
			List<Feed> feedsToPoll = getFeedsToPoll();
			
			if(!feedsToPoll.isEmpty()) {
				int numOfFeeds = feedsToPoll.size();
				int remainingRequests = (maxRequests - requests.get()) / numOfFeeds;	// remaining requests per feed
				if(remainingRequests < 1) {
					logger.info("Remaining Requests: " + remainingRequests + " for " + stream.getName());
					return totalItems;
				}
				
				for(Feed feed : feedsToPoll) {
					logger.info("Poll for " + feed);
					
					long executionTime = System.currentTimeMillis();
					
					Response response = stream.poll(feed, remainingRequests);
					totalItems += response.getNumberOfItems();
					
					lastExecutionTime = System.currentTimeMillis();
					lastExecutionFeed = feed.getId();
					
					// increment performed requests
					requests.addAndGet(response.getRequests());
					
					FeedFetch feedFetch = feeds.get(feed.getId());
					if(feedFetch != null) {
						feedFetch.setLastExecutionTime(executionTime);
						feedFetch.incFetchedItems(response.getNumberOfItems());
						
						feed.setUntilDate(executionTime);
					}
					else {
						logger.error("There is no fetch structure for feed (" + feed.getId() + ")");
					}
					
				}
				return totalItems;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in stream fetch task for " + stream.getName(), e);
		}	
		return totalItems;
	}

	@Override
	public void run() {
		// each feed must consume no more that the 20% of the available requests, even if its the only active feed
		int maxRequestsPerFeed = (int) (0.2 * maxRequests);
		
		while(running) {
			try {
				long currentTime = System.currentTimeMillis();
				if((currentTime -  lastResetTime) > timeWindow) {
					logger.info((maxRequests - requests.get()) + " available requests for " + stream.getName() + ". Reset them to " + maxRequests);
					
					requests.set(0);	// reset performed requests
					lastResetTime = currentTime;
				}

				// get feeds ready for polling
				Feed feed = feedsQueue.take();
				feedsQueue.offer(feed);
				
				List<Feed> feedsToPoll = getFeedsToPoll();
				if(!feedsToPoll.isEmpty()) {
					if(feedsToPoll.contains(feed)) {
						
						int availableRequests = maxRequests - requests.get();
						if(availableRequests <= 1) {
							long waitingTime = (timeWindow - (currentTime -  lastResetTime));
							if(waitingTime > 0) {	
								logger.info("No more remaining requests for " + stream.getName() + ". "
										+ feedsToPoll + " feeds are ready for polling but must wait for " + (waitingTime / 1000) 
										+ " seconds until reset.");
								
								try {
									Thread.sleep(waitingTime);
								}
								catch(InterruptedException e) {
									logger.error("Exception while waiting for reseting in stream fetch task for " + stream.getName(), e);
								}
							}
						}
						else {
							// calculate number of requests per feed. (At least one request)
							int requestsPerFeed = Math.min(maxRequestsPerFeed, Math.max(availableRequests / feedsToPoll.size(), 1));
							logger.info("Poll for [" + feed.getId() + "]. Requests: " + requestsPerFeed);
							
							Response response = stream.poll(feed, requestsPerFeed);
							totalRetrievedItems += response.getNumberOfItems();
						
							lastExecutionTime = System.currentTimeMillis();
							lastExecutionFeed = feed.getId();
							
							// increment performed requests
							requests.addAndGet(response.getRequests());
							
							FeedFetch feedFetch = feeds.get(feed.getId());
							if(feedFetch != null) {
								if(feedFetch.getFirstExecutionTime() == null) {
									logger.info("Feed " + feed.getId() + " executed after " + ((lastExecutionTime - feedFetch.getInitializationTime())/1000) + " seconds");
									feedFetch.setFirstExecutionTime(lastExecutionTime);
								}
								
								feedFetch.incExecutions();
								feedFetch.setLastExecutionTime(lastExecutionTime);
								feedFetch.incFetchedItems(response.getNumberOfItems());
								
								logger.info("Feed " + feed.getId() + " executed " + feedFetch.getExecutions() + " times. " + 
										feedFetch.getFetchedItems() + " items fetched. FetchRate: " + feedFetch.getItemsPerSecond());
							}
							else {
								// cannot happen normally
								logger.error("There is no fetch structure for feed (" + feed.getId() + ")");
							}
						}
					}
				}
				else {
					Thread.sleep(5000);
				}
				
				List<ItemState> itemStates = itemsMonitoring();
				StorageHandler handler = stream.getHandler();
				
				handler.handleItemStates(itemStates);
				
			} catch (Exception e) {
				logger.error("Exception in stream fetch task for " + stream.getName(), e);
			}	
		}
	}
	
	public List<ItemState> itemsMonitoring() {
		
		List<ItemState> itemStates = new ArrayList<ItemState>();
		
		Set<String> ids = new HashSet<String>();
		long currentTime = System.currentTimeMillis();
		for(Entry<String, Pair<Long, Integer>> entry : itemsToMonitor.entrySet()) {
			String id = entry.getKey();
			Pair<Long, Integer> value = entry.getValue();	
			Long timeToBeChecked = value.getKey();
			if(currentTime - timeToBeChecked > 0) {
				// get items new state
				ids.add(id);
			}
		}
		
		if(ids.isEmpty()) {
			return itemStates;
		}
		
		logger.info(ids.size() + " items to monitor. ");
		for(String id : ids) {
			if(requests.get() < maxRequests) {
				Item item;
				try {
					requests.incrementAndGet();
					
					item = stream.poll(id);
					if(item == null) {
						continue;
					}
					
					ItemState itemState = new ItemState(id);
					itemState.setTimestamp(currentTime);
					itemState.setLikes(item.getLikes());
					itemState.setComments(item.getComments());
					itemState.setShares(item.getShares());
					
					itemStates.add(itemState);
					
					int p = rand.nextInt(4);
					long timeToBeChecked = System.currentTimeMillis() + (p+1) * executionPeriod;
					
					Pair<Long, Integer> value = itemsToMonitor.get(id);
					
					itemsToMonitor.put(id, Pair.of(timeToBeChecked, value.getRight()));
					
				} catch (StreamException e) {
					logger.error("Exception during polling of " + id, e);
				}
				
			}
			else {
				break;
			}
		}
		
		return itemStates;
	}
	
}
