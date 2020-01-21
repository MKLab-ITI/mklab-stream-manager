package gr.iti.mklab.framework.client.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

import gr.iti.mklab.framework.common.domain.collections.Collection;
import gr.iti.mklab.framework.common.domain.feeds.Feed;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.QueryResults;

/**
 *
 * @author Manos Schinas - manosetro@iti.gr
 *
 */
public class DAOFactory {

    public static int ASC = 1;
    public static int DESC = -1;
    
    private static MongoClientOptions options = MongoClientOptions.builder()
            .writeConcern(WriteConcern.UNACKNOWLEDGED).build();
    
    private static Map<String, MongoClient> connections = new HashMap<String, MongoClient>();
    private static Map<String, Datastore> datastores = new HashMap<String, Datastore>();

    public <K> BasicDAO<K, String> getDAO(String hostname, String dbName, Class<K> clazz) throws Exception {
    	return getDAO(hostname, dbName, clazz, null, null);
    }

    public <K> BasicDAO<K, String> getDAO(String hostname, String dbName, Class<K> clazz,
    		String username, String password) throws Exception {
    	
        String connectionKey = hostname + "#" + dbName;
        Datastore ds = datastores.get(connectionKey);
        
        Morphia morphia = new Morphia();
        MongoClient mongoClient = connections.get(hostname);

        if (mongoClient == null) {
        	ServerAddress srvAdr = new ServerAddress(hostname);
        	if(username != null && password != null) {
        		MongoCredential credential = MongoCredential.createScramSha1Credential(username, "admin", password.toCharArray());
        		mongoClient = new MongoClient(srvAdr, Arrays.asList(credential), options);
        	}
        	else {
        		mongoClient = new MongoClient(srvAdr, options);
        	}
        	
            connections.put(hostname, mongoClient);
        }
        
        if (ds == null) {
            ds = morphia.createDatastore(mongoClient, dbName);
            datastores.put(connectionKey, ds);
        }
		
        return new BasicDAO<K, String>(clazz, mongoClient, morphia, dbName);
    }

