package gr.iti.mklab.framework.abstractions.socialmedia.mediaitems;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.api.client.util.Key;

import gr.iti.mklab.framework.common.domain.MediaItem;

/**
 * Class that holds the information regarding the vimeo media item
 * @author manosetro - manosetro@iti.gr
 */
public class VimeoMediaItem extends MediaItem {
	
	private static final long serialVersionUID = -5822189263800534128L;
	
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public VimeoMediaItem(VimeoVideo video) throws Exception {
		//url
		super(new URL("http://vimeo.com/moogaloop.swf?clip_id="+video.id));
		
		//Id
		this.setId("Vimeo#"+video.id);
		//SocialNetwork Name
		this.setSource("Vimeo");
		//Type 
		this.setType("video");
		//Time of publication
		try {
			Date date = formatter.parse(video.upload_date);
			this.setPublicationTime(date.getTime());
		}
		catch(Exception e) {}
		//PageUrl
		this.setPageUrl(video.url);
		//Thumbnail
		this.setThumbnail(video.thumbnail_large);
		//Title
		this.setTitle(video.title);
		//Description
		this.setDescription(video.description);
		//Tags
		String tags = video.tags;
		if(tags != null) {
			this.setTags(tags.split(","));
		}
		//Description
		this.setDescription(video.description);
		//Popularity
		likes = new Long(video.stats_number_of_likes);
		views = new Long(video.stats_number_of_plays);
		comments = new Long(video.stats_number_of_comments);
		//Size
		this.setSize(video.width, video.height);

		this.setUserId("Vimeo#" + video.user_id);
	}
	
	public static class VimeoVideo {
		@Key
		public int id;
		@Key
		public String title, url, thumbnail_large, description, tags;
		@Key
		public int stats_number_of_comments, stats_number_of_likes, stats_number_of_plays;
		@Key
		public String upload_date;
		@Key
		public int user_id;
		@Key
		public int height, width;
	}

}
