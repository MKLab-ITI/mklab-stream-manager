package gr.iti.mklab.framework.client.search.solr.beans;

import gr.iti.mklab.framework.common.domain.NamedEntity;
import gr.iti.mklab.framework.common.domain.WebPage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.beans.Field;

/**
 * @author 	Manos Schinas - manosetro@iti.gr
 * 
 */
public class WebPageBean extends Bean {
	
	@Field(value = "url")
	private String url;

	@Field(value = "expandedUrl")
	private String expandedUrl;
	
	@Field(value = "domain")
	private String domain;
	
	@Field(value = "title")
	private String title;
	
	@Field(value = "text")
	private String text;
	
	@Field(value = "keywords")
	private String[] keywords;
	
	@Field(value = "date")
	private Date date;
	
    @Field(value = "publicationTime")
	private Long publicationTime;
    
    @Field(value = "persons")
    private List<String> persons = new ArrayList<String>();
    
    @Field(value = "locations")
    private List<String> locations = new ArrayList<String>();
    
    @Field(value = "organizations")
    private List<String> organizations = new ArrayList<String>();

    @Field(value = "links")
    private List<String> links = new ArrayList<String>();
    
    @Field(value = "hash")
	private String hash;
    
	public WebPageBean() {
		
	}

	public WebPageBean(WebPage webPage) {
		id = webPage.getUrl();
		
        url = webPage.getUrl();
        expandedUrl = webPage.getExpandedUrl();
        domain = webPage.getDomain();
        
        title = webPage.getTitle();
        text = webPage.getText();
        
        date = webPage.getDate();     
        publicationTime = webPage.getPublicationTime();
        
        keywords = webPage.getKeywords();
        
        setHash(webPage.getHash());
        if(webPage.getEntities() != null) {
        	for(NamedEntity entity : webPage.getEntities()) {
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
        
        if(webPage.getLinks() != null) {
        	for(String link : webPage.getLinks()) {
        		links.add(link);
        	}
        }
        
    }
	
    public String getUrl() {
    	return url;
	}
    
    public String getExpandedUrl() {
    	return expandedUrl;
	}
    
    public String getDomain() {
    	return domain;
	}
    
    public String getTitle() {
    	return title;
	}
    
    public Date getDate() {
    	return date;
	}
	
    public String[] getKeywords() {
    	return keywords;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
}