	public static void main(String...args) throws Exception {
		DAOFactory factory = new DAOFactory();
		BasicDAO<Collection, String> dao = factory.getDAO("160.40.50.207:27017", "StepV2", Collection.class);
		
		QueryResults<Collection> r = dao.find();
		List<Collection> collections = r.asList();
		
		for(Collection c : collections) {
			System.out.println(c);
			
			List<Feed> feeds = c.getFeeds();
			for(Feed feed : feeds) {
				System.out.println(feed.hashCode() + " => " + feed);
			}
			System.out.println("==============================================================");
		}
		
		/*
		BasicDAO<Feed, String> dao = factory.getDAO("xxx.xxx.xxx.xxx:27017", "db_name", Feed.class,
				"username", "passwd");
		
		Long since = System.currentTimeMillis()- 15l * 24l * 3600l * 1000l;
		String[] usernames = {
				"IPCCNews",
				"Environmental-issues-249918293252", 
				"EnvironmentalIssuesCommittee", 
				"EnvironmentalIssuesImpact",
				"creationcare",
				"greendig",
				"environmentalinvestigationagency",
				"earthworksaction",
				"greenforall",
				"ienearth",
				"green",
				"IDEASforUs",
				"WWF",
				"wildcatsanctuary",
				"greenpeace.international",
				"bigcatrescue",
				"seashepherdglobal",
				"thenatureconservancy",
				"NationalWildlife",
				"DefendersofWildlife",
				"ewg.org",
				"oceanconservancy",
				"AfricanWildlifeFoundation",
				"SierraClub",
				"350.org",
				"NationalAudubonSociety",
				"TheWildernessSociety",
				"RainforestAlliance",
				"nrdc.org",
				"NationalParks",
				"Earthjustice",
				"rainforestactionnetwork",
				"surfrider",
				"thedswt",
				"foe.us",
				"oceana",
				"EnvDefenseFund",
				"conservation.intl",
				"EPA",
				"www.santion.org",
				"HuffPostGreen",
				"SierraClub",
				"ClientEarth",
				"caneurope",
				"Coastwatch-8955392153",
				"EAERE",
				"The-European-Biomass-Association-164889743559184",
				"INFORSE",
				"EuropeanWildlife",
				"WorldNatureOrg",
				"unep.org",
				"iucn.org",
				"earthsystemgovernance",
				"TheGEF1"
		};
		
		for(Integer i=0; i<usernames.length; i++) {
			Feed feed = new AccountFeed("Facebook#"+usernames[i], usernames[i], since, "Facebook");
			feed.setLabel("environment");
			dao.save(feed);
		}
		 */
		
		/*
		String[] tags = {
			"#climate",
			"#environment",
			"#climatechange",
			"#globalwarming",
			"#renewablenergy",
			"#econews",
			"#greenjobs"
		};

		Feed hashtagsFeed = new KeywordsFeed("Twitter#hashtags", Arrays.asList(tags), since, "Twitter");
		hashtagsFeed.setLabel("environment");
		
		dao.save(hashtagsFeed);
		
		
		String[] keywordsToTrack = {
			"climate AND change",
			"global AND warming",
			"sea AND level AND rise",
			"greenhouse AND gas",
			
			"flooding",
			"ocean AND acidification",
			"environmental AND degradation",
			"air AND quality",
			
			"invasive AND species",
			"lead AND poisoning",
			"environmental AND health",
			"environmental AND issues AND energy",
			
			"toxicants",
			"air AND pollution",
			"soil AND pollution",
			"water AND pollution",
			
			"genetic AND pollution",
			"genetically AND modified AND food",
			"smog",
			"tropospheric AND ozone",
			
			"ozone AND depletion",
			"soil AND erosion",
			"soil AND contamination",
			"algal AND bloom",
			
			"eutrophication",
			"wastewater",
			"soil AND salination",
			"soil AND conservation",
			
			"deforestation",
			"volatile AND organic AND compounds",
			"electronic AND waste",
			"illegal AND dumping",
			
			"waste AND disposal AND incidents",
			"hazardous AND waste",
			"ocean AND acidification",
			"oil AND spills", 
			
			"overfishing",
			"nutrient AND pollution",
			"resource AND depletion",
			"toxic AND waste",
			
			"environmental AND law",
			"dichlorodiphenyltrichloroethane",
			"coal AND industry",
			"residual AND sodium AND carbonate",
			
			"ocean AND dumping",
			"persistent AND organic AND pollutant",
			"source AND pollution",
			"species AND extinction",
			
			"global AND dimming",
			"fossil AND fuels",
			"desertification",
			"climatechange",
			
			"renewable AND energy",
			"environmental AND crime",
			"environmental AND issues",
			"dioxin",
			
			"garbage AND patch",
			"wastewater",
			"toxic AND heavy AND metals",
			"thermohaline AND circulation",
			
			"marine AND debris",
			"acid AND mine AND drainage",
			"anoxic AND waters",
			"environmental AND crime",
	
			"illegal AND logging",
			"efficient AND energy AND use",
			"renewable AND energy",
			"electromagnetic AND radiation AND health",
			
			"preventing AND pollution",
			"clean AND energy",
			"particulate AND matter",
			"carbon AND pollution",
			
			"implications AND nanotechnology",
			"land AND pollution",
			"environmental AND concerns",
			"nuclear AND weapons",
			
			"waste AND management",
			"incineration",
			"recycling AND facilities", 
			"natural AND resource AND depletion",
			
			"recycling",
			"ozone AND layer",
			"acid AND rain",
			"urban AND sprawl"
			
			
		};
		
		int p = 4;
		List<String> ngrams = Arrays.asList(keywordsToTrack);
		for(int i=0; i<(ngrams.size()/p); i++) {
			
			int fromIndex = i*p;
			int toIndex = Math.min((fromIndex+p), ngrams.size());
			
			List<String> keywords = ngrams.subList(fromIndex, toIndex);
		
			Feed feed = new KeywordsFeed("Twitter#"+i, keywords, since, "Twitter");
			feed.setLabel("environment");
			
			dao.save(feed);
		}
			*/
		
		/*
		String[] urls = {
			"http://www.telegraph.co.uk/news/uknews/rss",
			"http://www.telegraph.co.uk/culture/tvandradio/rss",
			"http://www.telegraph.co.uk/finance/newsbysector/mediatechnologyandtelecoms/media/rss",
			"http://www.ft.com/rss/home/uk",
			"http://www.ft.com/rss/companies/media",
			"http://www.ft.com/rss/life-arts/film-television",
			"http://www.theguardian.com/uk/rss",
			"http://www.theguardian.com/media/rss",
			"http://www.theguardian.com/tv-and-radio/rss",
			"http://www.thetimes.co.uk/tto/news/uk/rss",
			"http://www.thetimes.co.uk/tto/news/medianews/rss",
			"http://www.thetimes.co.uk/tto/arts/tv-radio/rss",
			"http://www.independent.co.uk/news/uk/?service=rss",
			"http://www.independent.co.uk/news/media/?service=rss",
			"http://www.independent.co.uk/arts-entertainment/tv/?service=rss",
			"http://www.standard.co.uk/news/uk/rss",
			"http://www.standard.co.uk/business/media/rss",
			"http://www.standard.co.uk/showbiz/tv/rss",
			"http://metro.co.uk/news/uk/feed/",
			"http://metro.co.uk/entertainment/tv/feed/",
			"http://feeds.bbci.co.uk/news/uk/rss.xml",
			"http://feeds.bbci.co.uk/news/entertainment_and_arts/rss.xml",
			"http://www.channel4.com/news/uk/rss",
			"http://www.channel4.com/news/culture/rss",
			"http://feeds.skynews.com/feeds/rss/uk.xml",
			"http://feeds.skynews.com/feeds/rss/entertainment.xml",
			"http://www.economist.com/topics/united-kingdom/index.xml",
			"http://www.economist.com/topics/media/index.xml",
			"http://www.newstatesman.com/feeds/topics/media.rss",
			"http://www.huffingtonpost.co.uk/news/news/feed/",
			"http://www.huffingtonpost.co.uk/news/media/feed/",
			"http://www.huffingtonpost.co.uk/news/uktv/feed/",
			"http://feeds2.feedburner.com/guidofawkes"
		};
		
		for(Integer i=0; i<urls.length; i++) {
		
			Feed feed = new RssFeed(i.toString(), urls[i], since, "RSS");
			feed.setLabel("BBC");
			
			dao.save(feed);
		}
		*/
		
	}
	
}
