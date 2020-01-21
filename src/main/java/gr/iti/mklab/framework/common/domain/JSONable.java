package gr.iti.mklab.framework.common.domain;

import java.io.Serializable;

import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;


/**
 * 
 * @author manosetro - manosetro@iti.gr
 */

public class JSONable implements Serializable, Comparable<JSONable> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3056665779688434226L;
	
	private static Morphia morphia = new Morphia();

    // Unique id of an instance with the following structure: StreamName#internalId
    @Id
    @Property("id")
    protected String id;
    
	protected Double score;
	
	public double getScore() {
		return score;
	}
	
	public void setScore(Double score) {
		this.score = score;
	}
	
	@Override
	public String toString() {
		synchronized(morphia) {
			morphia.map(JSONable.class);
			DBObject obj = morphia.toDBObject(this);
			return obj.toString();
		}
	}
    
	public DBObject toDBObject() {
		synchronized(morphia) {
			morphia.map(JSONable.class);
			DBObject obj = morphia.toDBObject(this);
			return obj;
		}
	}
	
	public static <K> K toObject(String json, Class<K> clazz) {
		synchronized(morphia) {
			morphia.map(clazz);
			DBObject obj = (DBObject) JSON.parse(json);
			return morphia.fromDBObject(clazz, obj);
		}
	}

	public static <K> K toObject(DBObject obj, Class<K> clazz) {
		synchronized(morphia) {
			morphia.map(clazz);
			return morphia.fromDBObject(clazz, obj);
		}
	}
	
	@Override
	public int compareTo(JSONable other) {
		if(this.score > other.score) {
			return 1;
		}
		else {
			return -1;
		}
	}
	
	public static void mapClass(Class<?> c) {
		morphia.map(c);
	}
}
