package gr.iti.mklab.sfc.subscribers.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import twitter4j.FilterQuery;
import twitter4j.ResponseList;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;
import gr.iti.mklab.framework.abstractions.socialmedia.items.TwitterItem;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.config.Configuration;
import gr.iti.mklab.framework.common.domain.feeds.Feed;
import gr.iti.mklab.framework.common.domain.feeds.AccountFeed;
import gr.iti.mklab.framework.common.domain.feeds.KeywordsFeed;
import gr.iti.mklab.framework.common.domain.feeds.LocationFeed;
import gr.iti.mklab.sfc.streams.StreamException;
import gr.iti.mklab.sfc.subscribers.Subscriber;

/**
 * Class for retrieving real-time Twitter content by subscribing on Twitter Streaming API. 
 * Twitter content can be based on keywords,twitter users or locations or be 
 * a random sampling (1%) of currently posted statuses. 
 * 
 * @author Manos Schinas - manosetro@iti.gr
 *
 */
public class TwitterSubscriber extends Subscriber {
	
	private Logger  logger = LogManager.getLogger(TwitterSubscriber.class);
	
	private BlockingQueue<Status> queue = new LinkedBlockingQueue<Status>();
	
	public enum AccessLevel {
		
		PUBLIC(400, 5000, 25),
		EXTENDED(400, 75000, 25);
		
		private int filterMaxKeywords;
		private int filterMaxFollows;
		private int filterMaxLocations;
		
		private AccessLevel(int filterMaxKeywords,
						   int filterMaxFollows,
						   int filterMaxLocations) {
			this.filterMaxKeywords = filterMaxKeywords;
			this.filterMaxFollows = filterMaxFollows;
			this.filterMaxLocations = filterMaxLocations;
		}

		public int getFilterMaxKeywords() {
			return filterMaxKeywords;
		}

		public int getFilterMaxFollows() {
			return filterMaxFollows;
		}

		public int getFilterMaxLocations() {
			return filterMaxLocations;
		}

	}
	
	private AccessLevel accessLevel = AccessLevel.PUBLIC;
	private StatusListener listener = null;
	
	private twitter4j.TwitterStream twitterStream  = null;
	private Twitter twitterApi;

	private int numberOfConsumers = 10;
	private List<TwitterStreamConsumer> streamConsumers = new ArrayList<TwitterStreamConsumer>();

	private ExecutorService executorService;
	
	public TwitterSubscriber() {
	
	}
	
	@Override
	public synchronized void subscribe(Set<Feed> feeds) throws StreamException {
		
		if (twitterStream == null) {
			logger.error("Stream is closed");
			throw new StreamException("Stream is closed", null);
		} 
		else {
			List<String> keywordsList = new ArrayList<String>();
			Set<String> users = new HashSet<String>();
			Set<Long> userids = new HashSet<Long>();
			List<double[]> locs = new ArrayList<double[]>();
			
			if(feeds != null) {
				logger.info(feeds.size() + " feeds to track");
				for(Feed feed : feeds) {
					if(KeywordsFeed.class.isInstance(feed)) {
						KeywordsFeed keywordFeed = (KeywordsFeed) feed;
						keywordsList.addAll(keywordFeed.getKeywords());	
					}
					else if(AccountFeed.class.isInstance(feed)) {
						AccountFeed accountFeed = (AccountFeed) feed;	
						users.add(accountFeed.getUsername());
					}
					else if(LocationFeed.class.isInstance(feed)) {
						double[] location = new double[2];
					
						location[0] = ((LocationFeed) feed).getLocation().getLatitude();
						location[1] = ((LocationFeed) feed).getLocation().getLongitude();
						locs.add(location);
					}
				}
			}
			
			userids.addAll(getUserIds(users));
			
			String[] keywords = new String[keywordsList.size()];
			long[] follows = new long[Math.min(userids.size(), accessLevel.filterMaxFollows)];
			double[][] locations = new double[locs.size()][2];
			
			for(int i=0; i<keywordsList.size(); i++) {
				keywords[i] = keywordsList.get(i);
				if(i >= accessLevel.filterMaxKeywords) {
					break;
				}
			}
			
			int index = 0;
			for(Long userId : userids) {
				follows[index++] = userId;
				if(index >= accessLevel.filterMaxFollows) {
					break;
				}
			}
			
			for(int i=0; i<locs.size(); i++) {
				locations[i] = locs.get(i);
				if(i >= accessLevel.filterMaxLocations) {
					break;
				}
			}
			
			if (!ensureFilterLimits(keywords, follows, locations)) {
				logger.error("Filter exceeds Twitter's public access level limits");
				throw new StreamException("Filter exceeds Twitter's public access level limits");
			}

			FilterQuery filterQuery = getFilterQuery(keywords, follows, locations);
			if (filterQuery != null) {			
				logger.info("Start tracking from twitter stream");
				twitterStream.shutdown();
				twitterStream.filter(filterQuery);	
			}
			else {
				logger.info("Start sampling from twitter stream");
				twitterStream.shutdown();
				twitterStream.sample();
			}
		}
		
	}
	
