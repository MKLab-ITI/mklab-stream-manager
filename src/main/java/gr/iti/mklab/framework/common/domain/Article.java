package gr.iti.mklab.framework.common.domain;

import java.util.ArrayList;
import java.util.List;

import gr.iti.mklab.framework.common.domain.MediaItem;

public class Article extends JSONable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7880711718654107043L;
	
	private String title = null;
	private String text = null;
	private String[] tags = null;
	
	private boolean isLowQuality = true;
	
	private List<MediaItem> mediaItems = new ArrayList<MediaItem>();
	
	public Article() {
		
	}
	
	public Article(String title, String text) {
		this.title = title;
		this.text = text;
	}
	
	public void addMediaItem(MediaItem mediaItem) {
		this.mediaItems.add(mediaItem);
	}
	
	public String getTitle() {
		return title;
	}

	public String getText() {
		return text;
	}
	
	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}
	
	public List<MediaItem> getMediaItems() {
		return mediaItems; 
	}

	public boolean isLowQuality() {
		return this.isLowQuality;
	}
	
	public void setLowQuality(boolean isLowQuality) {
		this.isLowQuality = isLowQuality;
	}
	
	public String toString() {
		return title;
	}
	
}
