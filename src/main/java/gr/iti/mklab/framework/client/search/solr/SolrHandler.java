package gr.iti.mklab.framework.client.search.solr;

import gr.iti.mklab.framework.client.search.Bucket;
import gr.iti.mklab.framework.client.search.Facet;
import gr.iti.mklab.framework.client.search.SearchResponse;
import gr.iti.mklab.framework.client.search.solr.beans.Bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

public abstract class SolrHandler<K extends Bean> {
    
	protected Logger logger;
	
	protected SolrClient client;
    
	protected int commitWithinMs = 0;
	
    public boolean insert(K bean) {
        boolean status = true;
        try {
        	if(commitWithinMs > 0) {
        		client.addBean(bean, commitWithinMs);
        	}
        	else {
        		client.addBean(bean);
        	}
        } 
        catch (SolrServerException ex) {
            logger.error(ex);
            status = false;
        } catch (Exception ex) {
        	logger.error(ex);
            status = false;
        } 
       
        return status;
    }

    public boolean insertWithUpdateChain(K bean) {
        boolean status = true;
        try {
        	DocumentObjectBinder binder = client.getBinder();
        	UpdateRequest request = new UpdateRequest();
        	
        	SolrInputDocument doc = binder.toSolrInputDocument(bean);
        	request.add(doc);
        	if(commitWithinMs > 0) {
        		request.setCommitWithin(commitWithinMs);
        	}
        	request.setParam("update.chain", "langid");
        	
        	request.process(client);
        } 
        catch (SolrServerException ex) {
            logger.error(ex);
            status = false;
        } catch (Exception ex) {
        	logger.error(ex);
            status = false;
        } 
        return status;
    }
    
    public boolean insert(List<K> beans) {
        boolean status = true;
        try {
        	client.addBeans(beans);
        	client.commit();
        } catch (SolrServerException ex) {
            logger.error(ex.getMessage());
            status = false;
        } catch (IOException ex) {
            logger.error(ex);
            status = false;
        }
        return status;
    }
	
    public boolean insertWithUpdateChain(List<K> beans) {
        boolean status = true;
        try {
        	DocumentObjectBinder binder = client.getBinder();
        	UpdateRequest request = new UpdateRequest();
        	
        	List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
        	for(Bean bean : beans) {
        		SolrInputDocument doc = binder.toSolrInputDocument(bean);	
        		docs.add(doc);
        	}
        	
        	request.add(docs);
        	request.setParam("update.chain", "langid");
        	
        	request.process(client);
        } 
        catch (SolrServerException ex) {
            logger.error(ex);
            status = false;
        } catch (Exception ex) {
        	logger.error(ex);
            status = false;
        } 
        return status;
    }
    
    public boolean deleteById(String itemId) {
        boolean status = false;
        try {
        	UpdateResponse response = client.deleteByQuery("id:" + itemId);
            response.getStatus();
            status = true; 
        } catch (SolrServerException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }
        
        return status;
    }
	
    public boolean delete(String query) {
        boolean status = false;
        try {
        	UpdateResponse response = client.deleteByQuery(query);
            response.getStatus();
            status = true;
        } catch (SolrServerException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }
        return status;
    }
	
    public long count(String query) {
    	long numFound = 0;
        try {
        	SolrQuery solrQuery = new SolrQuery(query);
        	QueryResponse rsp = client.query(solrQuery);
           
        	numFound = rsp.getResults().getNumFound();
        } catch (SolrServerException e) {
            logger.error(e);
        } catch (IOException e) {
        	logger.error(e);
		}    
        return numFound;
    }
    
	public abstract SearchResponse<K> find(SolrQuery query);
    
	public List<Facet> getFacets(QueryResponse rsp) {
		
		List<Facet> facets = new ArrayList<Facet>();
		List<FacetField> facetFields = rsp.getFacetFields();
        if (facetFields != null) {
            for (FacetField solrFacet : facetFields) {
            	
                Facet facet = new Facet(); 
                facet.setName(solrFacet.getName());
                
                List<Bucket> buckets = new ArrayList<Bucket>();
                List<FacetField.Count> values = solrFacet.getValues();
                if(!values.isEmpty()) {
                	//populate Valid Facets
                	for (FacetField.Count facetCount : values) {
                    	Bucket bucket = new Bucket();
                   
                    	bucket.setCount(facetCount.getCount());
                    	bucket.setName(facetCount.getName());
                    	bucket.setQuery(facetCount.getAsFilterQuery());
                    	
                    	buckets.add(bucket);
                	}
                	facet.setBuckets(buckets);
                	facets.add(facet);
                }
            }

            // Sort
            Collections.sort(facets, new Comparator<Facet>() {
                @Override
                public int compare(Facet f1, Facet f2) {

                    String value1 = f1.getName();
                    String value2 = f2.getName();

                    if (value1.compareTo(value2) > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
        }
    
        return facets;
	}
	
	public void close() throws IOException {
		try {
			client.commit();
			client.optimize();
			client.close();
		} catch (SolrServerException e) {
			logger.error(e.getMessage());
			
			throw new IOException(e);
			
		}
	}
	
	public void commit() {
		try {
			client.commit();
			//client.optimize(); // is optimization a good practice?
		} catch (SolrServerException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		
	}

	public int getCommitWithinMs() {
		return commitWithinMs;
	}

	public void setCommitWithinMs(int commitWithinMs) {
		this.commitWithinMs = commitWithinMs;
	}
}
