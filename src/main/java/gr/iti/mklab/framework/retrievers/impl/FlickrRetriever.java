package gr.iti.mklab.framework.retrievers.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.people.PeopleInterface;
import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.photos.Extras;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.SearchParameters;

import gr.iti.mklab.framework.Credentials;
import gr.iti.mklab.framework.abstractions.socialmedia.items.FlickrItem;
import gr.iti.mklab.framework.abstractions.socialmedia.users.FlickrStreamUser;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.Location.Coordinates;
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
 * Class responsible for retrieving Flickr content based on keywords,users or location coordinates
 * The retrieval process takes place through Flickr API.
 * 
 * @author manosetro - manosetro@iti.gr
 */
public class FlickrRetriever extends Retriever {

	private Logger logger = LogManager.getLogger(FlickrRetriever.class);
	
	private static final int RESULTS_PER_PAGE = 500;
	
	private String flickrKey;
	private String flickrSecret;

	private Flickr flickr;	
	private Map<String, StreamUser> userMap = new HashMap<String, StreamUser>();
	
	public FlickrRetriever(Credentials credentials) {
		super(credentials);
		
		this.flickrKey = credentials.getKey();
		this.flickrSecret = credentials.getSecret();
		
		Flickr.debugStream = false;
		
		this.flickr = new Flickr(flickrKey, flickrSecret, new REST());
	}
	
	@Override
	public Response retrieveAccountFeed(AccountFeed feed, Integer maxRequests) {
		
		List<Item> items = new ArrayList<Item>();
		
		Date sinceDate = new Date(feed.getSinceDate());
		String label = feed.getLabel();
		
		int page=1, pages=1; //pagination
		int numberOfRequests = 0;
		
		String userID = feed.getId();
		if(userID == null) {
			logger.info("#Flickr : No source feed " + feed);
			Response response = getResponse(items, numberOfRequests);
			return response;
		}
		
		
		StreamUser streamUser = getStreamUser(userID);
		if(streamUser == null) {
			logger.info("#Flickr: There is no user with id " + userID);
		}
		
		PhotosInterface photosInteface = flickr.getPhotosInterface();
		SearchParameters params = new SearchParameters();
		params.setUserId(userID);
		params.setMinUploadDate(sinceDate);
		
		Set<String> extras = new HashSet<String>(Extras.ALL_EXTRAS);
		extras.remove(Extras.MACHINE_TAGS);
		params.setExtras(extras);
		
		boolean sinceDateReached = false;
		while(true) {
			PhotoList<Photo> photos;
			try {
				numberOfRequests++;
				photos = photosInteface.search(params , RESULTS_PER_PAGE, page++);
			} catch (Exception e) {
				logger.error(e);
				break;
			}
			
			pages = photos.getPages();
			if(photos.isEmpty()) {
				break;
			}
		
			for(Photo photo : photos) {
				if(photo.getDatePosted().before(sinceDate)) {
					sinceDateReached = true;
					break;
				}
				FlickrItem flickrItem = new FlickrItem(photo, streamUser);
				if(label != null) {
					flickrItem.addLabel(label);
				}
				items.add(flickrItem);
			}
			
			if(page > pages) {
				logger.info("Stop retriever. Number of pages (" + pages + ") reached.");
				break;
			}
			
			if(numberOfRequests >= maxRequests) {
				logger.info("Stop retriever. Number of requests (" + maxRequests + ") reached.");
				break;
			}
			
			if(sinceDateReached) {
				logger.info("Stop retriever. Since Date Reached: " + sinceDate);
				break;
			}
		}
		
		Response response = getResponse(items, numberOfRequests);
		return response;
	}
	
	@Override
	public Response retrieveKeywordsFeed(KeywordsFeed feed, Integer maxRequests) {
		
		List<Item> items = new ArrayList<Item>();
		
		Date sinceDate = new Date(feed.getSinceDate());
		String label = feed.getLabel();
		
		int page = 1, pages = 1;
		int numberOfRequests = 0;
		
		List<String> keywords = feed.getKeywords();
		if(keywords == null || keywords.isEmpty()) {
			logger.error("#Flickr : Text is emtpy for (" + feed.getId() + ")");
			Response response = getResponse(items, numberOfRequests);
			return response;
		}
		
		List<String> tags = new ArrayList<String>();
		String text = "";
		for(String key : keywords) {
			String [] words = key.split("\\s+");
			for(String word : words) {
				if(!tags.contains(word) && word.length()>1) {
					tags.add(word);
					text += (word + " ");
				}
			}
		}
		
		if(text.equals("")) {
			logger.error("#Flickr: Text is emtpy for (" + feed.getId() + ")");
			Response response = getResponse(items, numberOfRequests);
			return response;
		}
		
		PhotosInterface photosInteface = flickr.getPhotosInterface();
		SearchParameters params = new SearchParameters();
		params.setText(text);
		params.setMinUploadDate(sinceDate);
		
		Set<String> extras = new HashSet<String>(Extras.ALL_EXTRAS);
		extras.remove(Extras.MACHINE_TAGS);
		params.setExtras(extras);
		
		boolean sinceDateReached = false;
				
		logger.info("Search for (" + text + ")");
		while(true) {
			PhotoList<Photo> photos;
			try {
				numberOfRequests++;
				photos = photosInteface.search(params , RESULTS_PER_PAGE, page++);
			} catch (Exception e) {
				logger.error(e);
				continue;
			}
			
			pages = photos.getPages();
			if(photos.isEmpty()) {
				break;
			}
		
			for(Photo photo : photos) {
				if(photo.getDatePosted().before(sinceDate)) {
					sinceDateReached = true;
					break;
				}
				
				String userid = photo.getOwner().getId();
				StreamUser streamUser = getStreamUser(userid);
				if(streamUser == null) {
					continue;
				}
				
				Item flickrItem = new FlickrItem(photo, streamUser);
				if(label != null) {
					flickrItem.addLabel(label);
				}
				items.add(flickrItem);
			}
			
			if(page > pages) {
				logger.info("Stop retriever. Number of pages (" + pages + ") reached.");
				break;
			}
			
			if(numberOfRequests >= maxRequests) {
				logger.info("Stop retriever. Number of requests (" + maxRequests + ") reached for ( " + feed.getId() + ")");
				break;
			}
			
			if(sinceDateReached) {
				logger.info("Stop retriever. Since date reached " + sinceDate + " for (" + feed.getId() + ")");
				break;
			}
		}

		Response response = getResponse(items, numberOfRequests);
		return response;
	}
	
