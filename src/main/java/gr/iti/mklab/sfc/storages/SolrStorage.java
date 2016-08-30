package gr.iti.mklab.sfc.storages;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gr.iti.mklab.framework.client.search.solr.SolrItemHandler;
import gr.iti.mklab.framework.client.search.solr.SolrMediaItemHandler;
import gr.iti.mklab.framework.client.search.solr.SolrWebPageHandler;
import gr.iti.mklab.framework.client.search.solr.beans.ItemBean;
import gr.iti.mklab.framework.client.search.solr.beans.MediaItemBean;
import gr.iti.mklab.framework.client.search.solr.beans.WebPageBean;
import gr.iti.mklab.framework.common.domain.config.Configuration;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.ItemState;
import gr.iti.mklab.framework.common.domain.MediaItem;
import gr.iti.mklab.framework.common.domain.WebPage;

/**
 * Class for indexing items to solr
 * 
 * @author Manos Schinas - manosetro@iti.gr
 *
 */
public class SolrStorage implements Storage {

	private Logger logger = LogManager.getLogger(SolrStorage.class);
	
	private static final String HOSTNAME = "solr.hostname";
	private static final String SERVICE = "solr.service";
	
	private static final String ITEMS_COLLECTION = "solr.items.collection";
	private static final String MEDIAITEMS_COLLECTION = "solr.mediaitems.collection";
	private static final String WEBPAGES_COLLECTION = "solr.webpages.collection";
	
	private static final String ONLY_ORIGINAL = "solr.onlyOriginal";
	
	private String hostname, service;
	
	private String itemsCollection = null;
	private String mediaItemsCollection = null;
	private String webPagesCollection = null;
	
	private String storageName = "Solr";
	
	private SolrItemHandler solrItemHandler = null; 
	private SolrMediaItemHandler solrMediaHandler = null;
	private SolrWebPageHandler solrWebPageHandler = null;
	
	private Boolean onlyOriginal = true;
	
	private AtomicLong indexedItems = new AtomicLong(0l);
	
	public SolrStorage(Configuration config) throws IOException {
		this.hostname = config.getParameter(SolrStorage.HOSTNAME);
		this.service = config.getParameter(SolrStorage.SERVICE);
		this.itemsCollection = config.getParameter(SolrStorage.ITEMS_COLLECTION);
		this.mediaItemsCollection = config.getParameter(SolrStorage.MEDIAITEMS_COLLECTION);
		this.webPagesCollection = config.getParameter(SolrStorage.WEBPAGES_COLLECTION);
	
		this.onlyOriginal = Boolean.valueOf(config.getParameter(SolrStorage.ONLY_ORIGINAL));
	}
	
	@Override
	public boolean open() {
		try {
			if(itemsCollection != null) {
				solrItemHandler = SolrItemHandler.getInstance(hostname + "/" + service + "/" + itemsCollection);
			}
			
			if(mediaItemsCollection != null) {	
				solrMediaHandler = SolrMediaItemHandler.getInstance(hostname + "/" + service+"/" + mediaItemsCollection);
			}
			
			if(webPagesCollection != null) {	
				solrWebPageHandler = SolrWebPageHandler.getInstance(hostname + "/" + service + "/" + webPagesCollection);
			}
			
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
		return true;	
	}

	@Override
	public void store(Item item) throws IOException {

		// Index only original Items and MediaItems come from original Items
		if(!item.isOriginal() && onlyOriginal) {
			return;
		}
		
		if(solrItemHandler != null) {
			ItemBean itemBean = new ItemBean(item);
			boolean status = solrItemHandler.insertWithUpdateChain(itemBean);
			if(status) {
				indexedItems.incrementAndGet();
			}
		}
		
		if(solrMediaHandler != null) {
			for(MediaItem mediaItem : item.getMediaItems()) {
				MediaItemBean mediaItemBean = new MediaItemBean(mediaItem);
				solrMediaHandler.insert(mediaItemBean);
			}
		}
		
		if(solrWebPageHandler != null) {
			List<WebPage> webPages = item.getWebPages();
			if(webPages != null) {
				for(WebPage webPage : webPages) {
					WebPageBean webpageBean = new WebPageBean(webPage);
					solrWebPageHandler.insert(webpageBean);
				}
			}
		}
		
	}
	
	@Override
	public void store(ItemState itemState) {
		
	}
	
	
	@Override
	public boolean delete(String itemId) throws IOException {
		logger.info("Delete item with id " + itemId + " from Solr.");
		solrItemHandler.delete(itemId);
		return true;
	}
	
	@Override
	public boolean checkStatus() {
		logger.info(indexedItems.get() + " indexed items.");
		if(itemsCollection != null) {
			try {
				solrItemHandler.count("*:*");
				solrItemHandler.commit();
			} 
			catch (Exception e) {
				logger.error(e);
				return false;
			}
		}
		
		if(mediaItemsCollection != null) {
			try {
				solrMediaHandler.count("*:*");
				solrMediaHandler.commit();
			} 
			catch (Exception e) {
				logger.error(e);
				return false;
			}
		}

		if(webPagesCollection != null) {
			try {
				solrWebPageHandler.count("*:*");
				solrMediaHandler.commit();
			} 
			catch (Exception e) {
				logger.error(e);
				return false;
			}
		}
		return true;
	}
	

	@Override
	public void close() {
		if(solrItemHandler != null) {
			try {
				solrItemHandler.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
		
		if(solrMediaHandler != null) {
			try {
				solrMediaHandler.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
		
		if(solrWebPageHandler != null) {
			try {
				solrWebPageHandler.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
		
	}
	
	@Override
	public String getStorageName() {
		return this.storageName;
	}
	
	
}
