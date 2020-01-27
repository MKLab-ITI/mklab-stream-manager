package gr.iti.mklab.framework.retrievers.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;

import com.restfb.util.StringUtils;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import gr.iti.mklab.framework.Credentials;
import gr.iti.mklab.framework.abstractions.socialmedia.items.RSSItem;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.MediaItem;
import gr.iti.mklab.framework.common.domain.StreamUser;
import gr.iti.mklab.framework.common.domain.feeds.AccountFeed;
import gr.iti.mklab.framework.common.domain.feeds.Feed;
import gr.iti.mklab.framework.common.domain.feeds.GroupFeed;
import gr.iti.mklab.framework.common.domain.feeds.KeywordsFeed;
import gr.iti.mklab.framework.common.domain.feeds.LocationFeed;
import gr.iti.mklab.framework.common.domain.feeds.RssFeed;
import gr.iti.mklab.framework.retrievers.Response;
import gr.iti.mklab.framework.retrievers.Retriever;

/**
 * Class for retrieving RSS feeds using Rome API.
 *  
 * @author Manos Schinas - manosetro@iti.gr
 * 
 */
public class RssRetriever extends Retriever {
	
	public RssRetriever(Credentials credentials) {
		super(credentials);	
	}

	public final Logger logger = LogManager.getLogger(RssRetriever.class);
	
	private HttpClient httpClient = HttpClientBuilder.create().build();
	 
	@Override
	public Response retrieve(Feed feed) throws Exception {
		return retrieve(feed, 1);
	}
		
	@Override
	public Response retrieve(Feed feed, Integer maxRequests) throws Exception {

		Response response = new Response();
		List<Item> items = new ArrayList<Item>();
		
		if(RssFeed.class.isInstance(feed.getClass())) {
			logger.error("Feed " + feed.getClass() + "is not instance of Rss Feed");
			throw new Exception("Feed " + feed.getClass() + "is not instance of Rss Feed");
		}
		
		RssFeed rssFeed = (RssFeed) feed;
		logger.info("["+new Date()+"] Retrieving RSS Feed: " + rssFeed.getURL());
		
		if(rssFeed.getURL().equals("")) {
			logger.error("URL is null");
			response.setItems(items);
			return response;
		}
		
		StreamUser user = new StreamUser();
		user.setId(rssFeed.getSource() + "#" + rssFeed.getId());
		user.setUsername(rssFeed.getURL());
		user.setName(rssFeed.getName());
		user.setSource(rssFeed.getSource());
		user.setProfileImage("imgs/noprofile.gif");
		
		Date since = new Date(rssFeed.getSinceDate());
		try {
		
			SyndFeed syndFeed = null;
			synchronized(httpClient) {
				HttpGet r = new HttpGet(rssFeed.getURL());
				r.addHeader("accept", "application/xml");
				
				HttpResponse xmlResp = httpClient.execute(r);
				int statusCode = xmlResp.getStatusLine().getStatusCode();
		        if (statusCode != 200)  {
		            throw new RuntimeException("Failed with HTTP error code : " + statusCode);
		        }
		        HttpEntity httpEntity = xmlResp.getEntity();
				SyndFeedInput input = new SyndFeedInput();	    
				syndFeed = input.build(new XmlReader(httpEntity.getContent()));
			}
			
			List<SyndEntry> entries = syndFeed.getEntries();
			for (SyndEntry entry : entries) {		
				if(entry.getLink() != null) {
					
					Date publicationDate = entry.getPublishedDate();
					if(publicationDate == null) {
						continue;
					}
					
					if(publicationDate.before(since)) {
						logger.info(publicationDate + " before " + since);
						break;
					}
					
					Item item = new RSSItem(entry);
					item.setUserId(rssFeed.getSource() + "#" + rssFeed.getId());
					
					URL pageUrl = new URL(entry.getLink());
					user.setPageUrl(pageUrl.getProtocol() + "://" + pageUrl.getHost());
					item.setStreamUser(user);
					
					String label = feed.getLabel();
					if(label != null) {
						item.addLabel(label);
					}	
					
					items.add(item);			
				}
			}
			
		} catch (MalformedURLException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	
		response.setItems(items);
		return response;
	}
	

	@Override
	public Response retrieveKeywordsFeed(KeywordsFeed feed, Integer requests)
			throws Exception {
		return null;
	}

	@Override
	public Response retrieveAccountFeed(AccountFeed feed, Integer requests)
			throws Exception {
		return null;
	}

	@Override
	public Response retrieveLocationFeed(LocationFeed feed, Integer requests)
			throws Exception {
		return null;
	}

	@Override
	public Response retrieveGroupFeed(GroupFeed feed, Integer maxRequests) {
		return null;
	}

	@Override
	public StreamUser getStreamUser(String uid) {
		return null;
	}

	@Override
	public MediaItem getMediaItem(String id) {
		return null;
	}

	@Override
	public Item getItem(String id) {
		return null;
	}

	@Override
	public List<Item> getItemComments(Item item, long since) {
		return null;
	}
	
	
public static void main(String...args) throws Exception {
		
		String id = "bbci";
		String url = "http://feeds.bbci.co.uk/news/rss.xml";		
		
		long since = System.currentTimeMillis() - 90*24*3600*1000L;
		
		RssFeed feed = new RssFeed(id, url, since);
			
		RssRetriever retriever = new RssRetriever(null);
		Response response = retriever.retrieve(feed);
		
		System.out.println(response.getNumberOfItems() + " items");
		for(Item item : response.getItems()) {
			System.out.println("ID: " + item.getId());
			System.out.println("Title: " + item.getTitle());
			System.out.println(new Date(item.getPublicationTime()));
			System.out.println("Description: " + item.getDescription());
			System.out.println("User: " + item.getUserId());
			System.out.println("Url: " + item.getPageUrl());
			System.out.println("MediaIds: " + item.getMediaIds());
			System.out.println("Tags: " + StringUtils.join(item.getTags()));
			System.out.println(item.getMediaItems());
			System.out.println("Comments: " + item.getComments());
			System.out.println("------------------------------------");
			System.out.println(item.getStreamUser());
			System.out.println("====================================");
		}
	}
}
