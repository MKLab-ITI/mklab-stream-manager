package gr.iti.mklab.framework.retrievers.impl;

import java.util.List;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import gr.iti.mklab.framework.Credentials;
import gr.iti.mklab.framework.abstractions.socialmedia.mediaitems.VimeoMediaItem;
import gr.iti.mklab.framework.abstractions.socialmedia.mediaitems.VimeoMediaItem.VimeoVideo;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.MediaItem;
import gr.iti.mklab.framework.common.domain.StreamUser;
import gr.iti.mklab.framework.common.domain.feeds.AccountFeed;
import gr.iti.mklab.framework.common.domain.feeds.Feed;
import gr.iti.mklab.framework.common.domain.feeds.GroupFeed;
import gr.iti.mklab.framework.common.domain.feeds.KeywordsFeed;
import gr.iti.mklab.framework.common.domain.feeds.LocationFeed;
import gr.iti.mklab.framework.retrievers.Response;
import gr.iti.mklab.framework.retrievers.Retriever;

/**
 * The retriever that implements the Vimeo simplified retriever
 * 
 * @author manosetro - manosetro@iti.gr
 */
public class VimeoRetriever extends Retriever {

	static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	static final JsonFactory JSON_FACTORY = new JacksonFactory();
	
	private HttpRequestFactory requestFactory;
	private String requestPrefix = "http://vimeo.com/api/v2/video/";
	
	public VimeoRetriever(Credentials credentials) {
		
		super(credentials);
		
		requestFactory = HTTP_TRANSPORT.createRequestFactory(
				new HttpRequestInitializer() {
					@Override
					public void initialize(HttpRequest request) {
						request.setParser(new JsonObjectParser(JSON_FACTORY));
					}
				});
	}
	
	public MediaItem getMediaItem(String id) {
	
		GenericUrl url = new GenericUrl(requestPrefix + id + ".json");
		
		HttpRequest request;
		try {
			request = requestFactory.buildGetRequest(url);
			HttpResponse response = request.execute();
			VimeoVideo[] videos = response.parseAs(VimeoVideo[].class);
			if(videos != null && videos.length>0) {
				MediaItem mediaItem = new VimeoMediaItem(videos[0]);
				return mediaItem;
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
		 
		
	}

	@Override
	public Response retrieve(Feed feed) {
		return new Response();
	}

	@Override
	public Response retrieveKeywordsFeed(KeywordsFeed feed) throws Exception {
		return new Response();
	}
	
	@Override
	public Response retrieveAccountFeed(AccountFeed feed) throws Exception {
		return new Response();
	}

	@Override
	public Response retrieveLocationFeed(LocationFeed feed) throws Exception {
		return new Response();
	}

	@Override
	public StreamUser getStreamUser(String uid) {

		return null;
	}

	@Override
	public Response retrieveGroupFeed(GroupFeed feed) {
		return new Response();
	}

	@Override
	public Response retrieve(Feed feed, Integer requests) {
		return null;
	}

	@Override
	public Response retrieveKeywordsFeed(KeywordsFeed feed, Integer requests) throws Exception {
		return null;
	}

	@Override
	public Response retrieveAccountFeed(AccountFeed feed, Integer requests) throws Exception {
		return null;
	}

	@Override
	public Response retrieveLocationFeed(LocationFeed feed, Integer requests) throws Exception {
		return null;
	}

	@Override
	public Response retrieveGroupFeed(GroupFeed feed, Integer requests) {
		return null;
	}

	@Override
	public Item getItem(String id) {
		return null;
	}

	@Override
	public List<Item> getItemComments(Item item, long since) {

		return null;
	}
}
