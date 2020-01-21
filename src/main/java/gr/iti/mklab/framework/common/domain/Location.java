package gr.iti.mklab.framework.common.domain;

import java.io.Serializable;
import java.util.List;

import org.mongodb.morphia.annotations.Entity;

@Entity(noClassnameStored = true)
public class Location extends JSONable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -333964331364087658L;

	protected Coordinates center = null;
	
	protected Double radius = null;
	
	protected String name = null;
	
	protected String city = null;
	
	protected String country = null;
	
	protected List<Coordinates> polygon = null;
	
	protected Boolean inferred = false;
	
	public Location() {
		
	}
	
	public Location(String name) {
		this.name = name;
	}
	
	public Location(Double latitude, Double longitude) {
		center = new Coordinates();
		center.latitude = latitude;
		center.longitude = longitude;
	}
	
	public Location(Double latitude, Double longitude, Double radius) {
		center = new Coordinates();
		center.latitude = latitude;
		center.longitude = longitude;
		this.radius = radius;
	}
	
	public Location(Double latitude, Double longitude, String name) {
		center = new Coordinates(latitude, longitude);
		this.name = name;
	}
	
	public Location(Double[][] bbox, String name) {
		if(bbox.length != 2 && bbox[0].length != 2){
			return;
		}
		this.name = name;
	}
	
	public void setLatitude(Double latitude) {
		if(center == null) {
			center = new Coordinates();
		}
		
		center.latitude = latitude;
	}
	
	public void setLongitude(Double longitude) {
		if(center == null) {
			center = new Coordinates();
		}
		
		center.longitude = longitude;
	}
	
	public void setRadius(Double radius) {
		this.radius = radius;
	}
	
	public  void setName(String name) {
		this.name = name;
	}

	public  void setCityName(String city) {
		this.city = city;
	}
	
	public  void setCountryName(String country) {
		this.country = country;
	}
	
	public Double getLatitude() {
		if(center == null) {
			return null;
		}
		
		return center.latitude;
	}
	
	public Double getLongitude() {
		if(center == null) {
			return null;
		}
		
		return center.longitude;
	}
	
	public Double getRadius() {
		return radius;
	}
	
	public List<Coordinates> getPolygon() {
		return polygon;
	}
	
	public  String getName(){
		return name;
	}

	public  String getCityName(){
		return city;
	}
	
	public  String getCountryName(){
		return country;
	}
	
	@Entity(noClassnameStored = true)
	public static class Coordinates implements Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 4394908733076702633L;
		
		public Coordinates() {
			
		}
		
		public Coordinates(Double latitude, Double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}

		protected Double latitude = null;

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

		protected Double longitude = null;
	}
	

}
