package gr.iti.mklab.framework.retrievers.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.Joiner;

import gr.iti.mklab.framework.Credentials;
import gr.iti.mklab.framework.abstractions.socialmedia.items.YoutubeItem;
import gr.iti.mklab.framework.abstractions.socialmedia.users.YoutubeStreamUser;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.MediaItem;
import gr.iti.mklab.framework.common.domain.StreamUser;
import gr.iti.mklab.framework.common.domain.feeds.AccountFeed;
import gr.iti.mklab.framework.common.domain.feeds.GroupFeed;
import gr.iti.mklab.framework.common.domain.feeds.KeywordsFeed;
import gr.iti.mklab.framework.common.domain.feeds.LocationFeed;
import gr.iti.mklab.framework.retrievers.Response;
import gr.iti.mklab.framework.retrievers.Retriever;

public class YoutubeRetriever extends Retriever {

	private Logger  logger = LogManager.getLogger(YoutubeRetriever.class);

	private String apiKey;
	
	private static YouTube youtubeService;
	
	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	public static final JsonFactory JSON_FACTORY = new JacksonFactory();
	
	private static final long NUMBER_OF_RESULTS_RETURNED = 50;
	
	public YoutubeRetriever(Credentials credentials) {
		super(credentials);
		
		apiKey = credentials.getKey();
		
		// This object is used to make YouTube Data API requests. The last argument is required, but since we don't need anything
        // initialized when the HttpRequest is initialized, we override the interface and provide a no-op function.
		youtubeService = new YouTube.Builder(
				HTTP_TRANSPORT, 
				JSON_FACTORY, 
				new HttpRequestInitializer() {
					public void initialize(HttpRequest request) throws IOException {
				}
        }).setApplicationName("youtube-search-module").build();
		
	}