	private Set<Long> getUserIds(Collection<String> followsUsernames) {
		
		Set<Long> ids = new HashSet<Long>();
		
		List<String> usernames = new ArrayList<String>(followsUsernames.size());
		for(String username : followsUsernames) {
			usernames.add(username);
		}
		
		int size = usernames.size();
		int start = 0;
		int end = Math.min(start+100, size);
		
		while(start < size) {
			List<String> sublist = usernames.subList(start, end);
			String[] _usernames = sublist.toArray(new String[sublist.size()]);
			try {
				System.out.println("Request for " + _usernames.length + " users ");
				ResponseList<User> users = twitterApi.lookupUsers(_usernames);
				System.out.println(users.size() + " users ");
				for(User user : users) {
					long id = user.getId();
					ids.add(id);
				}
			} catch (TwitterException e) {
				logger.error("Error while getting user ids from twitter...");
				logger.error("Exception in getUserIds: ", e);
				break;
			}
			
			start = end + 1;
			end = Math.min(start+100, size);
		}
		
		return ids;
	}

	@Override
	public void stop() {
		if (listener != null) {
			if(twitterStream != null) {
				logger.info("Shutdown TwiterStream.");
				twitterStream.shutdown();
			}
			listener = null;
			twitterStream  = null;
		}
		
		for(TwitterStreamConsumer consumer : this.streamConsumers) {
			consumer.stop();
		}
		
		this.executorService.shutdown();
	}
	
