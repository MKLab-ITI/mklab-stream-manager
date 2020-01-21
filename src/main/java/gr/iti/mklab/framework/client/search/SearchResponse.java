package gr.iti.mklab.framework.client.search;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author	manosetro - manosetro@iti.gr
 */
public class SearchResponse<T> {
    
	private List<T> results = new ArrayList<T>();
	
	private List<Facet> facets = new ArrayList<Facet>();
	private long numFound = 0;
    
    public SearchResponse() {
    	
    }

    public List<T> getResults() {
    	return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public List<Facet> getFacets() {
    	return facets;
    }

    public void setFacets(List<Facet> facets) {
    	this.facets = facets;
    }

    public long getNumFound() {
    	return numFound;
    }

    public void setNumFound(long numFound) {
    	this.numFound = numFound;
    }    

}
