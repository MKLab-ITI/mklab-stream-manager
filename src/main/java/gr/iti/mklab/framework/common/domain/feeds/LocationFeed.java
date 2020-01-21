package gr.iti.mklab.framework.common.domain.feeds;

import gr.iti.mklab.framework.common.domain.Location;

public class LocationFeed extends Feed {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6371051856398652350L;
	
	private Location location;
	
	public LocationFeed() {
		
	}
	
	public LocationFeed(String id, Location location, long since, String source) {
		super(id, since, source);
		
		this.location = location;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
}
