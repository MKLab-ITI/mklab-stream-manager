package gr.iti.mklab.framework.common.domain;

import org.mongodb.morphia.annotations.Entity;

/**
 *
 * @author	Manos Schinas - manosetro@iti.gr
 * 
 */

@Entity(noClassnameStored = true)
public  enum Source {
	All, 
	Facebook, 
	Twitter, 
	Flickr, 
	Web, 
	Youtube, 
	WebFeeds, 
	GooglePlus, 
	Tumblr, 
	Instagram, 
	Google, 
	Topsy,
	RSS,
	Reddit,
	OpenFuego,
	NewsWhip,
	TrendsMap,
	LiveStream
}