	@Override
	public Response retrieveKeywordsFeed(KeywordsFeed feed, Integer requests) throws Exception {
				
		String label = feed.getLabel();
		long sinceDate = feed.getSinceDate();
		
		List<Item> items = new ArrayList<Item>();
		int numberOfRequests = 0;
		
		// Define the API request for retrieving search results.
        YouTube.Search.List search = youtubeService.search()
        		.list("id");
        
        search.setKey(apiKey);
        		
        List<String> keywords = feed.getKeywords();
		if(keywords == null || keywords.isEmpty()) {
			logger.error("#Youtube : No keywords feed");
			Response response = getResponse(items, numberOfRequests);
			return response;
		}
		
		String textQuery = StringUtils.join(keywords, " OR ");
		if(textQuery == null || textQuery.equals("")) {
			logger.error("Text Query is empty.");
			Response response = getResponse(items, numberOfRequests);
			return response;
		}
		
		logger.info("Text Query: (" + textQuery + ")" + (label==null ? "" : ("with label=" + label)));
		
        search.setQ(textQuery);
        search.setType("video");
        search.setMaxResults(NUMBER_OF_RESULTS_RETURNED);
        search.setPublishedBefore(new DateTime(System.currentTimeMillis()));
		search.setPublishedAfter(new DateTime(feed.getSinceDate()));

        Set<String> uids = new HashSet<String>();
        boolean sinceDateReached = false;
        String nextPageToken = null;
        while(true) {
        	try {
        		
        		if(nextPageToken != null) {
        			search.setPageToken(nextPageToken);
        		}
        		
        		SearchListResponse searchResponse = search.execute();
        		numberOfRequests++;
        	
        		List<SearchResult> searchResultList = searchResponse.getItems();
        		if (searchResultList != null) {
 
        			List<String> videoIds = new ArrayList<String>();
        			for (SearchResult searchResult : searchResultList) {
        				videoIds.add(searchResult.getId().getVideoId());
        			}
        			Joiner stringJoiner = Joiner.on(',');
        			String videoId = stringJoiner.join(videoIds);
        			
        			YouTube.Videos.List listVideosRequest = youtubeService.videos().list("snippet,statistics,recordingDetails,player");
        			listVideosRequest.setId(videoId);
        			listVideosRequest.setMaxResults(NUMBER_OF_RESULTS_RETURNED);
        			listVideosRequest.setKey(apiKey);
        			
                	VideoListResponse listResponse = listVideosRequest.execute();
                	numberOfRequests++;
                
                	List<Video> videoList = listResponse.getItems();
                	if (videoList != null) {
                		for(Video video : videoList) {
                			uids.add(video.getSnippet().getChannelId());
                			Item item = new YoutubeItem(video);
                			if(item.getPublicationTime() < sinceDate) {
                				sinceDateReached = true;
								continue;
                			}
                			
							if(label != null) {
								item.addLabel(label);
							}
						
                			items.add(item);
                		}
                	}
        		}
        	
        		nextPageToken = searchResponse.getNextPageToken();
        		if(nextPageToken == null) {
        			logger.info("Stop retriever. There is no more pages to fetch for query (" + textQuery + ")");
        			break;
        		}
        				
			} catch (GoogleJsonResponseException e) {
				logger.error("There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage(), e);
				break;
			} catch (IOException e) {
				logger.error("There was an IO error: " + e.getCause() + " : " + e.getMessage(), e);
				break;
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				break;
			}
        
        	if(numberOfRequests >= requests) {
        		logger.info("Stop retriever. Number of requests (" + numberOfRequests + ") has reached for " + textQuery);
				break;
			}
        	
			if(sinceDateReached) {
				logger.info("Stop retriever. Since date " + new Date(sinceDate) + " reached for query " + textQuery);
				break;
			}
			
        }
        
        Map<String, StreamUser> users = getStreamUsers(uids);
        for(Item item : items) {
        	String uid = item.getUserId();
        	StreamUser streamUser = users.get(uid);
        	item.setStreamUser(streamUser);
        }
        
		Response response = getResponse(items, numberOfRequests);
		return response;
	}

	@Override
	public Response retrieveAccountFeed(AccountFeed feed, Integer requests) throws Exception {

		List<Item> items = new ArrayList<Item>();
		int numberOfRequests = 0;
		
		long sinceDate = feed.getSinceDate();
		String label = feed.getLabel();
		
		String uid = feed.getId();
		String uName = feed.getUsername();
		
		StreamUser streamUser = null;
		if(uid != null) {
			streamUser = this.getStreamUser(uid);
		}
		else if (uName != null){
			streamUser = getStreamUserForUsername(uName);
		}
		
		
		numberOfRequests++;
		if(streamUser == null) {
			logger.error("#YouTube : No account feed");
			Response response = getResponse(items, numberOfRequests);
			return response;
		}
		logger.info("#YouTube: Retrieving User Feed " + streamUser.getUserid());
		
		// Define the API request for retrieving search results.
        YouTube.Search.List search = youtubeService.search().list("id");
        search.setKey(apiKey);
        search.setChannelId(streamUser.getUserid());
        //search.setOrder("date");
        search.setType("video");
        search.setPublishedBefore(new DateTime(System.currentTimeMillis()));
		search.setPublishedAfter(new DateTime(feed.getSinceDate()));
        search.setMaxResults(NUMBER_OF_RESULTS_RETURNED);
        
		boolean sinceDateReached = false;
		String nextPageToken = null;
		while(true) {
			try {
				if(nextPageToken != null) {
					search.setPageToken(nextPageToken);
				}
				
				SearchListResponse searchResponse = search.execute();
				numberOfRequests++;
								
				List<SearchResult> searchResultList = searchResponse.getItems();
        		if (searchResultList != null && !searchResultList.isEmpty()) {
        			Set<String> videoIds = new HashSet<String>();
        			for (SearchResult searchResult : searchResultList) {
        				if(searchResult.getId().getVideoId() != null) {
        					videoIds.add(searchResult.getId().getVideoId());
        				}
        			}
        			Joiner stringJoiner = Joiner.on(',');
        			String videoId = stringJoiner.join(videoIds);
        			
        			YouTube.Videos.List listVideosRequest = youtubeService.videos().list("snippet,statistics,recordingDetails,player");
        			listVideosRequest.setId(videoId);
        			listVideosRequest.setKey(apiKey);
        			listVideosRequest.setMaxResults(NUMBER_OF_RESULTS_RETURNED);
                	VideoListResponse listResponse = listVideosRequest.execute();
                	numberOfRequests++;
                	
                	List<Video> videoList = listResponse.getItems();
                	if (videoList != null) {
                		for(Video video : videoList) {
                			Item item = new YoutubeItem(video, streamUser);
                			if(item.getPublicationTime() < sinceDate) {
                				sinceDateReached = true;
								continue;
                			}
                			
							if(label != null) {
								item.addLabel(label);
							}
                			items.add(item);
                		}
                	}
                	
                	nextPageToken = searchResponse.getNextPageToken();
    				if(nextPageToken == null) {
    					logger.info("Stop retriever. There is no more pages to fetch for " + uName);
            			break;
    				}
        		}
        		else {
        			logger.info("Stop retriever. No more results in response.");
        			break;
        		}
			} catch (GoogleJsonResponseException e) {
				logger.error("There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage(), e);
				break;
			} catch (IOException e) {
				logger.error("There was an IO error: " + e.getCause() + " : " + e.getMessage(), e);
				break;
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				break;
			}
		
			if(numberOfRequests >= requests) {
        		logger.info("Stop retriever. Number of requests (" + numberOfRequests + ") has reached for " + uName);
				break;
			}
        	
			if(sinceDateReached) {
				logger.info("Stop retriever. Since date " + new Date(sinceDate) + " reached for " + uName);
				break;
			}
		}
		
		Response response = getResponse(items, numberOfRequests);
		return response;
	}

	@Override
	public Response retrieveLocationFeed(LocationFeed feed, Integer requests) throws Exception {
		List<Item> items = new ArrayList<Item>();
		int numberOfRequests = 0;
		
		Response response = getResponse(items, numberOfRequests);
		return response;
	}

	@Override
	public Response retrieveGroupFeed(GroupFeed feed, Integer maxRequests) {
		return null;
	}

	public StreamUser getStreamUserForUsername(String uName) {
		
		try {
			YouTube.Channels.List channelListResponse = youtubeService.channels()
					.list("id,snippet,statistics");
			channelListResponse.setKey(apiKey);
			channelListResponse.setForUsername(uName);
			 
			ChannelListResponse response = channelListResponse.execute();
			List<Channel> channels = response.getItems();
			if(channels != null) {
				Channel channel = channels.get(0);
				
				YoutubeStreamUser user = new YoutubeStreamUser(channel, uName);
				user.setUsername(uName);
				
				return user;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public StreamUser getStreamUser(String uid) {
		try {
			YouTube.Channels.List channelListResponse = youtubeService.channels()
					.list("id,snippet,statistics");
			
			channelListResponse.setKey(apiKey);
			channelListResponse.setId(uid);
			channelListResponse.setMaxResults(NUMBER_OF_RESULTS_RETURNED);
			
			ChannelListResponse response = channelListResponse.execute();
			List<Channel> channels = response.getItems();
			if(channels != null) {
				Channel channel = channels.get(0);
				YoutubeStreamUser user = new YoutubeStreamUser(channel);

				return user;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Map<String, StreamUser> getStreamUsers(Set<String> uids) {
		List<String> list = new ArrayList<String>(uids);
		Map<String, StreamUser> users = new HashMap<String, StreamUser>();
		try {
			int fromIndex = 0;
			while(fromIndex <= uids.size()) {
				int toIndex = Math.min(fromIndex+50, uids.size());
				List<String> sublist = list.subList(fromIndex, toIndex);
			
				Joiner stringJoiner = Joiner.on(',');
				String userIds = stringJoiner.join(sublist);
			
				YouTube.Channels.List channelListResponse = youtubeService.channels().list("id,snippet,statistics");
				channelListResponse.setKey(apiKey);
				channelListResponse.setId(userIds);
				channelListResponse.setMaxResults(NUMBER_OF_RESULTS_RETURNED);

				ChannelListResponse response = channelListResponse.execute();
				List<Channel> channels = response.getItems();
				if(channels != null) {
					for(Channel channel : channels) {
						YoutubeStreamUser user = new YoutubeStreamUser(channel);
						users.put(user.getId(), user);
					}
				}
				
				fromIndex += 50;
			}
			
		} catch (IOException e) {
			logger.error(e);
		}

		return users;
	}
	
	@Override
	public MediaItem getMediaItem(String id) {
		return null;
	}

	@Override
	public Item getItem(String id) {
		YouTube.Videos.List listVideosRequest;
		try {
			listVideosRequest = youtubeService.videos().list("snippet,statistics,recordingDetails,player");
			
			listVideosRequest.setId(id);
			listVideosRequest.setMaxResults(NUMBER_OF_RESULTS_RETURNED);
			listVideosRequest.setKey(apiKey);
			
	    	VideoListResponse listResponse = listVideosRequest.execute();
	    	
	    	List<Video> videos = listResponse.getItems();
	    	for(Video v : videos) {
	    		if(v.getId().equals(id)) {
	    			Item item = new YoutubeItem(v);
	    			return item;
	    		}
	    		
	    	}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public List<Item> getItemComments(Item item, long since) {

		return null;
	}
	
	public static void main(String...args) throws Exception {
		
		Credentials credentials = new Credentials();
		credentials.setKey("");
		
		YoutubeRetriever retriever = new YoutubeRetriever(credentials);
		
		long since = System.currentTimeMillis()-(24*3600000l);
		//KeywordsFeed feed = new KeywordsFeed("id", "food waste", since, "Youtube");
		AccountFeed feed = new AccountFeed("UCWAsWgFVqxDzwExfOdYjAcg", "wtaetv", since, "Youtube");
		
		Response response = retriever.retrieve(feed, 6);
		System.out.println(response.getNumberOfItems());
	
		for(Item item : response.getItems()) {
			System.out.println(item.getTitle());
		}
		
	}
	
}
