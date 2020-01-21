package gr.iti.mklab.framework.abstractions.socialmedia.users;

import java.util.Date;

import twitter4j.User;
import gr.iti.mklab.framework.common.domain.Source;
import gr.iti.mklab.framework.common.domain.StreamUser;

/**
 * Class that holds the information of a twitter user
 * 
 * @author manosetro
 * 
 */
public class TwitterStreamUser extends StreamUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6485573747686458937L;

	public TwitterStreamUser(User user) {

		if (user == null) 
			return;
		
		//Id
		id = Source.Twitter + "#" + user.getId();
		//The id of the user in the network
		userid = Long.toString(user.getId());
		//The name of the user
		name = user.getName();
		//The username of the user
		username = user.getScreenName();
		//streamId
		source = Source.Twitter.toString();
		//The description of the user
		description = user.getDescription();
		//Profile picture of the user
		if(user.getBiggerProfileImageURL() != null) {
			profileImage = user.getBiggerProfileImageURL();
		}
		else {
			profileImage = user.getProfileImageURL();
		}
		
		
		//Page URL of the user
		pageUrl = "https://twitter.com/" + user.getScreenName();
		url = user.getURL();
		
		verified = user.isVerified();
		listedCount = (long) user.getListedCount();
		
		//Statuses of the user
		items = user.getStatusesCount();
		//Creation date of user's profile
		Date date = user.getCreatedAt();
		if(date != null) {
			createdAt = new Date(date.getTime());
		}
		
		//Location
		location = user.getLocation();
		//Followers of the user
		followers = (long) user.getFollowersCount();
		//Friends of the user
		friends =  (long) user.getFriendsCount();
		
		favorities = user.getFavouritesCount();
		
		timezone = user.getTimeZone();
	}
	
}
