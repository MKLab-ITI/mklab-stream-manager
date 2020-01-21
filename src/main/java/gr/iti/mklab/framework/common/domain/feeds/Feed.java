package gr.iti.mklab.framework.common.domain.feeds;


import org.mongodb.morphia.annotations.Entity;

import gr.iti.mklab.framework.common.domain.JSONable;


@Entity(value="feeds", noClassnameStored=false)
public class Feed extends JSONable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8972611573430977057L;

	protected long since;

	protected long until = 0L;
	
	protected String source = null;
	
	protected String label;
	
	public Feed() {
		
	}
	
	public Feed(String id, long since, String source) {
		this.id = id;
		this.since = since;
		this.source = source;
	}
	
	public  String getId() {
		return id;
	}
	
	public  void setId(String id) {
		this.id = id;
	}
	
	public  String getLabel() {
		return label;
	}
	
	public  void setLabel(String label) {
		this.label = label;
	}
	
	public long getSinceDate() {
		return since;
	}
	
	public void setSinceDate(long since) {
		this.since = since;
	}
	
	public long getUntilDate() {
		return until;
	}
	
	public void setUntilDate(long until) {
		this.until = until;
	}
	
	public  String getSource() {
		return source;
	}
	
	public  void setSource(String source) {
		this.source = source;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {

		if(this == obj) {
			return true;
		}
		
        return ((Feed) obj).id.equals(id);
    }
}
