package gr.iti.mklab.framework.common.domain.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mongodb.morphia.annotations.Entity;

import gr.iti.mklab.framework.common.domain.JSONable;

/**
 * Class for the configuration of streams or storages 
 * reading a set of parameters
 * 
 * @author manosetro - manosetro@iti.gr
 *
 */

@Entity(noClassnameStored = true)
public class Configuration extends JSONable implements Iterable<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5070483137103099259L;
	
	private static Pattern pattern = Pattern.compile("(\\Q${\\E)(.*)(\\Q}\\E)");
	
	public static final String CLASS_PATH = "Classpath";
	
	private Map<String, String> params = new HashMap<String, String>();

	public String getParameter(String name) {
		return getParameter(name,null);
	}
	
	public String getParameter(String name, String defaultValue){	
		String value = params.get(name);
		
		if(value != null) { 
		Matcher matcher = pattern.matcher(value);
			if(matcher.find()) {
				value = matcher.group(2);
				value = System.getProperty(value);
			}
		}
		
		return value == null ? defaultValue : value;
	}
	
	public void setParameter(String name, String value) {
		params.put(name,value);
	}

	@Override
	public Iterator<String> iterator() {
		return params.keySet().iterator();
	}
	
}
