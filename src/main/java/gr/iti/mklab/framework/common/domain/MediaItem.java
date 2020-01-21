package gr.iti.mklab.framework.common.domain;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Transient;

/**
 * Represents a single stream media item and acts as an envelop for the native
 * stream media object.
 *
 * @author manosetro - manosetro@iti.gr
 *
 */
@Entity(noClassnameStored = true)
@Indexes({
	@Index("id"), 
	@Index("-publicationTime")
})
public class MediaItem extends JSONable {

    /**
     *
     */
    private static final long serialVersionUID = 7811714823188242818L;
    
    // The URL of a media item
    private String url;
    
    // Thumbnail version of a media item
    private String thumbnail;
    
    // The URL of the page that contains the media item
    private String pageUrl;
    
    // The name of the stream that a Media Item comes from
    private String source;
    
    // The id of the first Item that contains the MediaItem
    private String reference;
    
    // The id of the user that posted the first Item that contains the MediaItem
    private String uid;
    
    // A detailed instance of the user of an Item
    // This field is not exposed in mongodb
    @Transient
    private StreamUser streamUser;
    
    // Textual information
    private String title;

    private String description;

    private String[] tags;
   
    // The type of a media item. Can only be image/video
    private String type;
    
    // A set of labels that indicate the feeds that are associated with this media item
    protected Set<String> labels;
    
    // The publication time of the first item that share the media item
    private long publicationTime;
    
    private String[] mentions;
    
    // Popularity values
    protected Long likes = 0L;

    protected Long shares = 0L;

    protected Long comments = 0L;

    protected Long views = 0L;

    protected Float ratings = 0F;
    
    // The sentiment value of a MediaItem
    protected int sentiment;
    
    // A list of concepts related to the MediaItem
    private List<Concept> concepts = null;
    
    // Id of the Visual Cluster
    private String clusterId = null;
    
    // Geo information 
    private Location location;
    
    // Size of the Media item
    private Integer width;

    private Integer height;
	
    public MediaItem() {
		
	}
	
    public MediaItem(URL url) {
        this.url = url.toString();
    }

    public MediaItem(URL url, WebPage page) {

        this.url = url.toString();

        this.source = "Web";
        this.id = "Web#" + url.hashCode();

        this.reference = page.getUrl();
        this.title = page.getTitle();

        this.publicationTime = page.getDate().getTime();
    }

    public MediaItem(URL url, MediaItem tempMediaItem) {

        this.id = tempMediaItem.getId();
        this.width = tempMediaItem.getWidth();
        this.height = tempMediaItem.getHeight();
        this.thumbnail = tempMediaItem.getThumbnail();
        this.type = tempMediaItem.getType();
        this.pageUrl = tempMediaItem.getPageUrl();

        this.url = url.toString();

        source = tempMediaItem.getSource();
        reference = tempMediaItem.getReference();

        description = tempMediaItem.getDescription();
        tags = tempMediaItem.getTags();
        title = tempMediaItem.getTitle();

        publicationTime = tempMediaItem.getPublicationTime();

        location = tempMediaItem.getLocation();

        mentions = tempMediaItem.getMentions();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void addLabels(List<String> labels) {
    	this.labels = new HashSet<String>();
        this.labels.addAll(labels);
    }

    public void addLabel(String label) {
    	labels = new HashSet<String>();
    	if(label != null) {
    		labels.add(label);
    	}
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public void setMentions(String[] mentions) {
        this.mentions = mentions;
    }

    public Long getLikes() {
        return likes;
    }

    public Long getShares() {
        return shares;
    }

    public Long getComments() {
        return comments;
    }

    public Float getRatings() {
        return ratings;
    }

    public Long getViews() {
        return views;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public void setShares(Long shares) {
        this.shares = shares;
    }

    public void setComments(Long comments) {
        this.comments = comments;
    }

    public void setRatings(Float ratings) {
        this.ratings = ratings;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public int getSentiment() {
        return sentiment;
    }

    public void setSentiment(int sentiment) {
        this.sentiment = sentiment;
    }

    public long getPublicationTime() {
        return publicationTime;
    }

    public void setPublicationTime(long publicationTime) {
        this.publicationTime = publicationTime;
    }

    public Double getLatitude() {
        if (location == null) {
            return null;
        }
        return location.getLatitude();
    }

    public Double getLongitude() {
        if (location == null) {
            return null;
        }
        return location.getLongitude();
    }

    public void setLatLong(Double latitude, Double longitude) {
        if (location == null) {
            location = new Location(latitude, longitude);
        } else {
            location.setLatitude(latitude);
            location.setLongitude(longitude);
        }
    }

    public Location getLocation() {
        return location;
    }

    public String getLocationName() {
        if (location == null) {
            return null;
        }
        return location.getName();
    }

    public void setLocationName(String locationName) {
        if (location == null) {
            location = new Location(locationName);
        } else {
            location.setName(locationName);
        }
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String[] getMentions() {
        return this.mentions;
    }

    public String getUserId() {
        return uid;
    }

    public void setUserId(String uid) {
        this.uid = uid;
    }

    public StreamUser getUser() {
        return streamUser;
    }

    public void setUser(StreamUser streamUser) {
        this.streamUser = streamUser;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public List<Concept> getConcepts() {
        return concepts;
    }

    public void setConcepts(List<Concept> concepts) {
        this.concepts = concepts;
    }

    public void addConcept(Concept concept) {
    	if(concepts == null)
    		concepts = new ArrayList<Concept>();
    	concepts.add(concept);
    }
    
    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    
}
