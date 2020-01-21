package gr.iti.mklab.framework.abstractions.socialmedia.items;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.photos.GeoData;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.tags.Tag;

import gr.iti.mklab.framework.abstractions.socialmedia.users.FlickrStreamUser;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.Location;
import gr.iti.mklab.framework.common.domain.MediaItem;
import gr.iti.mklab.framework.common.domain.Source;
import gr.iti.mklab.framework.common.domain.StreamUser;

/**
 * Class that holds the information of a flickr photo
 * @author Manos Schinas
 */
public class FlickrItem extends Item {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4323341976887218659L;
	
	public FlickrItem() {
		
	}
	
	@SuppressWarnings("deprecation")
	public FlickrItem(Photo photo) {

		if (photo == null || photo.getId() == null) return;
		
		//Id
		id = Source.Flickr + "#" + photo.getId();
		//SocialNetwork Name
		source = Source.Flickr.toString();
		//Timestamp of the creation of the photo
		publicationTime = photo.getDatePosted().getTime();
		//Title of the photo
		if(photo.getTitle()!=null){
			
				title = photo.getTitle();
				text = photo.getTitle();
			
		}
		//Description of the photo
		description = photo.getDescription();
		//Tags of the photo
		Collection<Tag> photoTags = photo.getTags();
		if (photoTags != null) {
			List<String> tagsList = new ArrayList<String>();
			for(Tag tag : photoTags) {
				String tagStr = tag.getValue();
				if(tagStr != null && !tagStr.contains(":"))
					tagsList.add(tagStr);
			}
			tags = tagsList.toArray(new String[tagsList.size()]);
		}
		
		//User that posted the photo
        User user = photo.getOwner();
        if(user != null) {
        	streamUser = new FlickrStreamUser(user);
        	uid = streamUser.getId();
        }
        
		//Location
		if(photo.hasGeoData()){
			
			GeoData geo = photo.getGeoData();
			
			double latitude = (double)geo.getLatitude();
			double longitude = (double) geo.getLongitude();
			
			location = new Location(latitude, longitude);
		}
		
		pageUrl = photo.getUrl();
		
		//Popularity
		comments = (long) photo.getComments();
		
		//Getting the photo
		try {
			String url = null;
			String thumbnail = photo.getMediumUrl();
			if(thumbnail==null) {
				thumbnail = photo.getThumbnailUrl();
			}
			URL mediaUrl = null;
			if((url = photo.getLargeUrl()) != null) {
				mediaUrl = new URL(url);
			
			}
			else if ((url = photo.getMediumUrl()) != null) {
				mediaUrl = new URL(url);
			}
			
			if(mediaUrl != null) {
				//url
				MediaItem mediaItem = new MediaItem(mediaUrl);
				
				String mediaId = Source.Flickr + "#"+photo.getId(); 
				
				//id
				mediaItem.setId(mediaId);
				//SocialNetwork Name
				mediaItem.setSource(source);
				//Reference
				mediaItem.setReference(id);
				//Type 
				mediaItem.setType("image");
				//Time of publication
				mediaItem.setPublicationTime(publicationTime);
				//Author
				if(streamUser != null) {
					mediaItem.setUser(streamUser);
					mediaItem.setUserId(streamUser.getId());
				}
				//PageUrl
				mediaItem.setPageUrl(photo.getUrl());
				//Thumbnail
				mediaItem.setThumbnail(thumbnail);
				//Title
				mediaItem.setTitle(title);
				//Description
				mediaItem.setDescription(description);
				//Tags
				mediaItem.setTags(tags);
				//Popularity
				mediaItem.setComments(new Long(photo.getComments()));
				mediaItem.setViews(new Long(photo.getViews()));
				//Location
				mediaItem.setLocation(location);
				
				//Store mediaItems and their ids 
				mediaItems.add(mediaItem);
				mediaIds.add(mediaId);
				
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public FlickrItem(Photo photo, StreamUser streamUser) {
		this(photo);

		//User that posted the photo
		this.streamUser = streamUser;
		uid = streamUser.getId();

		for(MediaItem mItem : mediaItems) {
			mItem.setUserId(uid);
		}

	}
	
}
