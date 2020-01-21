package gr.iti.mklab.framework.retrievers;

import gr.iti.mklab.framework.common.domain.Item;

import java.util.ArrayList;
import java.util.List;

public class Response {
	
	private List<Item> items = new ArrayList<Item>();
	private int requests = 0;
	private long lastTimestamp = 0l;

	public void setItems(List<Item> items) {
		this.items.addAll(items);
	}
	
	public List<Item> getItems() {
		return items;
	}
	
	public int getNumberOfItems() {
		return items.size();
	}
	
	public int getRequests() {
		return requests;
	}
	
	public void setRequests(int requests) {
		this.requests = requests;
	}
	
	public long getLastTimestamp() {
		return lastTimestamp;
	}

	public void setLastTimestamp(long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}
}
