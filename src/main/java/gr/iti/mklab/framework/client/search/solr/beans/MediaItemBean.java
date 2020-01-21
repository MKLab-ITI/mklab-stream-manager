package gr.iti.mklab.framework.client.search.solr.beans;

import gr.iti.mklab.framework.common.domain.Concept;
import gr.iti.mklab.framework.common.domain.MediaItem;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.beans.Field;

/**
 *
 * @author 	Manos Schinas - manosetro@iti.gr
 * 
 */
public class MediaItemBean extends Bean {
 
	@Field(value = "url")
    private String url;
	
	@Field(value = "source")
    private String source;
    
	@Field(value = "title")
    private String title;
    
	@Field(value = "description")
    private String description;
    
	@Field(value = "tags")
    private String[] tags;
    
	@Field(value = "uid")
    private String uid;
	
	@Field(value = "publicationTime")
    private long publicationTime;
    
	@Field(value = "latitude")
    private Double latitude;
    
	@Field(value = "longitude")
    private Double longitude;
    
	@Field(value = "location")
    private String location;
    
	@Field(value = "concepts")
    private String[] concepts;
    
	@Field(value = "type")	// image/video
    private String type;
	
    @Field(value = "labels")
    private List<String> labels;
	
    public MediaItemBean() {
    	
    }

    public MediaItemBean(MediaItem mediaItem) {

        id = mediaItem.getId();
        source = mediaItem.getSource();
        
        title = mediaItem.getTitle();
        description = mediaItem.getDescription();
        tags = mediaItem.getTags();

        uid = mediaItem.getUserId();

        url = mediaItem.getUrl();
        publicationTime = mediaItem.getPublicationTime();

        latitude = mediaItem.getLatitude();
        longitude = mediaItem.getLongitude();
        location = mediaItem.getLocationName();

        List<Concept> miConcepts = mediaItem.getConcepts();
        if (miConcepts != null) {
            concepts = new String[miConcepts.size()];
            for (int i = 0; i < concepts.length; i++) {
                concepts[i] = miConcepts.get(i).getConcept();
            }
        }

        labels = new ArrayList<String>();
        if (mediaItem.getLabels() != null) {
        	labels.addAll(mediaItem.getLabels());
        }
        
        type = mediaItem.getType();

    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUserId() {
        return uid;
    }

    public void setUserId(String uid) {
        this.uid = uid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public List<String> getLabels() {
        return labels;
    }

    public void getLabels(List<String> labels) {
        this.labels = labels;
    }
}
