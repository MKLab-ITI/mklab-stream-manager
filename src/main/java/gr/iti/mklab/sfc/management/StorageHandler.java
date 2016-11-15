package gr.iti.mklab.sfc.management;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gr.iti.mklab.framework.common.domain.config.Configuration;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.ItemState;
import gr.iti.mklab.sfc.filters.ItemFilter;
import gr.iti.mklab.sfc.processors.Processor;
import gr.iti.mklab.sfc.storages.Storage;
import gr.iti.mklab.sfc.streams.StreamException;
import gr.iti.mklab.sfc.streams.StreamsManagerConfiguration;

/**
 * Thread-safe class for managing the storage of items to databases 
 * The storage may be accomplished using multiple consumer-threads.
 * 
 * @author Manos Schinas - manosetro@iti.gr
 *
 */
public class StorageHandler implements Runnable {
	
	public final Logger logger = LogManager.getLogger(StorageHandler.class);
	
	// Internal queue used as a buffer of incoming items 
	private BlockingQueue<Item> queue = new LinkedBlockingDeque<Item>();
	
	private int numberOfConsumers = 16;
	private List<Consumer> consumers = new ArrayList<Consumer>(numberOfConsumers);
	
	private List<Storage> storages = new ArrayList<Storage>();
	
	private List<ItemFilter> filters = new ArrayList<ItemFilter>();
	private List<Processor> processors = new ArrayList<Processor>();
	
	private Map<String, Boolean> workingStatuses = new HashMap<String, Boolean>();
	
	private AtomicLong handled = new AtomicLong(0L);
	
	enum StorageHandlerState {
		OPEN, CLOSE
	}
	
	private StorageHandlerState state = StorageHandlerState.CLOSE;

	private Thread statusThread;
	
	public StorageHandler(StreamsManagerConfiguration config) {
		try {	
			state = StorageHandlerState.OPEN;
			
			createFilters(config);
			logger.info(filters.size() + " filters initialized!");
			
			createProcessors(config);
			logger.info(processors.size() + " processors initialized!");
			
			initializeStorageHandler(config);	
			
			statusThread = new Thread(this);	
			
		} catch (StreamException e) {
			logger.error("Error during storage handler initialization: " + e.getMessage());
		}
	}
	
	public StorageHandlerState getState() {
		return state;
	}
	
	/**
	 * Starts the consumer threads responsible for storing items to the database.
	 */
	public void start() {
		for(int i = 0; i < numberOfConsumers; i++) {
			Consumer consumer = new Consumer(queue, storages, filters, processors);
			consumers.add(consumer);
		}
		
		for(Consumer consumer : consumers) {
			consumer.start();
		}
		logger.info(consumers.size() + " consumers initialized.");
		
		statusThread.start();
	}

	public void handle(Item item) {
		try {
			
			waitStoragesToInitialize();
			
			handled.incrementAndGet();
			queue.add(item);
		}
		catch(Exception e) {
			logger.error(e);
		}
	}

	public void handle(Item[] items) {
		for (Item item : items) {
			handle(item);
		}
	}
	
	public void handle(List<Item> items) {
		for (Item item : items) {
			handle(item);
		}
	}
	
	private void handle(ItemState itemState) {
		for(Storage storage : storages) {
			storage.store(itemState);
		}
		
	}
	
	public void handleItemStates(List<ItemState> itemStates) {
		for (ItemState item : itemStates) {
			handle(item);
		}
	}

	public void delete(String id) {
		for(Storage storage : storages) {
			try {
				storage.delete(id);
			} catch (IOException e) {
				logger.error(e);
			}	
		}
	}
	
