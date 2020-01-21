package gr.iti.mklab.framework.common.domain;

import java.util.HashSet;
import java.util.Set;

import org.mongodb.morphia.annotations.Entity;

@Entity(noClassnameStored = true)
public class Cluster extends JSONable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5041762845708676137L;

	public Cluster(String id) {
		this.id = id;
	}
	
	private Set<String> members = new HashSet<String>();
	
	private int count = 0;
	
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public Set<String> getMembers() {
        return members;
    }

    public void addMembers(Set<String> members) {
        this.members.addAll(members);
        this.count = this.members.size();
    }
    
    public void addMember(String member) {
        this.members.add(member);
        this.count = this.members.size();
    }
    
    public int getCount() {
        return count;
    }
	
}
