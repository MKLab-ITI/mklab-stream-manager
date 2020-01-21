package gr.iti.mklab.framework.retrievers.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import gr.iti.mklab.framework.Credentials;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.MediaItem;
import gr.iti.mklab.framework.common.domain.StreamUser;
import gr.iti.mklab.framework.common.domain.feeds.AccountFeed;
import gr.iti.mklab.framework.common.domain.feeds.Feed;
import gr.iti.mklab.framework.common.domain.feeds.GroupFeed;
import gr.iti.mklab.framework.common.domain.feeds.KeywordsFeed;
import gr.iti.mklab.framework.common.domain.feeds.LocationFeed;
import gr.iti.mklab.framework.common.domain.feeds.URLFeed;
import gr.iti.mklab.framework.retrievers.Response;
import gr.iti.mklab.framework.retrievers.Retriever;

/**
 * Class for retrieving RSS feeds using Rome API.
 *  
 * @author Manos Schinas - manosetro@iti.gr
 */
public class URLRetriever extends Retriever {
	
	public URLRetriever(Credentials credentials) {
		super(credentials);
		
	}

	public final Logger logger = LogManager.getLogger(URLRetriever.class);
	
	@Override
	public Response retrieve(Feed feed) throws Exception {
		return retrieve(feed, 1);
	}
		
	@Override
	public Response retrieve(Feed feed, Integer maxRequests) throws Exception {

		Response response = new Response();
		List<Item> items = new ArrayList<Item>();
		
		if(URLFeed.class.isInstance(feed.getClass())) {
			throw new Exception("Feed " + feed.getClass() + "is not instance of URLFeed");
		}
		
		URLFeed urlFeed = (URLFeed) feed;
		logger.info("["+new Date()+"] Retrieving RSS Feed: " + urlFeed.getURL());
		
		if(urlFeed.getURL().equals("")) {
			logger.error("URL is null");
			response.setItems(items);
			return response;
		}
		
		//Date since = new Date(urlFeed.getSinceDate());
		try {
			URL url = new URL(urlFeed.getURL());
			
			String content = getContent(url);
			System.out.println(content);
			
			Document doc = Jsoup.parse(content);
			getArticles(doc);
			
		} catch (MalformedURLException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error(e);
		}
	
		response.setItems(items);
		return response;
	}

	private String getContent(URL url) throws IOException {
		StringBuffer content = new StringBuffer();
		
		URLConnection connection = url.openConnection();
		BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String inputLine;
		while ((inputLine = input.readLine()) != null) {
			content.append(inputLine);
		}
        input.close();
        
        return content.toString();
	}
	
	private List<Item> getArticles(Document doc) {
		List<Item> items = new ArrayList<Item>();
		
		Elements articleElements = doc.getElementsByTag("article");
		for(Element articleNode : articleElements) {
			articleNode.getElementById("");
		}
		
		return items;
	}

	@Override
	public Response retrieveKeywordsFeed(KeywordsFeed feed, Integer requests)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response retrieveAccountFeed(AccountFeed feed, Integer requests)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response retrieveLocationFeed(LocationFeed feed, Integer requests)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response retrieveGroupFeed(GroupFeed feed, Integer maxRequests) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StreamUser getStreamUser(String uid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MediaItem getMediaItem(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getItem(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Item> getItemComments(Item item, long since) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