	/**
	 * Initializes the databases that are going to be used in the service
	 */
	private void initializeStorageHandler(StreamsManagerConfiguration config) throws StreamException {
		for (String storageId : config.getStorageIds()) {
		
			Storage storageInstance = null;
			try {
				logger.info("Initialize " + storageId);
				Configuration storageConfig = config.getStorageConfig(storageId);
				String storageClass = storageConfig.getParameter(Configuration.CLASS_PATH);
				Constructor<?> constructor = Class.forName(storageClass).getConstructor(Configuration.class);
				storageInstance = (Storage) constructor.newInstance(storageConfig);
					
				storages.add(storageInstance);
			} catch (Exception e) {
				logger.error(e);
				continue;
			}	
				
			try {
				if(storageInstance.open()) {
					logger.info("Storage " + storageId + " is working.");
					workingStatuses.put(storageId, true);
				}
				else {
					logger.error("Storage " + storageId + " is not working.");
					workingStatuses.put(storageId, false);	
				}
			} catch (Exception e) {
				logger.error("Error during " + storageId + " storage initialization", e);
				workingStatuses.put(storageId, false);	
			}
		}
	}
	
	private void createFilters(StreamsManagerConfiguration config) throws StreamException {
		for (String filterId : config.getFilterIds()) {
			try {
				logger.info("Initialize filter " + filterId);
				Configuration fconfig = config.getFilterConfig(filterId);
				String className = fconfig.getParameter(Configuration.CLASS_PATH);
				Constructor<?> constructor = Class.forName(className).getConstructor(Configuration.class);
				ItemFilter filterInstance = (ItemFilter) constructor.newInstance(fconfig);
			
				filters.add(filterInstance);
			}
			catch(Exception e) {
				logger.error("Error during filter " + filterId + "initialization", e);
			}
		}
	}
	
	private void createProcessors(StreamsManagerConfiguration config) throws StreamException {
		for (String processorId : config.getProcessorsIds()) {
			try {
				logger.info("Initialize processor " + processorId);
				Configuration pconfig = config.getProcessorConfig(processorId);
				String className = pconfig.getParameter(Configuration.CLASS_PATH);
				Constructor<?> constructor = Class.forName(className).getConstructor(Configuration.class);
				Processor processorInstance = (Processor) constructor.newInstance(pconfig);
			
				processors.add(processorInstance);
			}
			catch(Exception e) {
				e.printStackTrace();
				logger.error("Error during processor " + processorId + " initialization", e);
			}
		}
	}
	
	/**
	 * Stops all consumer threads and all the databases used
	 */
	public void stop() {
		for(Consumer consumer : consumers) {
			consumer.die();
		}
		
		for(Storage storage : storages) {
			storage.close();
		}
		
		state = StorageHandlerState.CLOSE;
		try {
			statusThread.interrupt();
		}
		catch(Exception e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public void run() {
		
		// runs just for sanity checks and logging
		while(state.equals(StorageHandlerState.OPEN)) {
			logger.info(handled.get() + " items handled in total.");
			logger.info(queue.size() + " items are queued for processing.");
			
			if(queue.size() > 1000000) {
				queue.clear();
			}
			
			for(Storage storage : storages) {
				try {
					boolean workingStatus = storage.checkStatus();
					
					workingStatuses.put(storage.getStorageName(), workingStatus);
					logger.info(storage.getStorageName() + " working status: " + workingStatus);
					
					if(!workingStatus) {
						storage.close();
						
						boolean status = storage.open();
						workingStatuses.put(storage.getStorageName(), status);
						logger.info(storage.getStorageName() + " working status: " + status);
					}
				}
				catch(Exception e) {
					logger.error("Exception during checking of " + storage.getStorageName(), e);
				}
			}
			
			for(ItemFilter filter : filters) {
				logger.info(filter.status());
			}
			
			for(Consumer consumer : consumers) {
				logger.info(consumer.status());
			}
			
			try {
				Thread.sleep(600000);
			} catch (InterruptedException e) {
				logger.info("Thread interrupted.");
			}
		}
	}
	
	private void waitStoragesToInitialize() {
		while(true) {
			boolean shouldContinue = true; 
			for(Entry<String, Boolean> ws : workingStatuses.entrySet()) {
				shouldContinue = shouldContinue && ws.getValue();
			}
			
			if(shouldContinue) {
				break;
			}
			else {
				try {
					logger.info("Wait as not all storages were initialized properly.");
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					logger.info(e);
				}
			}
		}
	}
	
}