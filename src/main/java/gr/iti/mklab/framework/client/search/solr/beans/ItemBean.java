package gr.iti.mklab.framework.client.search.solr.beans;

import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.Location;
import gr.iti.mklab.framework.common.domain.NamedEntity;
import gr.iti.mklab.framework.common.domain.StreamUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.solr.client.solrj.beans.Field;

/**
 *
 * @author 	Manos Schinas - manosetro@iti.gr
 * 
 */
public class ItemBean extends Bean {
    
    @Field(value = "source")
    private String source;
    
    @Field(value = "title")
    private String title;
    
    @Field(value = "cleanTitle")
    private String cleanTitle;
    
    @Field(value = "description")
    private String description;
    
    @Field(value = "tags")
    private String[] tags;
    
    @Field(value = "text")
    private String text;
    
    @Field(value = "minhash")
    private String minhash;
    
    @Field(value = "signature")
    private String signature;
    
    @Field(value = "uid")
    private String uid;
    
    @Field(value = "username")
    private String username;
    
    @Field(value = "reference")
    private String reference;
    
    @Field(value = "publicationTime")
    private long publicationTime;
    
    @Field(value = "latitude")
    private Double latitude;
    
    @Field(value = "longitude")
    private Double longitude;
    
    @Field(value = "latlon")
	private String latlon;
	
    @Field(value = "latlonRPT")
	private String latlonRPT;
    
    @Field(value = "location")
    private String location;
    
    @Field(value = "city")
    private String city;
    
    @Field(value = "country")
    private String country;
    
    @Field(value = "language")
    private String language;
    
    @Field(value = "labels")
    private List<String> labels;

    @Field(value = "persons")
    private List<String> persons = new ArrayList<String>();
    
    @Field(value = "locations")
    private List<String> locations = new ArrayList<String>();
    
    @Field(value = "organizations")
    private List<String> organizations = new ArrayList<String>();
    
    @Field(value = "mediaIds")
    private List<String> mediaIds;

    @Field(value = "mentions")
	private List<String> mentions;

	@Field(value = "referenceUserId")
	private String referenceUserId;
    
	@Field(value = "likes")
    protected Long likes = 0L;
    
	@Field(value = "shares")
    protected Long shares = 0L;
    
	@Field(value = "comments")
    protected Long comments = 0L;
    
	@Field(value = "followers")
    protected Long followers = 0L;
	
	@Field(value = "friends")
    protected Long friends = 0L;
	
	@Field(value = "original")
    protected Boolean original = true;
    
	public ItemBean() {
	}
	
	public ItemBean(Item item) {
        id = item.getId();
        source = item.getSource();
        
        title = item.getTitle();
        description = item.getDescription();
        tags = item.getTags();
        
        cleanTitle = item.getCleanTitle();
        
        text = item.getText();
        
        minhash = item.getMinhash();
        signature = item.getSignature();
        
        uid = item.getUserId();
        reference = item.getReference();
        referenceUserId = item.getReferencedUserId();
        
        publicationTime = item.getPublicationTime();

        latitude = item.getLatitude();
        longitude = item.getLongitude();
        
        if(latitude != null && longitude != null) {
        	latlon = latitude + "," + longitude;
        	latlonRPT = longitude + " " + latitude;
        }
        
        
        Location itemLocation = item.getLocation();
        if(itemLocation != null) {
        	location = itemLocation.getName();
        	city = itemLocation.getCityName();
        	country = itemLocation.getCountryName();
        }
        
        language = item.getLanguage();

        labels = new ArrayList<String>();
        if (item.getLabels() != null) {
        	labels.addAll(item.getLabels());
        }
        
        StreamUser user = item.getStreamUser();
        if(user != null) {
        	username = user.getUsername();
        	followers = user.getFollowers();
        	friends = user.getFriends();
        }
        
        if(item.getEntities() != null) {
        	for(NamedEntity entity : item.getEntities()) {
        		if(entity.getType().equals(NamedEntity.Type.PERSON)) {
        			persons.add(entity.getName());
        		}
        		if(entity.getType().equals(NamedEntity.Type.LOCATION)) {
        			locations.add(entity.getName());
        		}
        		if(entity.getType().equals(NamedEntity.Type.ORGANIZATION)) {
        			organizations.add(entity.getName());
        		}
        	}
        }
        
        // list of media ids, used as facet to find most shared media items
        mediaIds = item.getMediaIds();
        
        // list of mentions, used as facet to find most mentioned users
        mentions = new ArrayList<String>();
        if(item.getMentions() != null) {
        	mentions.addAll(Arrays.asList(item.getMentions()));
        }
        
        likes = item.getLikes();
        comments = item.getComments();
        shares = item.getShares();
        
        original = item.isOriginal();
        
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCleanTitle() {
        return cleanTitle;
    }

    public void setCleanTitle(String cleanTitle) {
        this.cleanTitle = cleanTitle;
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
    
    public String getUserId() {
        return uid;
    }

    public void setUserId(String uid) {
        this.uid = uid;
    }

    public Long getPublicationTime() {
        return publicationTime;
    }

    public void setPublicationTime(Long publicationTime) {
        this.publicationTime = publicationTime;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLatLon(Double latitude, Double longitude) {
    	this.latitude = latitude;
        this.longitude = longitude;
        
        this.latlon = latitude + "," + longitude;
    }
    
    public String getLatLon() {
        return latlon;
    }

    public void setLatLon(String latlon) {
        this.latlon = latlon;
    }
    
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void getLabels(List<String> labels) {
        this.labels = labels;
    }

	public String getReferenceUserId() {
		return referenceUserId;
	}

	public void setReferenceUserId(String referenceUserId) {
		this.referenceUserId = referenceUserId;
	}

    public Boolean isOriginal() {
		return original;
	}

	public void setOriginal(Boolean original) {
		this.original = original;
	}

	public String getMinhash() {
		return minhash;
	}

	public void setMinhash(String minhash) {
		this.minhash = minhash;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
}
