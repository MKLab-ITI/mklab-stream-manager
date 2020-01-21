package gr.iti.mklab.framework.abstractions.socialmedia.items;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;
import com.google.gdata.data.Person;
import com.google.gdata.data.extensions.Rating;
import com.google.gdata.data.media.mediarss.MediaDescription;
import com.google.gdata.data.media.mediarss.MediaPlayer;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaContent;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YtStatistics;

import gr.iti.mklab.framework.abstractions.socialmedia.users.YoutubeStreamUser;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.MediaItem;
import gr.iti.mklab.framework.common.domain.Source;
import gr.iti.mklab.framework.common.domain.StreamUser;

/**
 * Class that holds the information of a youtube video
 * 
 * @author 	Manos Schinas - manosetro@iti.gr
 * 
 */
public class YoutubeItem extends Item {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6355819301582285835L;
	
	public YoutubeItem() {
		
	}
			
	public YoutubeItem(VideoEntry videoEntry) {
		
		if (videoEntry == null || videoEntry.getId() == null) 
			return;
		
		YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
		//Id
		id = Source.Youtube+"#"+mediaGroup.getVideoId();
		//SocialNetwork Name
		source = Source.Youtube.toString();
		//Timestamp of the creation of the video
		publicationTime = mediaGroup.getUploaded().getValue();
		//Title of the video
		title = mediaGroup.getTitle().getPlainTextContent();
		//Description of the video
		MediaDescription desc = mediaGroup.getDescription();
		description = desc==null ? "" : desc.getPlainTextContent();
		//User that uploaded the video
		List<Person> authors = videoEntry.getAuthors();
		if(authors.size()>0) {
			streamUser = new YoutubeStreamUser(authors.get(0));
		}
		else{
			if(mediaGroup.getUploader()!=null){
				streamUser = new YoutubeStreamUser(mediaGroup.getUploader());
			}
		}
		uid = streamUser.getId();
		//Popularity
		YtStatistics statistics = videoEntry.getStatistics();
		if(statistics!=null){
			likes = statistics.getFavoriteCount();
			
		}

		//Getting the video
		List<MediaThumbnail> thumbnails = mediaGroup.getThumbnails();
		MediaPlayer mediaPlayer = mediaGroup.getPlayer();
		
		// Page Url of this Item
		pageUrl = mediaPlayer.getUrl();
				
		String videoID = videoEntry.getId().substring(videoEntry.getId().indexOf("video:")+("video:").length());
		List<YouTubeMediaContent> mediaContent = mediaGroup.getYouTubeContents();
		
		String videoURL = null;
		for(YouTubeMediaContent content : mediaContent){
			if(content.getType().equals("application/x-shockwave-flash")) {
				videoURL = content.getUrl();
				break;
			}
		}	
		if (videoURL == null) 
			videoURL = mediaPlayer.getUrl();
		
		URL url = null;
		try {
			url = new URL(videoURL);
		} catch (MalformedURLException e1) {

		}
		
		int size = 0;
		MediaThumbnail thumbnail = null;
		for(MediaThumbnail thumb : thumbnails) {
			int t_size = thumb.getWidth()*thumb.getHeight();
			if(size < t_size) {
				size = t_size;
				thumbnail = thumb; 
			}
		}
		
		if(thumbnail != null) {
			//url
			MediaItem mediaItem = new MediaItem(url);
			
			String mediaId = Source.Youtube + "#"+videoID; 
			
			//id
			mediaItem.setId(mediaId);
			//SocialNetwork Name
			mediaItem.setSource(source);
			//Reference
			mediaItem.setReference(id);
			//Type 
			mediaItem.setType("video");
			//Time of publication
			mediaItem.setPublicationTime(publicationTime);
			//Author
			if(streamUser != null) {
				mediaItem.setUser(streamUser);
				mediaItem.setUserId(streamUser.getId());
			}
			//PageUrl
			String pageUrl = mediaPlayer.getUrl();
			mediaItem.setPageUrl(pageUrl);
			//Thumbnail
			String thumbUrl = thumbnail.getUrl();
			mediaItem.setThumbnail(thumbUrl);	
			//Title
			mediaItem.setTitle(title);
			//Description
			mediaItem.setDescription(description);
			//Tags
			mediaItem.setTags(tags);
			//Popularity
			if(statistics!=null){
				mediaItem.setLikes(statistics.getFavoriteCount());
				mediaItem.setViews(statistics.getViewCount());
			}
			Rating rating = videoEntry.getRating();
			if(rating != null) {
				mediaItem.setRatings(rating.getAverage());
			}
			//Size
			mediaItem.setSize(thumbnail.getWidth(), thumbnail.getHeight());
			
			mediaItems.add(mediaItem);
			mediaIds.add(mediaId);
		}

	}
	
