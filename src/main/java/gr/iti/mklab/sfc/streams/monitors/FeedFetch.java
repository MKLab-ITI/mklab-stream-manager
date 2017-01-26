package gr.iti.mklab.sfc.streams.monitors;

import gr.iti.mklab.framework.common.domain.feeds.Feed;

public class FeedFetch {
	
	private Feed feed;
	
	private Long initializationTime = System.currentTimeMillis();
	private Long firstExecutionTime = null;
	private Long lastExecutionTime = 0L;
	
	private Integer executions = 0;
	
	private Integer lastFetchedItems = 0;
	private Long fetchedItems = 0L;
	
	public FeedFetch(Feed feed) {
		this.feed = feed;
	}

	public Long getInitializationTime() {
		return initializationTime;
	}

	public void setInitializationTime(Long initializationTime) {
		this.initializationTime = initializationTime;
	}

	public Long getFirstExecutionTime() {
		return firstExecutionTime;
	}

	public void setFirstExecutionTime(Long firstExecutionTime) {
		this.firstExecutionTime = firstExecutionTime;
	}
	
	public Long getLastExecutionTime() {
		return lastExecutionTime;
	}

	public void setLastExecutionTime(Long lastExecution) {
		this.lastExecutionTime = lastExecution;
	}

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	public Long getFetchedItems() {
		return fetchedItems;
	}

	public void setFetchedItems(Long fetchedItems) {
		this.fetchedItems = fetchedItems;
	}
	
	public void incFetchedItems(Integer lastFetchedItems) {
		this.lastFetchedItems = lastFetchedItems;
		this.fetchedItems += fetchedItems;
	}
	
	public double getItemsPerSecond() {
		
		Double runningTime = (double) (initializationTime - lastExecutionTime);
		if(runningTime <= 0) {
			return .0;
		}
			
		return (1000. * fetchedItems.doubleValue()) / (runningTime);
	}

	public Integer getExecutions() {
		return executions;
	}

	public void incExecutions() {
		this.executions++;
	}

	public Integer getLastFetchedItems() {
		return lastFetchedItems;
	}

	public void setLastFetchedItems(Integer lastFetchedItems) {
		this.lastFetchedItems = lastFetchedItems;
	}
}