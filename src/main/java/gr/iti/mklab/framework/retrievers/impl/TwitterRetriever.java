package gr.iti.mklab.framework.retrievers.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import twitter4j.GeoLocation;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import gr.iti.mklab.framework.Credentials;
import gr.iti.mklab.framework.abstractions.socialmedia.items.TwitterItem;
import gr.iti.mklab.framework.abstractions.socialmedia.users.TwitterStreamUser;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.Location;
import gr.iti.mklab.framework.common.domain.MediaItem;
import gr.iti.mklab.framework.common.domain.StreamUser;
import gr.iti.mklab.framework.common.domain.feeds.AccountFeed;
import gr.iti.mklab.framework.common.domain.feeds.GroupFeed;
import gr.iti.mklab.framework.common.domain.feeds.KeywordsFeed;
import gr.iti.mklab.framework.common.domain.feeds.LocationFeed;
import gr.iti.mklab.framework.retrievers.Response;
import gr.iti.mklab.framework.retrievers.Retriever;

/**
 * Class responsible for retrieving Twitter content based on keywords, twitter users or locations
 * The retrieval process takes place through Twitter API (twitter4j).
 * 
 * @author Manos Schinas - manosetro@iti.gr
 */
public class TwitterRetriever extends Retriever {
	
	private Logger  logger = LogManager.getLogger(TwitterRetriever.class);
	
	private Twitter twitter = null;
	private TwitterFactory tf = null;
	
	public TwitterRetriever(Credentials credentials) {
		super(credentials);
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setJSONStoreEnabled(false)
			.setOAuthConsumerKey(credentials.getKey())
			.setOAuthConsumerSecret(credentials.getSecret())
			.setOAuthAccessToken(credentials.getAccessToken())
			.setOAuthAccessTokenSecret(credentials.getAccessTokenSecret());
		Configuration conf = cb.build();
		
		tf = new TwitterFactory(conf);
		twitter = tf.getInstance();
		
	}
	
	@Override
	public Response retrieveAccountFeed(AccountFeed feed, Integer requests) {
		
		List<Item> items = new ArrayList<Item>();
		
		int count = 200;
		
		Integer numberOfRequests = 0;
		
		Date sinceDate = new Date(feed.getSinceDate());
		String label = feed.getLabel();

		String screenName = feed.getUsername();
		if(feed.getId() == null && screenName == null) {
			logger.error("Uid and Username is null for feed [" + feed + "]");
			Response response = getResponse(items, numberOfRequests);
			return response;
		}
		
		Long uid = Long.parseLong(feed.getId());
		logger.info("Retrieve timeline for user @" + screenName + " (" + uid + ")");

		int page = 1;
		Paging paging = new Paging(page, count);
		boolean sinceDateReached = false;
		while(true) {
			try {
				ResponseList<Status> responseList = null;
				if(uid != null) {
					responseList = twitter.getUserTimeline(uid, paging);
				}
				else {
					responseList = twitter.getUserTimeline(screenName, paging);	
				}
				
				numberOfRequests++;
				for(Status status : responseList) {
					if(status != null) {
						
						if(sinceDate != null) {
							Date createdAt = status.getCreatedAt();
							if(sinceDate.after(createdAt)) {
								sinceDateReached = true;
								break;
							}
						}
						
						Item twitterItem = new TwitterItem(status);
						if(label != null) {
							twitterItem.addLabel(label);
						}
						
						items.add(twitterItem);
					}
				}
				
				if(numberOfRequests >= requests) {	
					logger.info("Stop retriever. Number of requests (" + numberOfRequests + ") has reached for @" + screenName + " (" + uid + ")");
					break;
				}
				if(sinceDateReached) {
					logger.info("Stop retriever. Since date " + sinceDate + " reached for user @" + screenName + " (" + uid + ")");
					break;
				}
				paging.setPage(++page);
				
			} catch (TwitterException e) {
				logger.error(e);
				break;
			}
		}
		
		Response response = getResponse(items, numberOfRequests);
		return response;
	}
	
