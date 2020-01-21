package gr.iti.mklab.framework.common.domain;

import org.mongodb.morphia.annotations.Entity;

@Entity(noClassnameStored = true)
public class Account extends JSONable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 673802988788312082L;

	private String username;
	
	private String name;

	private Source source;
	
	public String getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Source getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = Source.valueOf(source);
	}
	
}
