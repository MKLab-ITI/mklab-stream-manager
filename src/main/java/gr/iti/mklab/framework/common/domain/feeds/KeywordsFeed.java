package gr.iti.mklab.framework.common.domain.feeds;

import java.util.ArrayList;
import java.util.List;

public class KeywordsFeed extends Feed {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4054548274586004413L;

	private List<String> keywords = new ArrayList<String>();
	
	public KeywordsFeed() {
		
	}
	
	public KeywordsFeed(String id, String keyword, long since, String source) {
		super(id, since, source);
		
		if(keyword.contains(" OR ") || keyword.contains(" AND ")) {
			if(keyword.startsWith("(")) {
				keyword = "(" + keyword;
			}
			if(keyword.endsWith(")")) {
				keyword = keyword + ")";
			}
		}
		this.keywords.add(keyword);
	}
	
	public KeywordsFeed(String id, List<String> keywords, long since, String source) {
		super(id, since, source);
		for(String keyword : keywords) {
			if(keyword.contains(" OR ") || keyword.contains(" AND ")) {
				if(!keyword.startsWith("(")) {
					keyword = "(" + keyword;
				}
				if(!keyword.endsWith(")")) {
					keyword = keyword + ")";
				}
			}
			this.keywords.add(keyword);
		}
		//this.keywords.addAll(keywords);
	}
	
	public List<String> getKeywords() {
		return keywords;
	}
	
	public void setKeywords(List<String> keywords) {
		this.keywords.clear();
		for(String keyword : keywords) {
			if(keyword.contains(" OR ") || keyword.contains(" AND ")) {
				if(!keyword.startsWith("(")) {
					keyword = "(" + keyword;
				}
				if(!keyword.endsWith(")")) {
					keyword = keyword + ")";
				}
			}
			this.keywords.add(keyword);
		}
		//this.keywords.addAll(keywords);
	}
	
	public void addKeywords(List<String> keywords) {
		for(String keyword : keywords) {
			if(keyword.contains(" OR ") || keyword.contains(" AND ")) {
				if(!keyword.startsWith("(")) {
					keyword = "(" + keyword;
				}
				if(!keyword.endsWith(")")) {
					keyword = keyword + ")";
				}
			}
			this.keywords.add(keyword);
		}
		//this.keywords.addAll(keywords);
	}
	
	public void addKeyword(String keyword) {
		if(keyword.contains(" OR ") || keyword.contains(" AND ")) {
			if(!keyword.startsWith("(")) {
				keyword = "(" + keyword;
			}
			if(!keyword.endsWith(")")) {
				keyword = keyword + ")";
			}
		}
		
		this.keywords.add(keyword);
	}
}
