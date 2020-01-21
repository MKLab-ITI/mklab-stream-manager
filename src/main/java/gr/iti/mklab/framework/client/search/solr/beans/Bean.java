package gr.iti.mklab.framework.client.search.solr.beans;

import org.apache.solr.client.solrj.beans.Field;

public class Bean {

	@Field(value = "id")
    protected String id;
	
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
}
