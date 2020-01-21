package gr.iti.mklab.framework.common.domain;

import java.util.Date;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

@Entity(value="StreamUser", noClassnameStored=false)
@Indexes(@Index("id"))
public class StreamUser extends JSONable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3558927430206936262L;
    
    // The internal id of a user in a specific social media
    protected String userid;
    
    // The username of the user in a specific social media
    protected String username;
    
    // A human readable version of the name of a user
    protected String name;
    
    // The name of the stream that a User comes from
    protected String source;
    
    // The number of Items posted from the user
    protected Integer items = 0;
    
    // The profile image of a user
    protected String profileImage;
    
    // The URL of the user page in the specific social media
    protected String pageUrl;
    
    // A URL of the personal web page of the user
    protected String url;
    
    // A short description of the user
    protected String description;
    
    // The date that this account has been created
    protected Date createdAt;
    
    // The last time that this user has been updated
    protected Long lastUpdated;
    
    // The location associated with a user
    protected String location;
    
    // The number of the times a user has been mentioned from other users
    protected long mentions = 0;
    
    // The number of friends of a User
    protected Long friends = 0l;
    
    // The number of followers of a User
    protected Long followers = 0l;
    
    // The number of the times a user is listed
    protected Long listedCount = 0l;
    
    // The number of times the Items of this user have been shared
    protected Long shares = 0L;
    
    // An indicator whether the user is verified by the social media service
    protected Boolean verified = false;
    
    // Timezone of the specific user
    protected String timezone;
    
    protected long favorities = 0L;
    
    // Getters & Setters for the fields of this class
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
         this.username = username;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public Integer getItems() {
        return items;
    }

    public void setItems(Integer items) {
        this.items = items;
    }
    
    public void incItems(Integer items) {
        this.items += items;
    }
    
    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
    
    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public Long getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Long lastUpdated){
    	this.lastUpdated = lastUpdated;
    }
    
    public String getLocation() {
        if ( location==null || location.equals("")) {
            return "Unknown";
        }
        return location;
    }
    
    public Long getMentions() {
        return mentions;
    }

    public void setMentions(Long mentions) {
        this.mentions = mentions;
    }
    
    public void incMentions(Long mentions) {
        this.mentions += mentions;
    }
    
    public Long getFriends() {
        return friends;
    }

    public void setFriends(Long friends) {
        this.friends = friends;
    }

    public Long getFollowers() {
        return followers;
    }

    public void setFollowers(Long followers) {
        this.followers = followers;
    }
    
    public long getListedCount() {
    	return listedCount;
    }
    
    public void setListedCount(long listedCount) {
    	this.listedCount = listedCount;
    }
    
    public Long getShares() {
        return shares;
    }

    public void setShares(Long shares) {
        this.shares = shares;
    }
    
    public void incShares(Long shares) {
        this.shares += shares;
    }
    
    public Long getFavorities() {
        return favorities;
    }

    public void setFavoritiesCount(Long shares) {
        this.shares = shares;
    }
    
    public void setVerified(Boolean verified) {
    	this.verified = verified;
    }
    
    public Boolean isVerified() {
    	return verified;
    }
    
}