	private StatusListener getListener() { 
		return new StatusListener() {
			long items = 0, deletion = 0;
			
			@Override
			public void onStatus(Status status) {
				if(status != null) {
					try {
						queue.add(status);
						if((++items)%5000==0) {
							logger.info(items + " incoming items from twitter. " + deletion + " deletions.");
							logger.info(queue.size() + " statuses in queue");
						}
						
						if(queue.size() > 5000) {
							logger.info("Twitter Queue size > 5000. Clear to prevent heapsize overflow.");
							queue.clear();
						}
					}
					catch(Exception e) {
						logger.error("Exception onStatus: ", e);
					}
				}
			}
			
			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
					try {
						deletion++;
						String id = "Twitter#" + statusDeletionNotice.getStatusId();
						
						if(storageHandler != null) {
							storageHandler.delete(id);
						}
					}
					catch(Exception e) {
						logger.error("Exception onDeletionNotice: ", e);
					}
				
			}
			
			@Override
			public void onTrackLimitationNotice(int numOfLimitedStatuses) {
				synchronized(this) {
					logger.error("Rate limit: " + numOfLimitedStatuses);
				}
			}
			
			@Override
			public void onException(Exception ex) {
				synchronized(this) {
					logger.error("Internal stream error occured: " + ex.getMessage());
				}
			}
			@Override
			public void onScrubGeo(long userid, long id) {
				logger.info("Remove appropriate geolocation information for user " + userid + " up to tweet with id " + id);
			}

			@Override
			public void onStallWarning(StallWarning warn) {	
				if(warn != null) {
					logger.error("Stall Warning " + warn.getMessage() + "(" + warn.getPercentFull() + ")");
				}
			}
		};
	}

	
	private boolean ensureFilterLimits(String[] keywords, long[] follows, double[][] locations) {
		if (keywords != null && keywords.length > accessLevel.getFilterMaxKeywords()) 
			return false;
		if (follows != null && follows.length > accessLevel.getFilterMaxFollows()) 
			return false;
		if (locations != null && (locations.length/2) > accessLevel.getFilterMaxLocations()) 
			return false;
		
		return true;
	}
	
	private FilterQuery getFilterQuery(String[] keywords, long[] follows, double[][] locations) {
		FilterQuery query = new FilterQuery();
		boolean empty = true;
		if (keywords != null && keywords.length > 0) {
			logger.info(keywords.length + " keywords to track.");
			query = query.track(keywords);
			empty = false;
		}
		
		if (follows != null && follows.length > 0) {
			logger.info(follows.length + " users to follow.");
			query = query.follow(follows);
			empty = false;
		}
		
		if (locations != null && locations.length > 0) {
			logger.info(locations.length + " locations to track.");
			query = query.locations(locations);
			empty = false;
		}
		
		if (empty) {
			return null;
		}
		else {
			return query;
		}
	}

	@Override
	public void open(Configuration config) throws StreamException {

		if (twitterStream != null) {
			logger.error("#Twitter : Stream is already opened");
			try {
				throw new StreamException("Stream is already opened", null);
			} catch (StreamException e) {
				e.printStackTrace();
			}
		}
		
		String oAuthConsumerKey 		= 	config.getParameter(KEY);
		String oAuthConsumerSecret 		= 	config.getParameter(SECRET);
		String oAuthAccessToken 		= 	config.getParameter(ACCESS_TOKEN);
		String oAuthAccessTokenSecret 	= 	config.getParameter(ACCESS_TOKEN_SECRET);
		
		String accessLevel = config.getParameter("AccessLevel");
		if(accessLevel != null && accessLevel.equals("public")) {
			this.accessLevel = AccessLevel.PUBLIC;
		}
		
		if (oAuthConsumerKey == null || oAuthConsumerSecret == null ||
				oAuthAccessToken == null || oAuthAccessTokenSecret == null) {
			logger.error("#Twitter : Stream requires authentication");
			throw new StreamException("Stream requires authentication");
		}
		
		logger.info("Twitter Credentials: \n" + 
				"\t\t\toAuthConsumerKey:  " + oAuthConsumerKey  + "\n" +
				"\t\t\toAuthConsumerSecret:  " + oAuthConsumerSecret  + "\n" +
				"\t\t\toAuthAccessToken:  " + oAuthAccessToken + "\n" +
				"\t\t\toAuthAccessTokenSecret:  " + oAuthAccessTokenSecret);
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setJSONStoreEnabled(false)
			.setOAuthConsumerKey(oAuthConsumerKey)
			.setOAuthConsumerSecret(oAuthConsumerSecret)
			.setOAuthAccessToken(oAuthAccessToken)
			.setOAuthAccessTokenSecret(oAuthAccessTokenSecret);
		twitter4j.conf.Configuration conf = cb.build();
		
		this.executorService = Executors.newFixedThreadPool(numberOfConsumers);
		for(int i=0; i<numberOfConsumers; i++) {
			TwitterStreamConsumer consumer = new TwitterStreamConsumer();
			streamConsumers.add(consumer);
			
			executorService.execute(consumer);
		}
		logger.info(numberOfConsumers + " stream consumers submitted for execution");
		
		listener = getListener();
		twitterStream = new TwitterStreamFactory(conf).getInstance();	
		twitterStream.addListener(listener);	
		
		twitterApi = new TwitterFactory(conf).getInstance();
	}

	@Override
	public String getName() {
		return "Twitter";
	}

	private class TwitterStreamConsumer implements Runnable {
		
		private boolean stop = false;
		
		@Override
		public void run() {
			while(!stop) {
				try {
					Status status = queue.take();
					
					if(status == null) {
						try {
							wait(10000);
						} catch (InterruptedException e) {
							logger.error(e);
						}
						continue;
					}

					Item item = new TwitterItem(status);
					if(storageHandler != null) {
						storageHandler.handle(item);
					}
				} catch (Exception e) {
					logger.error("Error during stream consumption.", e);
				}
				
			}
		}
		
		public void stop() {
			stop = true;
			synchronized(this) {
				try {
					this.notify();
				}
				catch(Exception e) {
					logger.error(e);
				}
			}
		}
		
	}

}