	@Override
	public Response retrieveLocationFeed(LocationFeed feed, Integer maxRequests) {
		
		List<Item> items = new ArrayList<Item>();
		
		Date dateToRetrieve = new Date(feed.getSinceDate());
		String label = feed.getLabel();
		
		int page=1, pages=1;
		int numberOfRequests = 0;
		
		List<Coordinates> polygon = feed.getLocation().getPolygon();
		if(polygon == null || polygon.size() == 0) {
			Response response = getResponse(items, numberOfRequests);
			return response;
		}
		
		Double[][] bbox = new Double[polygon.size()][2];
		for(int i = 0; i<polygon.size(); i++) {
			Coordinates point = polygon.get(i);
			bbox[i][0] = point.getLatitude();
			bbox[i][1] = point.getLongitude();
		}
		
		PhotosInterface photosInteface = flickr.getPhotosInterface();
		SearchParameters params = new SearchParameters();
		params.setBBox(bbox[0][0].toString(), bbox[0][1].toString(), bbox[1][0].toString(), bbox[1][1].toString());
		params.setMinUploadDate(dateToRetrieve);
		
		Set<String> extras = new HashSet<String>(Extras.ALL_EXTRAS);
		extras.remove(Extras.MACHINE_TAGS);
		params.setExtras(extras);
		
		while(page<=pages && numberOfRequests<=maxRequests ) {
			PhotoList<Photo> photos;
			try {
				photos = photosInteface.search(params , RESULTS_PER_PAGE, page++);
			} catch (FlickrException e) {
				break;
			}
			
			pages = photos.getPages();
			if(photos.isEmpty()) {
				break;
			}
		
			for(Photo photo : photos) {
				String userid = photo.getOwner().getId();
				StreamUser streamUser = getStreamUser(userid);
				if(streamUser == null) {
					continue;
				}
				
				Item flickrItem = new FlickrItem(photo, streamUser);
				if(label != null) {
					flickrItem.addLabel(label);
				}
				
				items.add(flickrItem);
			}
		}
		
		Response response = getResponse(items, numberOfRequests);
		return response;
    }
	
	@Override
	public Response retrieveGroupFeed(GroupFeed feed, Integer maxRequests) {
		return new Response();
	}
	
	@Override
	public MediaItem getMediaItem(String id) {
		return null;
	}
	
	@Override
	public Item getItem(String id) {
		PhotosInterface photosInteface = flickr.getPhotosInterface();
		try {
			Photo photo = photosInteface.getInfo(id, null);
			if(photo != null) {
				Item item = new FlickrItem(photo);
				return item;
			}
			
		} catch (FlickrException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	@Override
	public StreamUser getStreamUser(String uid) {
		try {
			if(userMap.size() > 5000) {
				userMap.clear();
			}
			
			StreamUser streamUser = userMap.get(uid);
			if(streamUser == null) {
				PeopleInterface peopleInterface = flickr.getPeopleInterface();
				User user = peopleInterface.getInfo(uid);
				
				streamUser = new FlickrStreamUser(user);
				userMap.put(uid, streamUser);
			}	
			return streamUser;
		}
		catch(Exception e) {
			return null;
		}
		
	}
	
	public static void main(String...args) throws Exception {
		
		String flickrKey = "xxxxxxxxxxxxxxxxxxx";
		String flickrSecret = "xxxxxxxxxx";
		
		Credentials credentials = new Credentials();
		credentials.setKey(flickrKey);
		credentials.setSecret(flickrSecret);
		
		FlickrRetriever retriever = new FlickrRetriever(credentials);
		
		Date since = new Date(System.currentTimeMillis()-30*24*3600000l);
		Feed feed = new KeywordsFeed( "1", "obamacare", since.getTime(), "Flickr");
		
		Response response = retriever.retrieve(feed, 1);
		System.out.println(response.getNumberOfItems());
		
		for(Item item : response.getItems()) {
			System.out.println(item);
		}
	}

	@Override
	public List<Item> getItemComments(Item item, long since) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
