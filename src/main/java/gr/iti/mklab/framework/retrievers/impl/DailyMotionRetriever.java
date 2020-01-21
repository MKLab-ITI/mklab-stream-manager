package gr.iti.mklab.framework.retrievers.impl;


import java.util.List;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;

import gr.iti.mklab.framework.Credentials;
import gr.iti.mklab.framework.abstractions.socialmedia.mediaitems.DailyMotionMediaItem;
import gr.iti.mklab.framework.abstractions.socialmedia.mediaitems.DailyMotionMediaItem.DailyMotionVideo;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.MediaItem;
import gr.iti.mklab.framework.common.domain.StreamUser;
import gr.iti.mklab.framework.common.domain.feeds.AccountFeed;
import gr.iti.mklab.framework.common.domain.feeds.GroupFeed;
import gr.iti.mklab.framework.common.domain.feeds.KeywordsFeed;
import gr.iti.mklab.framework.common.domain.feeds.LocationFeed;
import gr.iti.mklab.framework.retrievers.Response;
import gr.iti.mklab.framework.retrievers.Retriever;

/**
 * The retriever that implements the Daily Motion wrapper
 * 
 * @author manosetro - manosetro@iti.gr
 */
public class DailyMotionRetriever extends Retriever {

	static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	static final JsonFactory JSON_FACTORY = new JacksonFactory();

	private HttpRequestFactory requestFactory;
	private String requestPrefix = "https://api.dailymotion.com/video/";
	
	public DailyMotionRetriever(Credentials credentials) {
		super(credentials);
		
		requestFactory = HTTP_TRANSPORT.createRequestFactory(
				new HttpRequestInitializer() {
					@Override
					public void initialize(HttpRequest request) {
						request.setParser(new JsonObjectParser(JSON_FACTORY));
					}
				});
	}
	
	/** 
	 * URL for Dailymotion API. 
	 */
	private static class DailyMotionUrl extends GenericUrl {

		public DailyMotionUrl(String encodedUrl) {
			super(encodedUrl);
		}

		@Key
		public String fields = "id,tags,title,url,embed_url,rating,thumbnail_url," +
				"views_total,created_time,geoloc,ratings_total,comments_total";
	}
	
	/**
	 * Returns the retrieved media item
	 */
	public MediaItem getMediaItem(String id) {
		
		DailyMotionUrl url = new DailyMotionUrl(requestPrefix + id);
		
		HttpRequest request;
		try {
			request = requestFactory.buildGetRequest(url);
			DailyMotionVideo video = request.execute().parseAs(DailyMotionVideo.class);
			
			if(video != null) {
				MediaItem mediaItem = new DailyMotionMediaItem(video);
				return mediaItem;
			}
			
		} catch (Exception e) {
			
		}

		return null;
	}

	@Override
	public Response retrieveKeywordsFeed(KeywordsFeed feed, Integer maxRequests) throws Exception {
		return new Response();
	}

	@Override
	public Response retrieveAccountFeed(AccountFeed feed, Integer maxRequests) throws Exception {
		return new Response();
	}

	@Override
	public Response retrieveLocationFeed(LocationFeed feed, Integer maxRequests) throws Exception {
		return new Response();
	}

	@Override
	public StreamUser getStreamUser(String uid) {
		return null;
	}

	@Override
	public Response retrieveGroupFeed(GroupFeed feed, Integer maxRequests) {
		return new Response();
	}

	@Override
	public Item getItem(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Item> getItemComments(Item item, long since) {
		// TODO Auto-generated method stub
		return null;
	}
}