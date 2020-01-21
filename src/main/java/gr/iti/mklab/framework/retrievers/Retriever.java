package gr.iti.mklab.framework.retrievers;

import java.util.List;

import gr.iti.mklab.framework.Credentials;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.MediaItem;
import gr.iti.mklab.framework.common.domain.StreamUser;
import gr.iti.mklab.framework.common.domain.feeds.AccountFeed;
import gr.iti.mklab.framework.common.domain.feeds.Feed;
import gr.iti.mklab.framework.common.domain.feeds.GroupFeed;
import gr.iti.mklab.framework.common.domain.feeds.KeywordsFeed;
import gr.iti.mklab.framework.common.domain.feeds.LocationFeed;

/**
 * The interface for retrieving from social media - Currently the social networks supprorted by the platform are the following:
 * YouTube, Google+,Twitter, Facebook, Flickr, Instagram, Topsy, Tumblr, Vimeo, DailyMotion, Twitpic
 * 
 * @author Manos Schinas - manosetro@iti.gr
 */
public abstract class Retriever {
	
	public Retriever(Credentials credentials) {
		
	}
	
	/**
	 * Retrieves a keywords feed that contains certain keywords
	 * in order to retrieve relevant content
	 * 
	 * @param feed KeywordsFeed
	 * @return Response
	 * @throws Exception exception 
	 */
	public Response retrieveKeywordsFeed(KeywordsFeed feed) throws Exception {
		return retrieveKeywordsFeed(feed, 1);
	}
	
	public abstract Response retrieveKeywordsFeed(KeywordsFeed feed, Integer requests) throws Exception;
	
	/**
	 * Retrieves a user feed that contains the user/users in 
	 * order to retrieve content posted by them
	 * 
	 * @param feed AccountFeed
	 * @return Response
	 * @throws Exception exception
	 */
	public Response retrieveAccountFeed(AccountFeed feed) throws Exception {
		return retrieveAccountFeed(feed, 1);
	}
	
	public abstract Response retrieveAccountFeed(AccountFeed feed, Integer requests) throws Exception;
	
	/**
	 * Retrieves a location feed that contains the coordinates of the location
	 * that the retrieved content must come from.
	 * 
	 * @param feed LocationFeed
	 * @return Response
	 * @throws Exception exception
	 */
	public Response retrieveLocationFeed(LocationFeed feed) throws Exception {
		return retrieveLocationFeed(feed, 1);
	}
	
	public abstract Response retrieveLocationFeed(LocationFeed feed, Integer requests) throws Exception;

	/**
	 * Retrieves a list feed that contains the owner of a list an a slug 
	 * used for the description of the list.
	 * @param feed GroupFeed
	 * @return Response
	 */
	public Response retrieveGroupFeed(GroupFeed feed) {
		return retrieveGroupFeed(feed, 1);
	}
	
	public abstract Response retrieveGroupFeed(GroupFeed feed, Integer maxRequests);
	
	/**
	 * Retrieves the info for a specific user on the basis
	 * of his id in the social network
	 * @param uid the user id
	 * @return StreamUser a StreamUser instance
	 */
	public abstract StreamUser getStreamUser(String uid);
	
	/**
	 * Retrieves the info for a specific media object on the basis of its id in the social network
	 * 
	 * @param id the id of a media item 
	 * @return MediaItem a MediaItem instance
	 */
	public abstract MediaItem getMediaItem(String id);
	
	public abstract Item getItem(String id);
	
	public abstract List<Item> getItemComments(Item item, long since);
	
	public Response retrieve(Feed feed) throws Exception {
		return retrieve(feed, 1);
	}

	public Response retrieve (Feed feed, Integer requests) throws Exception {
		if(AccountFeed.class.isInstance(feed)) {
			AccountFeed userFeed = (AccountFeed) feed;				
			return retrieveAccountFeed(userFeed, requests);
		}
		if(KeywordsFeed.class.isInstance(feed)) {
			KeywordsFeed keyFeed = (KeywordsFeed) feed;				
			return retrieveKeywordsFeed(keyFeed, requests);
		}
		if(LocationFeed.class.isInstance(feed)) {
			LocationFeed locationFeed = (LocationFeed) feed;
			return retrieveLocationFeed(locationFeed, requests);
		}
		if(GroupFeed.class.isInstance(feed)) {
			GroupFeed listFeed = (GroupFeed) feed;
			return retrieveGroupFeed(listFeed, requests);
		}
		
		
		return new Response();
	}
	
	public Response getResponse(List<Item> items, int requests) {
		Response response = new Response();
		response.setItems(items);
		response.setRequests(requests);
		
		long lastTimestamp = 0l;
		for(Item item : items) {
			if(lastTimestamp < item.getPublicationTime()) {
				lastTimestamp = item.getPublicationTime();
			}
		}
		response.setLastTimestamp(lastTimestamp);
		
		return response;
	}
}
