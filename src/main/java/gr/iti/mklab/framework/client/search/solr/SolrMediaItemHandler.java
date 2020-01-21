package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.client.search.Facet;
import gr.iti.mklab.framework.client.search.SearchResponse;
import gr.iti.mklab.framework.client.search.solr.beans.MediaItemBean;
import gr.iti.mklab.framework.common.domain.Account;
import gr.iti.mklab.framework.common.domain.collections.Collection;
import gr.iti.mklab.framework.common.domain.collections.Collection.Keyword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 *
 * @author	Manos Schinas - manosetro@iti.gr
 * 
 */
public class SolrMediaItemHandler extends SolrHandler<MediaItemBean> {
    
	private static Map<String, SolrMediaItemHandler> INSTANCES = new HashMap<String, SolrMediaItemHandler>();

    // Private constructor prevents instantiation from other classes
    private SolrMediaItemHandler(String collection) throws Exception {
    	try {
    		logger = LogManager.getLogger(SolrMediaItemHandler.class);
    		client = new HttpSolrClient.Builder(collection).build();
    		
    	} catch (Exception e) {
    		logger.info(e.getMessage());
    	}
    }

    // implementing Singleton pattern
    public static SolrMediaItemHandler getInstance(String collection) throws Exception {
        SolrMediaItemHandler INSTANCE = INSTANCES.get(collection);
        if (INSTANCE == null) {
            INSTANCE = new SolrMediaItemHandler(collection);
            
            INSTANCES.put(collection, INSTANCE);
        }
        
        return INSTANCE;
    }

    public SearchResponse<MediaItemBean> find(SolrQuery query) {
    	
    	SearchResponse<MediaItemBean> response = new SearchResponse<MediaItemBean>();
        QueryResponse rsp;
        try {
            rsp = client.query(query);
        } catch (SolrServerException e) {
            logger.info(e.getMessage());
            return null;
        } catch (IOException e) {
        	 logger.info(e.getMessage());
             return null;
		}
        response.setNumFound(rsp.getResults().getNumFound());
        
        List<MediaItemBean> solrMediaItems = rsp.getBeans(MediaItemBean.class);
        if (solrMediaItems != null) {
            logger.info("got: " + solrMediaItems.size() + " media items from Solr - total results: " + response.getNumFound());
        }
        response.setResults(solrMediaItems);
        
        List<Facet> facets = getFacets(rsp);
        response.setFacets(facets);

        return response;
    }
    
    public SearchResponse<MediaItemBean> findMediaItems(String textQuery, List<String> filters, 
    		List<String> facetFields, String orderBy, int size) {

        SearchResponse<MediaItemBean> response = new SearchResponse<MediaItemBean>();
        if (textQuery == null || textQuery.equals("")) {
            return response;
        }

        StringBuffer query = new StringBuffer();
        query.append("(title : (" + query + ")) OR (description:(" + query + "))");
        
        SolrQuery solrQuery = new SolrQuery(query.toString());
        solrQuery.setRows(size);

      //Set filters in case they exist exist
        if(filters != null && !filters.isEmpty()) {
        	String[] fq = filters.toArray(new String[filters.size()]);
        	solrQuery.setFilterQueries(fq);
        }
        
        if(facetFields != null && !facetFields.isEmpty()) {
        	for (String facetField : facetFields) {
            	solrQuery.addFacetField(facetField);
        	}
        	solrQuery.setFacetLimit(10);
        }

        if (orderBy != null) {
            solrQuery.setSort(orderBy, ORDER.desc);
        } else {
            solrQuery.setSort("score", ORDER.desc);
        }
        
        logger.info("Solr Query : " + query);

        response = find(solrQuery);
        return response;
    }

    public SearchResponse<MediaItemBean> findMediaItems(Collection collection, List<String> filters, List<String> facets, String orderBy, int size) {

        SearchResponse<MediaItemBean> response = new SearchResponse<MediaItemBean>();

        // Create Query
        List<String> queryParts = new ArrayList<String>();
        
        List<Keyword> keywords = collection.getKeywords();
        if(keywords != null && !keywords.isEmpty()) {
        String contentQuery = StringUtils.join(keywords, " OR ");
        	if (contentQuery != null && !contentQuery.isEmpty()) {
        		queryParts.add("(title : (" + contentQuery + ")");
        		queryParts.add("(description : (" + contentQuery + ")");
        	}
        }
        
        //set Users Query
        List<Account> accounts = collection.getAccounts();
        if (accounts != null && !accounts.isEmpty()) {
        	List<String> uids = new ArrayList<String>();
        	for(Account account : accounts) {
        		uids.add(account.getId());
        	}
        	
            String usersQuery = StringUtils.join(uids, " OR ");
            if (usersQuery != null && !usersQuery.isEmpty()) {
            	queryParts.add("uid : (" + usersQuery + ")");
            }
        }
        
        if (queryParts.isEmpty()) {
            return response;
        }
        
        String query = StringUtils.join(queryParts, " OR ");

        //add words to exclude in query
        List<String> keywordsToExclude = collection.getKeywordsToExclude();
        if (keywordsToExclude != null && !keywordsToExclude.isEmpty()) {
        	String exclude = StringUtils.join(keywordsToExclude, " OR ");
        	query += " NOT (title : (" + exclude + ") OR description:(" + exclude + "))";
        }
        
        //Set source filters in case they exist exist
        if(filters!=null && !filters.isEmpty()) {
        	String filtersQuery = StringUtils.join(filters, " AND ");
        	 if (query.isEmpty()) {
             	query = filtersQuery;
             } else {
             	query = "(" + query + ") AND " + filtersQuery;
             }
        }

        SolrQuery solrQuery = new SolrQuery(query);
        logger.info("Solr Query: " + query);

        solrQuery.setRows(size);

        for (String facet : facets) {
        	//solrQuery.addFacetQuery(query);
            solrQuery.addFacetField(facet);
            solrQuery.setFacetLimit(6);
        }

        if (orderBy != null) {
            solrQuery.setSort(orderBy, ORDER.desc);
        } else {
            solrQuery.setSort("score", ORDER.desc);
        }

        response = find(solrQuery);

        return response;
    }
    
   public static void main(String...args) throws Exception {
    	
    	String solrCollection = "http://xxx.xxx.xxx.xxx:8983/solr/StepMediaItems";
    	SolrHandler<MediaItemBean> solrHandler = SolrMediaItemHandler.getInstance(solrCollection);
    	//solrHandler.delete("*:*");
    	System.out.println("Count: " + solrHandler.count("*:*"));		
    }
   
}