	@Override
	public Response retrieveKeywordsFeed(KeywordsFeed feed, Integer requests) {
			
		List<Item> items = new ArrayList<Item>();
		
		int count = 100;
		int numberOfRequests = 0;

		Date sinceDate = new Date(feed.getSinceDate());
		String label = feed.getLabel();
		
		List<String> keywords = feed.getKeywords();
		if(keywords == null || keywords.isEmpty()) {
			logger.error("#Twitter : No keywords feed");
			Response response = getResponse(items, numberOfRequests);
			return response;
		}
		
		String textQuery = StringUtils.join(keywords, " OR ");
		if(textQuery.equals("")) {
			logger.error("Text Query is empty.");
			Response response = getResponse(items, numberOfRequests);
			return response;
		}
		
		//Set the query
		logger.info("Text Query: (" + textQuery + ")" + (label==null ? "" : ("with label=" + label)));
		
		Query query = new Query(textQuery);
	
		//query.setUntil("2012-02-01");
		query.count(count);
		query.setResultType(Query.RECENT); //do not set last item date-causes problems!

		boolean sinceDateReached = false;
		try {
			QueryResult queryResult = twitter.search(query);
			
			while(queryResult != null) {
				numberOfRequests++;
				
				List<Status> statuses = queryResult.getTweets();
				if(statuses == null || statuses.isEmpty()) {
					logger.info("No more results for " + query);
					break;
				}
				
				for(Status status : statuses) {
					if(status != null) {
						if(sinceDate != null) {
							Date createdAt = status.getCreatedAt();
							if(sinceDate.after(createdAt)) {
								sinceDateReached = true;
								break;
							}
						}
						
						Item twitterItem = new TwitterItem(status);
						if(label != null) {
							twitterItem.addLabel(label);
						}
						
						items.add(twitterItem);
					}
				}
				
				if(numberOfRequests >= requests) {
					logger.info("Stop retriever. Number of requests (" + numberOfRequests + ") has reached for " + textQuery);
					break;
				}
				
				if(sinceDateReached) {
					logger.info("Stop retriever. Since date reached " + sinceDate + " for " + textQuery);
					break;
				}
				
				query = queryResult.nextQuery();
				if(query == null) {
					break;
				}
				queryResult = twitter.search(query);
			}
			
		} catch (Exception e) {
			logger.error(e);
		}	
	
		Response response = getResponse(items, numberOfRequests);
		return response;
	}
	
	@Override
	public Response retrieveLocationFeed(LocationFeed feed, Integer requests) {
		
		List<Item> items = new ArrayList<Item>();
		
		int count = 100;
		
		Integer numberOfRequests = 0;
		Date sinceDate = new Date(feed.getSinceDate());
		
		String label = feed.getLabel();
		
		Location location = feed.getLocation();
		if(location == null) {
			Response response = getResponse(items, numberOfRequests);
			return response;
		}
		
		//Set the query
		Query query = new Query();
		
		Double radius = location.getRadius();
		if(radius == null || radius == 0) {
			// default radius 1.5Km 
			radius = 1.5; 
		}
		
		GeoLocation geoLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
		query.setGeoCode(geoLocation, radius, Query.KILOMETERS);
		query.count(count);
				
		boolean sinceDateReached = false;
		while(true) {
			try {
				numberOfRequests++;
				QueryResult queryResult = twitter.search(query);
				List<Status> statuses = queryResult.getTweets();
				
				for(Status status : statuses) {
					if(status != null) {
						if(sinceDate != null) {
							Date createdAt = status.getCreatedAt();
							if(sinceDate.after(createdAt)) {
								sinceDateReached = true;
								break;
							}
						}
						
						Item twitterItem = new TwitterItem(status);
						if(label != null) {
							twitterItem.addLabel(label);
						}
						items.add(twitterItem);
					}
				}
				
				if(!queryResult.hasNext()) {
					logger.info("There is not next query for <" + location.getLatitude() + ", " + location.getLongitude() + ">");
					break;
				}
				
				if(numberOfRequests > requests) {
						logger.info("Stop retriever. NumberOfRequests: " + numberOfRequests + " > " + requests + " for <" + location.getLatitude() + ", " + location.getLongitude() + ">");
					break;
				}
				if(sinceDateReached) {
					logger.info("Stop retriever. Since date reached: " + sinceDate + " for <" + location.getLatitude() + ", " + location.getLongitude() + ">");
					break;
				}
				
				query = queryResult.nextQuery();
				if(query == null) {
					break;
				}
				
			} catch (TwitterException e) {
				logger.error(e);
				break;
			}
		}
		
		Response response = getResponse(items, numberOfRequests);
		return response;
	}
	
