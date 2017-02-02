package gr.iti.mklab.sfc.filters;

import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.config.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;


/**
 * 
 * @author Manos Schinas - manosetro@iti.gr
 *
 * This filter discard items that have many hashtags as possible spam.
 * 	
 */
public class TagsItemFilter extends ItemFilter {

	private int maxTags = 4;
	private Set<String> sourcesToInclude = new HashSet<String>();
			
	public TagsItemFilter(Configuration configuration) {
		super(configuration);
		String lenStr =configuration.getParameter("maxTags", "4");
		this.maxTags  = Integer.parseInt(lenStr);
		
		String sources = configuration.getParameter("sources");
		if(sources != null && !sources.equals("")) {
			String[] parts = sources.split(",");
			sourcesToInclude.addAll(Arrays.asList(parts));
		}
		
		LogManager.getLogger(TagsItemFilter.class).info("Initialized. Max Number of Tags: " + maxTags);
	}
	
	@Override
	public synchronized boolean accept(Item item) {
			
		if(item == null) {
			incrementDiscarded();
			return false;
		}
		
		if(!sourcesToInclude.contains(item.getSource())) {
			incrementAccepted();
			return true;
		}
		
		String[] tags = item.getTags();
		if(tags == null) {
			incrementAccepted();
			return true;
		}
		
		if(tags.length >= maxTags) {
			incrementDiscarded();
			return false;
		}
		
		incrementAccepted();
		return true;
	}

	@Override
	public String name() {
		return "TagsItemFilter";
	}
	
}
