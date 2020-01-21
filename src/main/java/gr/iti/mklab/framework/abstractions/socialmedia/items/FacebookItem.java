package gr.iti.mklab.framework.abstractions.socialmedia.items;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.Comment;
import com.restfb.types.Place;
import com.restfb.types.Post;
import com.restfb.types.Comments;
import com.restfb.types.Likes;
import com.restfb.types.Likes.LikeItem;
import com.restfb.types.Post.Shares;
import com.restfb.types.StoryAttachment;
import com.restfb.types.StoryAttachment.Media;
import com.restfb.types.User;

import gr.iti.mklab.framework.abstractions.socialmedia.users.FacebookStreamUser;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.Location;
import gr.iti.mklab.framework.common.domain.MediaItem;
import gr.iti.mklab.framework.common.domain.Source;
import gr.iti.mklab.framework.common.domain.StreamUser;
import gr.iti.mklab.framework.common.domain.WebPage;

/**
 * Class that holds the information of a facebook post
 * 
 * @author Manos Schinas - manosetro@iti.gr
 *
 */
public class FacebookItem extends Item {
	
	
	private static final long serialVersionUID = 2267260425325527385L;

	
	public FacebookItem() {
		
	}
	
	public FacebookItem(Post post) {
		
		if (post == null || post.getId() == null) {
			return;
		}
		
		//Id
		id = Source.Facebook + "#" + post.getId();
		
		//SocialNetwork Name
		source = Source.Facebook.toString();
		
		//Timestamp of the creation of the post
		publicationTime = post.getCreatedTime().getTime();
		
		//is this the original or a shared fb post
		original = true;
		
		String type = post.getType();
			
  		description = post.getDescription();
  		
  		if(type.equals("status") || type.equals("photo") || type.equals("video")) {
  			String title = post.getMessage();
  			if(title == null) {
  	 			if(post.getCaption() != null) {
  	 				title = post.getCaption();
  	 			}
  	 			if(post.getName() != null) {
  	 				title = post.getName();
  	 			}
  	 			else if(description != null) {
  	 				if(description.length() > 100) {
  	 					title = description.subSequence(0, 100)+"...";
  	 				}
  	 				else { 
  	 					title = description;
  	 				}
  	 			}
  	 			else {
  	 				title = "";
  	 			}
  			}
  			else if(title.length() > 100) {
  				this.title = title.subSequence(0, 100) + "...";
  			}
  			else {
  				this.title = title;
  			}
  			
  			if(description == null) {
  				description = title;
  			}
		}
  		else if(type.equals("link")) {
  			title = post.getName();
  			description = post.getDescription();
		}

  		if(description != null) {
  		 	text = description;
  		}
  		else {
  		 	text = title;
  		}
  		
		//Location 
		Place place = post.getPlace();
		if(place != null) {
			String placeName = place.getName();
			com.restfb.types.Location loc = place.getLocation();
			if(loc != null) {
				Double latitude = loc.getLatitude();
				Double longitude = loc.getLongitude();
		
				location = new Location(latitude, longitude, placeName);
			}
		}
		
		//Popularity of the post
		Likes postLikes = post.getLikes();
		if(postLikes != null) {
			if(postLikes.getTotalCount() == null) {
				List<LikeItem> likeData = postLikes.getData();
				if(likeData != null) {
					likes = (long) likeData.size();
				}
			}
			else {
				likes = postLikes.getTotalCount();
			}
		}
		else {
			likes = post.getLikesCount();
		}
		
		if(post.getSharesCount() != null && post.getSharesCount() > 0) {
			shares = post.getSharesCount();
		}
		else {
			Shares postShares = post.getShares();
			if(postShares != null) {
				shares = postShares.getCount();
			}
		}
		
		Comments cmnts = post.getComments();
		if(cmnts != null) {
			Long commentsCount = cmnts.getTotalCount();
			if(commentsCount != null) {
				comments = commentsCount;
			}
			else {
				List<Comment> data = cmnts.getData();
				if(data != null)
					comments = (long) data.size();
			}
		}
		
		//Media Items - WebPages in a post		
		if(type.equals("photo")) {
			pageUrl = post.getLink();
			String picture = post.getPicture();
			String fullPicture = post.getFullPicture();
			try {
				if (picture != null && fullPicture != null) { 
						URL pictureUrl = new URL(picture);
						MediaItem mediaItem = new MediaItem(pictureUrl);
						
						//id
						String mediaId = Source.Facebook + "#" + post.getObjectId();
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
						mediaItem.setPageUrl(pageUrl);
						//Thumbnail
						mediaItem.setThumbnail(picture);
						//Title
						mediaItem.setTitle(title);
						//Description
						mediaItem.setDescription(description);
						//Tags
						mediaItem.setTags(tags);
						//Popularity
						mediaItem.setLikes(likes);
						mediaItem.setShares(shares);

							
						//Store mediaItems and their ids 
						mediaItems.add(mediaItem);	
						mediaIds.add(mediaId);
						
					}
			} catch (MalformedURLException e) { 
				e.printStackTrace();
			}
		}
		else if(type.equals("link")) {
			
			pageUrl = "https://www.facebook.com/" + post.getId();
			
			webPages = new ArrayList<WebPage>();
			String picture = post.getPicture();
			String fullPicture = post.getFullPicture();
			if (picture != null && fullPicture != null) { 
				try {
					URL pictureUrl = new URL(fullPicture);
				
					//url
					MediaItem mediaItem = new MediaItem(pictureUrl);
					
					//id
					mediaItem.setId(id);
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
					String pageUrl = "https://www.facebook.com/" + id.replaceAll("_", "/posts/");
					mediaItem.setPageUrl(pageUrl);
					
					//Thumbnail
					mediaItem.setThumbnail(picture);	
					//Title
					mediaItem.setTitle(title);
					//Description
					mediaItem.setDescription(description);
					//Tags
					mediaItem.setTags(tags);	
					//Popularity
					mediaItem.setLikes(likes);
					mediaItem.setShares(shares);
					mediaItem.setComments(comments);
					
					//Store mediaItems and their ids 
					mediaItems.add(mediaItem);
					mediaIds.add(mediaItem.getId());
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			
			String link = post.getLink();
			if (link != null) {
				WebPage webPage = new WebPage(link, id);
				webPage.setSource(source);
				webPages.add(webPage);
			}
		}
		else if(type.equals("video")) {
			
			pageUrl = "https://www.facebook.com/" + post.getId();
			
			String videoUrl = post.getSource();
			String picture = post.getFullPicture();
			if(picture != null && videoUrl != null) {
				try {
					URL url = new URL(videoUrl);
					MediaItem mediaItem = new MediaItem(url);
					
					//id
					String mediaId = Source.Facebook+"#"+post.getId();
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
					mediaItem.setThumbnail(picture);
					//Title
					mediaItem.setTitle(title);
					//Description
					mediaItem.setDescription(description);
					//Tags
					mediaItem.setTags(tags);
					//Popularity
					mediaItem.setLikes(likes);
					mediaItem.setShares(shares);
					
					//Store mediaItems and their ids 
					mediaItems.add(mediaItem);
					mediaIds.add(mediaId);
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			
		}
		else {
			pageUrl = "https://www.facebook.com/" + post.getId();
		}
	
		CategorizedFacebookType from = post.getFrom();
		uid = Source.Facebook + "#" + from.getId();
		
	}

	
	public FacebookItem(Post post, StreamUser user) {
		
		this(post);
		
		//User that posted the post
		streamUser = user;
		uid = streamUser.getId();
		
		for(MediaItem mi : this.getMediaItems()) {
			mi.setUserId(uid);
		}
		
	}
	
	public FacebookItem(Comment comment, Post post, User user) {
		
		if (comment == null) return;
		
		//Id
		id = Source.Facebook+"#"+comment.getId();
		
		//Reference to the original post
		reference = Source.Facebook+"#"+post.getId();
		
		//SocialNetwork Name
		source = Source.Facebook.toString();
		//Timestamp of the creation of the post
		publicationTime = comment.getCreatedTime().getTime();
		//Message that post contains
		String msg = comment.getMessage();
		if(msg != null) {
			if(msg.length()>100) {
				title = msg.subSequence(0, 100)+"...";
			}
			else{
				title = msg;
			}
			description = "Comment";
		}
		
		//All the text inside the comment
		text = msg; 
		
		pageUrl = "https://www.facebook.com/" + post.getId();
		
		//User that posted the comment
		if(user != null) {
			streamUser = new FacebookStreamUser(user);
			uid = streamUser.getId();
		}
		else {
			CategorizedFacebookType from = comment.getFrom();
			streamUser = new FacebookStreamUser(from);
			uid = streamUser.getId();
		}
		
		original = false;
		
		//Popularity of the post
		if(comment.getLikeCount() != null) {
			likes = comment.getLikeCount();
		}
		
		StoryAttachment attachment = comment.getAttachment();
		if(attachment != null) {
			Media media = attachment.getMedia();

			String url = attachment.getUrl();
			try {
				URL mediaUrl = new URL(url);

				String mediaId = Source.Facebook+"#"+media.getId();
				//url
				MediaItem mediaItem = new MediaItem(mediaUrl);

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
				String pageUrl = post.getLink();
				mediaItem.setPageUrl(pageUrl);
				
				//Thumbnail
				String thumbnail = post.getPicture();
				mediaItem.setThumbnail(thumbnail);
				//Title
				mediaItem.setTitle(title);
				//Description
				mediaItem.setDescription(description);
				//Tags
				mediaItem.setTags(tags);
				//Popularity
				mediaItem.setLikes(likes);
				mediaItem.setShares(shares);

				//Store mediaItems and their ids 
				mediaItems.add(mediaItem);
				mediaIds.add(mediaId);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	
	}
}
