package gr.iti.mklab.framework.abstractions.socialmedia.users;

import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.google.api.services.youtube.model.ChannelStatistics;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.gdata.data.Link;
import com.google.gdata.data.Person;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.UserProfileEntry;
import com.google.gdata.data.youtube.YtUserProfileStatistics;

import gr.iti.mklab.framework.common.domain.Source;
import gr.iti.mklab.framework.common.domain.StreamUser;

/**
 * Class that holds the information of a youtube user
 * @author ailiakop
 */
public class YoutubeStreamUser extends StreamUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9208863907526546716L;

	public YoutubeStreamUser(String user) {

		if (user == null) {
			return;
		}
		
		//Id
		id = Source.Youtube+"#"+user;
		//The name of the user
		username = user;
		//streamId
		source = Source.Youtube.toString();
	}

	public YoutubeStreamUser(Person user) {

		if (user == null) 
			return;
		
		//Id
		id = Source.Youtube+"#"+user.getName();
		//The id of the user in the network
		userid = user.getName();
		//The name of the user
		username = user.getName();
		//streamId
		source = Source.Youtube.toString();
		//The link to the user's profile
		pageUrl = user.getUri();
	}
	
	public YoutubeStreamUser(UserProfileEntry user) {

		if (user == null) 
			return;

		//Id
		id = Source.Youtube+"#"+user.getUsername();

		//The id of the user in the network
		userid = user.getUsername();
		//The username of the user
		username = user.getUsername();
		//The name of the user
		name = (user.getFirstName()==null?"":user.getFirstName()+" ") + (user.getLastName()==null?"":user.getLastName());
		//streamId
		source = Source.Youtube.toString();

		MediaThumbnail thumnail = user.getThumbnail();
		profileImage = thumnail.getUrl();

		Link link = user.getLink("alternate", "text/html");
		if(link != null) {
			pageUrl = link.getHref();
		}
		
		location = user.getLocation();

		description = user.getAboutMe();

		YtUserProfileStatistics statistics = user.getStatistics();
		if(statistics != null) {
			followers = statistics.getSubscriberCount();
		}
	}
	
	public YoutubeStreamUser(Channel channel) {
		this(channel, null);
	}
	
	public YoutubeStreamUser(Channel channel, String username) {

		if (channel == null) {
			return;
		}
		
		ChannelSnippet snippet = channel.getSnippet();

		//Id
		id = Source.Youtube + "#" + channel.getId();

		//The id of the user in the network
		userid = channel.getId();
		
		//The username of the user
		if(username == null) {
			this.username =  channel.getId();
		}
		else {
			this.username = username;	
		}
		
		
		//The name of the user
		name = snippet.getTitle();
		
		//source
		source = Source.Youtube.toString();

		ThumbnailDetails thumbnails = snippet.getThumbnails();
		Thumbnail thumbnail = thumbnails.getHigh();
		profileImage = thumbnail.getUrl();

		pageUrl = "https://www.youtube.com/channel/" + channel.getId();
		
		//location = user.getLocation();

		description = snippet.getDescription();

		ChannelStatistics statistics = channel.getStatistics();
		if(statistics != null) {
			followers = statistics.getSubscriberCount().longValue();
			items = statistics.getVideoCount().intValue();
		}
	}
}
