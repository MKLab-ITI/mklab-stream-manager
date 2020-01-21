package gr.iti.mklab.framework.common.domain.feeds;


public class AccountFeed extends Feed {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6865708239941324229L;
	
	private String username = null;
	
	public AccountFeed() {
		
	}
	
	public AccountFeed(String id, String username, long since, String source) {
		super(id, since, source);

		this.username = username;
	}

	public String getUsername() {
		return this.username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}

}
