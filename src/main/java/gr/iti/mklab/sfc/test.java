package gr.iti.mklab.sfc;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;

public class test {

	public static void main(String[] args){
		String oAuthConsumerKey 		= 	"ddWUBr13TOcdl53WHWEtlAohD";
		String oAuthConsumerSecret 		= 	"E4BlfdKqydxhoRmd5QbDcHhaVNCOozfus6CjJQCpmz2RFBt0YF";
		String oAuthAccessToken 		= 	"204974667-gEubebHAQLqwviK8ITewypwNHxWPF4VgCax5MZaZ";
		String oAuthAccessTokenSecret 	= 	"cbqZqU8vXRhUwZdYJqocoVW2IOMCrC8ahA7hn33JuHx4A";

		ConfigurationBuilder cb = new ConfigurationBuilder();
		
		cb.setJSONStoreEnabled(false)
			.setOAuthConsumerKey(oAuthConsumerKey)
			.setOAuthConsumerSecret(oAuthConsumerSecret)
			.setOAuthAccessToken(oAuthAccessToken)
			.setOAuthAccessTokenSecret(oAuthAccessTokenSecret)
			.setTweetModeExtended(true);
		Configuration conf = cb.build();
		
		TwitterFactory tf = new TwitterFactory(conf);
		Twitter twitter = tf.getInstance();
		
		Query query = new Query("air pollution");
		
		query.count(100);
		query.setResultType(Query.RECENT);

		QueryResult queryResult;
		try {
			queryResult = twitter.search(query);
			while(queryResult != null) {
				List<Status> statuses = queryResult.getTweets();
				for(Status status : statuses) {
					System.out.println(status.isTruncated());
					System.out.println(status.getText());
					
					System.out.println("-----");
					if (status.isRetweet()) {
						System.out.println(status.getRetweetedStatus().getText());
					}
					System.out.println("=====================================================");
				}
				
			}
			
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
}

