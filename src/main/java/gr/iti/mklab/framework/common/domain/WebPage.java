package gr.iti.mklab.framework.common.domain;

import java.util.Date;
import java.util.List;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

/**
 *
 * @author 	Manos Schinas - manosetro@iti.gr
 */

@Entity(noClassnameStored = true)
@Indexes(@Index("url"))
public class WebPage extends JSONable {

    /**
     *
     */
    private static final long serialVersionUID = -8783341713025378581L;
    
	public WebPage() {
		
	}
			
	public WebPage(String url, String reference) {
        this.id = url;
    	this.url = url;
        this.reference = reference;
    }
	
    // The URL of a WebPage. This is usually a short URL
    private String url;

    // The expanded version of URL
    private String expandedUrl;

    // The expanded version of URL
    private String domain;

    // The title extracted from the WebPage
    private String title;

    // The textual content of the web page
    private String text;

    // The hash code generated from the content of the web page. (used for de-duplication)
    private String hash;
    
    // A flag that indicates whether this web page contains an article
    private boolean article;

    // Number of media contained in this web page
    private int media = 0;

    // Number of media contained in this web page
    private String[] mediaIds;

    // A representative mediaThumbnail
    private String mediaThumbnail;

    // The title extracted from the WebPage
    private String[] keywords;

    // The date that a web page shared for the first time
    private Date date;

    // The date that a web page shared for the first time
    private Long publicationTime;
    
    // The id of the Item that share a web page for the first time
    private String reference;

    // The stream of the Item that the web page comes from 
    private String source;

    // A list of URLs contained in the WebPage
    private String[] links;
    
    // The number of times a web page has been shared
    private int shares = 0;

    // A list of named entities extracted from a web page
    @Embedded
    protected List<NamedEntity> entities;
    
    public List<NamedEntity> getEntities() {
		return entities;
	}

	public void setEntities(List<NamedEntity> entities) {
		this.entities = entities;
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

    public String[] getLinks() {
		return links;
	}

	public void setLinks(String[] links) {
		this.links = links;
	}

	public void setUrl(String url) {
        this.url = url;
    }

    public String getExpandedUrl() {
        return this.expandedUrl;
    }

    public void setExpandedUrl(String expandedUrl) {
        this.expandedUrl = expandedUrl;
    }

    public String getDomain() {
        return this.domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean isArticle() {
        return article;
    }

    public void setArticle(Boolean article) {
        this.article = article;
    }

    public int getMedia() {
        return media;
    }

    public void setMedia(int media) {
        this.media = media;
    }

    public String[] getMediaIds() {
        return mediaIds;
    }

    public void setMediaIds(String[] mediaIds) {
        this.mediaIds = mediaIds;
    }

    public String getMediaThumbnail() {
        return mediaThumbnail;
    }

    public void setMediaThumbnail(String mediaThumbnail) {
        this.mediaThumbnail = mediaThumbnail;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public int getShares() {
        return shares;
    }

    public void setShares(int shares) {
        this.shares = shares;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

	public Long getPublicationTime() {
		return publicationTime;
	}

	public void setPublicationTime(Long publicationTime) {
		this.publicationTime = publicationTime;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

}
