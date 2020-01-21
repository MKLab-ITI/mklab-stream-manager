package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.client.search.SearchResponse;
import gr.iti.mklab.framework.client.search.solr.beans.WebPageBean;
import gr.iti.mklab.framework.common.domain.collections.Collection;
import gr.iti.mklab.framework.common.domain.collections.Collection.Keyword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 *
 * @author Manos Schinas - manosetro@iti.gr
 * 
 */
public class SolrWebPageHandler extends SolrHandler<WebPageBean> {

	private static Map<String, SolrWebPageHandler> INSTANCES = new HashMap<String, SolrWebPageHandler>();
    
    // Private constructor prevents instantiation from other classes
    private SolrWebPageHandler(String collection) {
        try {
        	logger = LogManager.getLogger(SolrWebPageHandler.class);
            client = new HttpSolrClient.Builder(collection).build();
        } catch (Exception e) {
            
        }
    }

    // implementing Singleton pattern
    public static SolrWebPageHandler getInstance(String collection) {
    	SolrWebPageHandler INSTANCE = INSTANCES.get(collection);
        if (INSTANCE == null) {
            INSTANCE = new SolrWebPageHandler(collection);
            INSTANCES.put(collection, INSTANCE);
        }
        
        return INSTANCE;
    }

    public static SolrWebPageHandler getInstance(String service, String collection) {
    	if(service.endsWith("/")) {
    		return getInstance(service + collection);
    	}
    	else {
    		return getInstance(service + "/" + collection);
    	}
    }

    public SearchResponse<WebPageBean> find(SolrQuery query) {

    	SearchResponse<WebPageBean> response = new SearchResponse<WebPageBean>();
        try {
        	QueryResponse rsp = client.query(query);
        	
            List<WebPageBean> wpBeans = rsp.getBeans(WebPageBean.class);
            response.setResults(wpBeans);
            
        } catch (SolrServerException e) {
        	logger.info(e.getMessage());
        } catch (IOException e) {
        	logger.info(e.getMessage());
		}

        return response;
    }
    
    public List<String> findWebPages(String textQuery, List<String> filters, List<String> facetsFields, int size) {
    	  
        List<String> webPages = new ArrayList<String>();

        StringBuffer query = new StringBuffer();
        query.append("title : (" + textQuery + ") OR text:(" + textQuery + ")");
        
        
        SolrQuery solrQuery = new SolrQuery(query.toString());
        solrQuery.setRows(size);
        
        //Set source filters in case they exist exist
        if(filters != null && !filters.isEmpty()) {
        	String[] fq = filters.toArray(new String[filters.size()]);
        	solrQuery.addFilterQuery(fq);
        }
        
        solrQuery.addSort("score", ORDER.desc);
        solrQuery.addSort("date", ORDER.desc);

        //Set facets if necessary
        if(facetsFields != null && !facetsFields.isEmpty()) {
        	for (String facetField : facetsFields) {
            	solrQuery.addFacetField(facetField);
        	}
        	solrQuery.setFacetLimit(10);
        }
        
        logger.info("Query : " + solrQuery);
        
        SearchResponse<WebPageBean> response = find(solrQuery);
        if (response != null) {
            List<WebPageBean> results = response.getResults();
            for (WebPageBean webPage : results) {
            	webPages.add(webPage.getUrl());
            }
        }
        
        return webPages.subList(0, Math.min(webPages.size(), size));
    }
    
    public List<String> findWebPages(Collection collection, List<String> filters, List<String> facets, int size) {
  
        Set<String> urls = new HashSet<String>();

        String query = "";
        List<Keyword> keywords = collection.getKeywords();
        if(keywords != null && !keywords.isEmpty()) {
        	query = StringUtils.join(keywords, "OR");
        	query = "((title : (" + query + ")) OR (text:(" + query + "))";
        }
        
        //Set source filters in case they exist exist
        String filterQuery = StringUtils.join(filters, " AND");
        if(query.isEmpty()) {
        	query = filterQuery;
        }
        else {
        	query += " AND " + filterQuery;
        }
        
        //add words to exclude in query
        List<String> keywordsToExclude = collection.getKeywordsToExclude();
        if (keywordsToExclude != null && !keywordsToExclude.isEmpty()) {
        	String excludeQuery = StringUtils.join(keywordsToExclude, " OR ");
        	query += " NOT (title : (" + excludeQuery + ") OR description:(" + excludeQuery + "))";
        }
        
        SolrQuery solrQuery = new SolrQuery(query);
        solrQuery.setRows(size);
        
        solrQuery.addSort("score", ORDER.desc);
        solrQuery.addSort("date", ORDER.desc);

        
      //Set facets if necessary
        for (String facet : facets) {
            solrQuery.addFacetField(facet);
            solrQuery.setFacetLimit(6);
        }
        
        logger.info("Query : " + solrQuery);
        
        SearchResponse<WebPageBean> response = find(solrQuery);
        for(WebPageBean wpBean : response.getResults()) {
        	urls.add(wpBean.getUrl());
        }
        
        return new ArrayList<String>(urls);
    }
      
   public static void main(String...args) throws Exception {
    	
    	String solrCollection = "http://160.40.50.207:8983/solr/StepWebPages";
    	SolrWebPageHandler solrHandler = SolrWebPageHandler.getInstance(solrCollection);
    	solrHandler.delete("*:*");
    	System.out.println("Count: " + solrHandler.count("*:*"));
		
    }
}