	public YoutubeItem(Video video) {
		
		if (video == null || video.getId() == null) {
			return;
		}
		
		VideoSnippet snippet = video.getSnippet();
		VideoStatistics statistics = video.getStatistics();

		//Id
		id = Source.Youtube + "#" + video.getId();
		
		//SocialNetwork Name
		source = Source.Youtube.toString();
		
		//Timestamp of the creation of the video
		publicationTime = snippet.getPublishedAt().getValue();
		
		//Title of the video
		title = snippet.getTitle();
		
		//Description of the video
		description = snippet.getDescription();
	
		List<String> tagsList = snippet.getTags();
		if(tagsList != null) {
			tags = tagsList.toArray(new String[tagsList.size()]);
		}
		
		//User that uploaded the video
		//List<Person> authors = videoEntry.getAuthors();
		//if(authors.size()>0) {
		//	streamUser = new YoutubeStreamUser(authors.get(0));
		//}
		//else{
			//if(mediaGroup.getUploader()!=null){
			//	streamUser = new YoutubeStreamUser(mediaGroup.getUploader());
			//}
		//}
		uid = Source.Youtube + "#" + snippet.getChannelId();
				
				
		//Getting the video
		ThumbnailDetails thumbnails = snippet.getThumbnails();
		Thumbnail thumbnail = thumbnails.getHigh();
		
		if(thumbnail != null) {
			String videoURL = "https://www.youtube.com/embed/" + video.getId();
			//url
			URL url;
			try {
				url = new URL(videoURL);
			} catch (MalformedURLException e) {
				return;
			}
			
			MediaItem mediaItem = new MediaItem(url);
			
			String mediaId = Source.Youtube + "#" + video.getId(); 
			
			//id
			mediaItem.setId(mediaId);
			
			//SocialNetwork Name
			mediaItem.setSource(source);
			
			//Reference
			mediaItem.setReference(id);
			
			//Type 
			mediaItem.setType("video");
			
			//Time of publication
			mediaItem.setPublicationTime(publicationTime);
			//Author
			if(streamUser != null) {
				mediaItem.setUser(streamUser);
				mediaItem.setUserId(streamUser.getId());
			}
			//PageUrl
			mediaItem.setPageUrl(pageUrl);
			
			//Thumbnail
			String thumbUrl = thumbnail.getUrl();
			mediaItem.setThumbnail(thumbUrl);	
			
			//Title
			mediaItem.setTitle(title);
			
			//Description
			mediaItem.setDescription(description);
			
			//Tags
			mediaItem.setTags(tags);
			
			//Popularity
			if(statistics != null) {
				if(statistics.getLikeCount() != null) {
					mediaItem.setLikes(statistics.getLikeCount().longValue());
				}
				if(statistics.getCommentCount() != null) {
					mediaItem.setComments(statistics.getCommentCount().longValue());
				}
				if(statistics.getViewCount() != null) {
					mediaItem.setViews(statistics.getViewCount().longValue());
				}
			}
			
			//Rating rating = videoEntry.getRating();
			//if(rating != null) {
			//	mediaItem.setRatings(rating.getAverage());
			//}
			
			//Size
			mediaItem.setSize(thumbnail.getWidth().intValue(), thumbnail.getHeight().intValue());
			
			mediaItems.add(mediaItem);
			mediaIds.add(mediaId);
		}
		
		//Popularity
		if(statistics != null) {
			if(statistics.getLikeCount() != null) {
				likes = statistics.getLikeCount().longValue();
			}
			if(statistics.getCommentCount() != null) {
				comments = statistics.getCommentCount().longValue();
			}
		}
		
		// Page Url of this Item
		pageUrl = "https://www.youtube.com/watch?v=" + video.getId(); 
		
	}
	
	public YoutubeItem(VideoEntry videoEntry, YoutubeStreamUser user) {
		this(videoEntry);
		//User that posted the post
		streamUser = user;
		uid = streamUser.getId();
		
		for(MediaItem mItem : this.mediaItems) {
			mItem.setUserId(uid);
		}
	}
	
	public YoutubeItem(Video video, StreamUser user) {
		this(video);
		//User that posted the post
		streamUser = user;
		uid = streamUser.getId();
		for(MediaItem mItem : this.mediaItems) {
			mItem.setUserId(uid);
		}
	}
	
}
