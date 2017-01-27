package gr.iti.mklab.sfc.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryResults;

import gr.iti.mklab.framework.client.mongo.DAOFactory;
import gr.iti.mklab.framework.common.domain.collections.Collection;
import gr.iti.mklab.framework.common.domain.config.Configuration;
import gr.iti.mklab.framework.common.domain.feeds.Feed;

/**
 * The class responsible for the creation of input feeds from mongodb storage
 * 
 * @author manosetro - manosetro@iti.gr
 */
public class CollectionsManager {
	
	public final Logger logger = LogManager.getLogger(CollectionsManager.class);
	
	protected static final String SINCE = "since";
	
	protected static final String HOST = "mongo.host";
	protected static final String DB = "mongo.database";
	protected static final String USERNAME = "mongo.username";
	protected static final String PWD = "mongo.password";
	
	private String host = null;
	private String db = null;
	private String username = null;
	private String password = null;
	
	private BasicDAO<Collection, String> collectionsDao;
	
	public CollectionsManager(Configuration config) throws Exception {
		this.host = config.getParameter(HOST);
		this.db = config.getParameter(DB);
		
		Pattern pattern = Pattern.compile("(\\Q${\\E)(.*)(\\Q}\\E)");
		
		this.username = config.getParameter(USERNAME);
		Matcher matcher = pattern.matcher(username);
		if(username != null && matcher.find()) {
			username = matcher.group(2);
			username = System.getProperty(username);
		}
		
		this.password = config.getParameter(PWD);
		matcher = pattern.matcher(password);
		if(password != null && matcher.find()) {
			password = matcher.group(2);
			password = System.getProperty(password);
		}
		
		connect();
	}
	
	public void connect() {
		try {
			DAOFactory daoFactory = new DAOFactory();
			
			if(username != null && password != null) {
				collectionsDao = daoFactory.getDAO(host, db, Collection.class, username, password);
			}
			else {
				collectionsDao = daoFactory.getDAO(host, db, Collection.class);
			}
		}
		catch(Exception e) {
			logger.error(e);
		}	
	}
	
	public void checkStatus() {
		if(collectionsDao == null) {
			logger.error("Collections Dao has not been initialized. Try to reconnect.");
			connect();
		}
		else {
			try {
				long count = collectionsDao.count();
				logger.info("Collections Dao is working properly: " + count + " saved collections.");
			}
			catch(Exception e) {
				logger.error(e);
				connect();
			}
		}
	}
	
	public Collection getCollection(String id) {
		Query<Collection> query = collectionsDao.createQuery()
				.filter("_id", id);

		Collection collection = collectionsDao.findOne(query);
		return collection;
	}
	
	public Map<String, Collection> getCollections() {
		Map<String, Collection> collections = new HashMap<String, Collection>();
		QueryResults<Collection> result = collectionsDao.find();
		Iterator<Collection> it = result.iterator();
		while(it.hasNext()) {
			Collection collection = it.next();
			collections.put(collection.getId(), collection);
		}
		return collections;
	}
	
	public Map<String, Collection> getActiveCollections() {
		Map<String, Collection> collections = new HashMap<String, Collection>();
		
		Query<Collection> query = collectionsDao.createQuery()
				.filter("status", "running")
				.order("-updateDate");
		
		QueryResults<Collection> result = collectionsDao.find(query);
		Iterator<Collection> it = result.iterator();
		while(it.hasNext()) {
			Collection collection = it.next();
			collections.put(collection.getId(), collection);
		}
		return collections;
	}
	
	public Map<String, Set<Feed>> createFeedsPerSource() {
	
		Map<String, Set<Feed>> feedsPerSource = new HashMap<String, Set<Feed>>();
		Set<Feed> allFeeds = createFeeds();
		for(Feed feed : allFeeds) {
			String source = feed.getSource();
			Set<Feed> feeds = feedsPerSource.get(source);
			if(feeds == null) {
				feeds = new HashSet<Feed>();
				feedsPerSource.put(source, feeds);
			}	
			feeds.add(feed);
		}
		return feedsPerSource;
	}

	public Set<Feed> createFeeds() {
		Set<Feed> feedsSet = new HashSet<Feed>();
		try {
			QueryResults<Collection> result = collectionsDao.find();
			List<Collection> collections = result.asList();
			for(Collection collection : collections) {
				List<Feed> feeds = collection.getFeeds();
				feedsSet.addAll(feeds);
			}
		}
		catch(Exception e) {
			logger.error(e);
		}

		return feedsSet;
	}

}