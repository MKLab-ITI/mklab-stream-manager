package gr.iti.mklab.framework.common.domain;

import org.mongodb.morphia.annotations.Entity;

@Entity(noClassnameStored = true)
public class MediaShare extends JSONable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7926531925761955502L;


	public MediaShare(String id, String reference, long publicationTime, String userid) {
		this.id = id;
		this.reference = reference;
		this.publicationTime = publicationTime;
		this.userid = userid;
	}
	
	// Unique id of a Media
	private String id;
		
	private String reference;
	
	private long publicationTime = 0;
	
	private String userid;
	
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public long getPublicationTime() {
        return publicationTime;
    }
    
    public void setPublicationTime(long publicationTime) {
        this.publicationTime = publicationTime;
    }
    
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
	
}
