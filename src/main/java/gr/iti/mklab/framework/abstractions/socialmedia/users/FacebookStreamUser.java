package gr.iti.mklab.framework.abstractions.socialmedia.users;

import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.Location;
import com.restfb.types.Page;
import com.restfb.types.ProfilePictureSource;
import com.restfb.types.User;

import gr.iti.mklab.framework.common.domain.Source;
import gr.iti.mklab.framework.common.domain.StreamUser;

/**
 * Class that holds the information of a facebook user or a facebook page
 */
public class FacebookStreamUser extends StreamUser {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3234574808873979755L;

	public FacebookStreamUser(User user) {
		
		if (user == null) return;
		
		//Id
		id = Source.Facebook+"#"+user.getId();
		
		//The id of the user in the network
		userid = user.getId();
		
		//The name of the user
		if(user.getFirstName() == null && user.getLastName() == null)
			name = user.getName();
		else if(user.getMiddleName() != null) 
			name = user.getFirstName()+" "+user.getMiddleName()+" "+user.getLastName();
		else
			name = user.getFirstName()+" "+user.getLastName();
		
		//streamId
		source =  Source.Facebook.toString();
		
		//The description of the user
		description = user.getAbout();
		
		//The link to the user's profile
		pageUrl = user.getLink(); 
		if(pageUrl == null) {
			pageUrl = username==null ? ("https://www.facebook.com/profile.php?id="+userid) : ("http://www.facebook.com/"+username);
		}
		
		profileImage = "https://graph.facebook.com/" + userid + "/picture";
		
		//Last time user's profile was updated
		if(user.getUpdatedTime() != null)
			lastUpdated = user.getUpdatedTime().getTime();
		
		//Location
		if(user.getLocation()!= null)
			location = user.getLocation().getName();
		
		//Is the user a verified user
		if(user.getVerified() != null)
			verified = user.getVerified();
		
		
	}

	public FacebookStreamUser(Page page) {
		if (page == null) return;
		
		//Id
		id = Source.Facebook+"#"+page.getId();
		
		//The id of the page in the network
		userid = page.getId();
		
		//The name of the page
		name = page.getName();
		
		//The username of the page
		username = page.getUsername();
		
		//The name of the Social Network
		source = Source.Facebook.toString();
		
		//The description of the page
		description = page.getAbout();
		
		pageUrl = page.getLink(); 
		if(pageUrl == null) {
			pageUrl = username==null ? ("https://www.facebook.com/profile.php?id="+userid) : ("http://www.facebook.com/"+username);
		}
		
		//Avatar of the page
		ProfilePictureSource picture = page.getPicture();
		if(picture == null || picture.getUrl() == null) {
			profileImage = "https://graph.facebook.com/" + userid + "/picture";
		}
		else {
			profileImage = picture.getUrl();
		}
		
		//Number of people talking about the page
		followers = page.getTalkingAboutCount();
		
		//Location 
		Location loc = page.getLocation();
		if(loc != null) {
			location = loc.getCity();
		}
		
		if(page.getLikes() != null) {
			favorities = page.getLikes().getTotalCount();
		}
		
		verified = page.getIsVerified();
		
		url = page.getWebsite();
		
	}
	
	public FacebookStreamUser(CategorizedFacebookType user) {

		if (user == null) return;

		//Id
		id = Source.Facebook+"#"+user.getId();
		//The id of the page in the network
		userid = user.getId();
		//The name of the page
		name = user.getName();
		//Link to the page
		pageUrl = "https://www.facebook.com/profile.php?id="+userid;
		//Avatar of the page
		profileImage = "https://graph.facebook.com/" + userid + "/picture";
		
	}
	
}
