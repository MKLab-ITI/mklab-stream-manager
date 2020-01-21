package gr.iti.mklab.framework.common.domain.feeds;

import gr.iti.mklab.framework.common.domain.Source;


public class RssFeed extends Feed {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6084756784860676248L;

	private String url = null;
	private String name = null;
	
	public RssFeed() {
		
	}
	
	public RssFeed(String id, String url, long since) {
		super(id, since, Source.RSS.toString());
		
		this.url = url;
	}

	public String getURL() {
		return this.url;
	}
	
	public void setURL(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
