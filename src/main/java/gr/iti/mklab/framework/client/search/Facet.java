package gr.iti.mklab.framework.client.search;

import gr.iti.mklab.framework.common.domain.JSONable;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Manos Schinas - manosetro@iti.gr
 */
public class Facet extends JSONable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7222978342717761726L;
	
	String name;
    List<Bucket> buckets = new ArrayList<Bucket>();

    public List<Bucket> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<Bucket> buckets) {
        this.buckets = buckets;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    

}
