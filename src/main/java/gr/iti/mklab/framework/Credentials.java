package gr.iti.mklab.framework;

import java.io.Serializable;

public class Credentials implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -278602176191233881L;
	
	private String key;
	private String secret;
	
	private String accessToken;
	private String accessTokenSecret;
	
	private String clientId;
	
	public Credentials() {
		
	}
	
	public Credentials(String key, String secret, String accessToken, String accessTokenSecret) {
		this.key = key;
		this.secret = secret;
		this.accessToken = accessToken;
		this.accessTokenSecret = accessTokenSecret;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	public String getSecret() {
		return secret;
	}
	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}
	
	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public String getClientId() {
		return clientId;
	}
	
}
