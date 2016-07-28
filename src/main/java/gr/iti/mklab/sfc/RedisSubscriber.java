package gr.iti.mklab.sfc;

import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mongodb.morphia.Morphia;

import gr.iti.mklab.framework.common.domain.collections.Collection;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.restfb.util.StringUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;


public class RedisSubscriber extends JedisPubSub implements Runnable {

		private Logger logger = LogManager.getLogger(RedisSubscriber.class);
		private Morphia morphia = new Morphia();
		
		private BlockingQueue<Pair<Collection, String>> collectionsQueue;
		private BlockingQueue<Pair<Pair<String, String>, String>> itemsQueue;
		
		private String redisHost = null;
		private Jedis jedis = null;
		
		private boolean stop = false;
		
		public RedisSubscriber(BlockingQueue<Pair<Collection, String>> collectionsQueue, 
				BlockingQueue<Pair<Pair<String, String>, String>> itemsQueue,
				String redisHost) {
			this.collectionsQueue = collectionsQueue;
			this.itemsQueue = itemsQueue;
			this.morphia.map(Collection.class);
			this.redisHost = redisHost;
			
			connect();
		}
		
		private boolean connect() {
			try {
				this.jedis = new Jedis(redisHost, 6379, 0);
				jedis.ping();
				
				return true;
			}
			catch(Exception e) {
				jedis = null;
				return false;
			}
		}
		
		private boolean isStatusOK() {
			if(jedis == null) {
				return false;
			}
			
			try {
				String r = jedis.ping();
				logger.info("redis ping: " + r);
			}
			catch(Exception e) {
				logger.error("Redis exception", e);
				return false;
			}
			
			return true;
		}
		
	    @Override
	    public void onMessage(String channel, String message) {
	    	logger.info("Channel: " + channel + ", Message: " + message);
	    }

	    @Override
	    public void onPMessage(String pattern, String channel, String message) {	
	    	try {
	    		logger.info("Pattern: " + pattern + ", Channel: " + channel + ", Message: " + message);
	    		
	    		if(pattern.equals("collections:*")) {
	    			DBObject obj = (DBObject) JSON.parse(message);	    	
		    		Collection collection = morphia.fromDBObject(Collection.class, obj);
		    		
		    		Pair<Collection, String> actionPair = Pair.of(collection, channel);
		    		collectionsQueue.put(actionPair);
	    		}
	    		else if(pattern.equals("items:*")) {
	    			DBObject obj = (DBObject) JSON.parse(message);	 
	    			Pair<String, String> pair = Pair.of(obj.get("id").toString(), obj.get("source").toString());
	    			
	    			itemsQueue.put(Pair.of(pair, channel));
	    		}
	    		
				
	    	}
	    	catch(Exception e) {
	    		logger.error("Error on message " + message + ". Channel: " + channel + " pattern: " + pattern, e);
	    	}
	    }

	    @Override
	    public void onSubscribe(String channel, int subscribedChannels) {
	    	logger.info("Subscribe to " + channel + " channel. " + subscribedChannels + " active subscriptions.");
	    }

	    @Override
	    public void onUnsubscribe(String channel, int subscribedChannels) {
	    	logger.info("Unsubscribe from " + channel + " channel. " + subscribedChannels + " active subscriptions.");
	    }

	    @Override
	    public void onPUnsubscribe(String pattern, int subscribedChannels) {
	    	logger.info("Unsubscribe to " + pattern + " pattern. " + subscribedChannels + " active subscriptions.");
	    }

	    @Override
	    public void onPSubscribe(String pattern, int subscribedChannels) {
	    	logger.info("Subscribe from " + pattern + " pattern. " + subscribedChannels + " active subscriptions.");
	    }

		@Override
		public void run() {
			while(!stop) {
				if(!isStatusOK()) {
					boolean connected = connect();
					if(!connected) {
						try {
							logger.info("Cannot connect to redis. Wait to recover.");
							Thread.sleep(30000);
						} catch (InterruptedException e) {
							logger.error(e);
						}
					}
				}
				else {
					try {
						String[] patterns = {"collections:*", "items:*"};
						
						logger.info("Subscribe to channels: " + StringUtils.join(patterns)); 
						jedis.psubscribe(this, patterns);
				
						logger.info("Subscriber unsubscribe from channels: " + StringUtils.join(patterns));
					}
					catch(Exception e) {
						logger.error(e);
					}
				}
			}
			logger.info("Subscriber shutdown");
		}
		
		public void start() {
			stop = false;
			
			Thread thread = new Thread(this);
			thread.start();
		}
		
		public void close() {
			stop = true;
			unsubscribe();
		}
	}