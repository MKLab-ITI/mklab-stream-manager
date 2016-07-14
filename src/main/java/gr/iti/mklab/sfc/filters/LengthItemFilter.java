package gr.iti.mklab.sfc.filters;

import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.config.Configuration;

import org.apache.logging.log4j.LogManager;

public class LengthItemFilter extends ItemFilter {

	private Integer minTextLenth = 15;
	
	public LengthItemFilter(Configuration configuration) {
		super(configuration);
		String lenStr = configuration.getParameter("length", "15");
		this.minTextLenth  = Integer.parseInt(lenStr);
		
		LogManager.getLogger(LengthItemFilter.class).info("Initialized. Min Text Lenth: " + minTextLenth);
	}
	
	@Override
	public boolean accept(Item item) {
		
		if(item == null) {
			incrementDiscarded();
			return false;
		}
		
		String title = item.getTitle();
		if(title == null) {
			incrementDiscarded();
			return false;
		}
		
		try {
			String[] tags = item.getTags();
			if(tags != null) {
				for(String tag : tags) {
					title = title.replaceAll(tag, " ");
				}
			}
		
			String[] mentions = item.getMentions();
			if(mentions != null) {
				for(String mention : mentions) {
					title = title.replaceAll(mention, " ");
				}
			}
		
			String[] links = item.getLinks();
			if(links != null) {
				for(String link : links) {
					title = title.replaceAll(link, " ");
				}
			}
		
			title = title.replaceAll("#", " ");
			title = title.replaceAll("@", " ");
			title = title.replaceAll("\\s+", " ");
			
		}
		catch(Exception e) { 
			
		}
		
		if(title.length() < minTextLenth) {
			incrementDiscarded();
			return false;
		}
		
		incrementAccepted();
		return true;
	}

	@Override
	public String name() {
		return "LengthItemFilter";
	}
}
