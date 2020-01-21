package gr.iti.mklab.framework.common.domain;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

@Entity(value="ItemState", noClassnameStored=false)
@Indexes({
	@Index("id, -timestamp")
})
public class ItemState extends JSONable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4721794425539829325L;
	
	public ItemState(String id) {
		super.id = id;
	}
	
    public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Long getLikes() {
		return likes;
	}

	public void setLikes(Long likes) {
		this.likes = likes;
	}

	public Long getShares() {
		return shares;
	}

	public void setShares(Long shares) {
		this.shares = shares;
	}

	public Long getComments() {
		return comments;
	}

	public void setComments(Long comments) {
		this.comments = comments;
	}

	protected Long timestamp;
    
    protected Long likes;
    
    protected Long shares;
    
    protected Long comments;
}