	@Override
	public Response retrieveGroupFeed(GroupFeed feed, Integer requests) {
	
		List<Item> items = new ArrayList<Item>();
		
		Integer numberOfRequests = 0;

		String label = feed.getLabel();
			
		String ownerScreenName = feed.getGroupCreator();
		String slug = feed.getGroupId();
				
		logger.info("Request for group(" + ownerScreenName + ", " + slug + ")");
		
		int page = 1;
		Paging paging = new Paging(page, 200);
		while(true) {
			try {
				numberOfRequests++;
				ResponseList<Status> responseList = twitter.getUserListStatuses(ownerScreenName, slug, paging);
				for(Status status : responseList) {
					if(status != null) {
						Item twitterItem = new TwitterItem(status);
						if(label != null) {
							twitterItem.addLabel(label);
						}
						items.add(twitterItem);
					}
				}
					
				if(numberOfRequests > requests) {
					logger.info("Stop Retriever. NumberOfRequests: " + numberOfRequests + " > " + requests + " for group(" + ownerScreenName + ", " + slug + ")");
					break;
				}
				
				paging.setPage(++page);
			} catch (TwitterException e) {
				logger.error(e);	
				break;
			}
		}
		
		Response response = getResponse(items, numberOfRequests);
		return response;
	}

	@Override
	public MediaItem getMediaItem(String id) {
		return null;
	}

	@Override
	public Item getItem(String id) {
		try {
			long twId = Long.parseLong(id);
			Status status = twitter.showStatus(twId);
			
			if(status != null) {
				Item item  = new TwitterItem(status);
				
				return item;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	
	@Override
	public StreamUser getStreamUser(String uid) {
		try {
			long userId = Long.parseLong(uid);
			User user = twitter.showUser(userId);
			
			StreamUser streamUser = new TwitterStreamUser(user);
			return streamUser;
		}
		catch(Exception e) {
			logger.error(e);
			return null;
		}
	}


	@Override
	public List<Item> getItemComments(Item item, long since) {

		Query query = new Query();
		
		try {
			twitter.search(query);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void main(String...args) throws Exception {
		
		Credentials credentials = new Credentials ();
		credentials.setKey("");
		credentials.setSecret("");
		credentials.setAccessToken("");
		credentials.setAccessTokenSecret("");
		
		TwitterRetriever retriever = new TwitterRetriever(credentials);
	
		Date since = new Date(System.currentTimeMillis() - 30l*24l*3600000l);
		
		Location location = new Location(35.300117, 24.74926909999994, 50.);
		LocationFeed feed = new LocationFeed("1", location, since.getTime(), "Twitter");

		Response response = retriever.retrieve(feed);
		
		for(Item item : response.getItems()) {
			System.out.println(item.toString());
		}
		
		/*
		List<String> keywords = new ArrayList<String>();
		keywords.add("(bbc AND bias)");
		keywords.add("(bbc AND impartial)");
		keywords.add("(bbc AND partisan)");
		keywords.add("(bbc AND left AND wing)");
		keywords.add("(bbc AND right AND wing)");
		
		KeywordsFeed feed = new KeywordsFeed("1", keywords, since.getTime(), "Twitter");
		
		Response response = retriever.retrieve(feed, 10);
		for(Item item : response.getItems()) {
			System.out.println(item.getTitle().replaceAll("\n", " "));
			System.out.println(new Date(item.getPublicationTime()));
			System.out.println("From: " + item.getStreamUser().getUsername());
			System.out.println("==============================================");
		}
		
		retriever.printAvailableReq();
		*/
	}
}
