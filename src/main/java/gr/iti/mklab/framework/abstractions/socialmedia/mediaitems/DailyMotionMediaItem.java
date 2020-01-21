package gr.iti.mklab.framework.abstractions.socialmedia.mediaitems;

import java.net.URL;

import com.google.api.client.util.Key;

import gr.iti.mklab.framework.common.domain.Location;
import gr.iti.mklab.framework.common.domain.MediaItem;

/**
 * Class that holds the information regarding the dailymotion video
 * @author manosetro - manosetro@iti.gr
 *
 */
public class DailyMotionMediaItem extends MediaItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8159926627140677547L;

	public DailyMotionMediaItem(DailyMotionVideo video) throws Exception {
		super(new URL(video.embed_url));
		
		//id
		this.setId("Dailymotion#" + video.id);
		//SocialNetwork Name
		this.setSource("Dailymotion");
		//Type 
		this.setType("video");
		//Time of publication
		this.setPublicationTime(1000 * video.created_time);
		//PageUrl
		this.setPageUrl(video.url);
		//Thumbnail
		this.setThumbnail(video.thumbnail_url);
		//Title
		this.setTitle(video.title);
		//Tags
		this.setTags(video.tags);
		//Popularity
		comments = new Long(video.comments_total);
		views = new Long(video.views_total);
		ratings = new Float(video.ratings_total);
		//Location
		double[] geoloc = video.geoloc;
		if(geoloc != null && geoloc.length>0) {
			Location location = new Location(geoloc[0], geoloc[1]);
			this.setLocation(location);
		}
	
	}

	/** Represents a daily motion video. */
	public static class DailyMotionVideo {
		@Key
		public String id, title, url, embed_url, thumbnail_url;
		@Key
		public String[] tags;
		@Key
		public int rating, ratings_total, views_total, comments_total;
		@Key
		public long created_time;
		@Key
		public double[] geoloc;
	}
	
}
