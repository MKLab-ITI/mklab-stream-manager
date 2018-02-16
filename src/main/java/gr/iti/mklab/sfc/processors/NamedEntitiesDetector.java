package gr.iti.mklab.sfc.processors;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.logging.Redwood;
import edu.stanford.nlp.util.logging.StanfordRedwoodConfiguration;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.NamedEntity;
import gr.iti.mklab.framework.common.domain.config.Configuration;

public class NamedEntitiesDetector extends Processor {
			
	private AbstractSequenceClassifier<CoreLabel> classifier;

	public NamedEntitiesDetector(Configuration configuration) {
		super(configuration);
		
		StanfordRedwoodConfiguration.setup();
		Redwood.hideChannelsEverywhere();

		String serializedClassifier = configuration.getParameter("serializedClassifier");
		classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
	}

	@Override
	public void process(Item item) {
		
		Map<String, NamedEntity> entities = new HashMap<String, NamedEntity>();
		String title = item.getTitle();
		if(title != null) {
			try {
				extractEntities(title, entities);
			} catch (Exception e) {}
		}
		
		String description = item.getDescription();
		if(description != null) {
			try {
				extractEntities(description, entities);
			} catch (Exception e) {}
		}
		item.setEntities(new ArrayList<NamedEntity>(entities.values()));
	}

	public String extractEntities(String text, Map<String, NamedEntity> entities) throws Exception {

		// clean before extraction
		text = Jsoup.parse(text).text();
		text = StringEscapeUtils.unescapeXml(text);
		text = text.replaceAll("&\\s+", "&amp; ");
		
		String textXML = classifier.classifyWithInlineXML(text);
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder docb = dbf.newDocumentBuilder();
        
        byte[] content = ("<DOC>" + textXML + "</DOC>").getBytes();
		ByteArrayInputStream bis = new ByteArrayInputStream(content);
		Document doc = docb.parse(bis);
		
		//3-class model
		extractEntities(entities, doc, NamedEntity.Type.PERSON);
		extractEntities(entities, doc, NamedEntity.Type.LOCATION);
		extractEntities(entities, doc, NamedEntity.Type.ORGANIZATION);

		return  textXML;
	}
	
	private void extractEntities(Map<String, NamedEntity> entities, Document doc, NamedEntity.Type type) {
		NodeList nodeList = doc.getElementsByTagName(type.name());
        for (int k = 0; k < nodeList.getLength(); k++) {
            String name = nodeList.item(k).getTextContent().toLowerCase();
            if(name == null) {
            	continue;
            }
            
            name = name.replaceAll("[^A-Za-z0-9 ]", "");
            name = name.replaceAll("\\s+", " ");
            name = name.trim();
            
            if(name == null || name.length() < 2 | name.length() > 40) {
            	continue;
            }
            
            String[] neParts = name.split(" ");
            if(neParts.length > 3) {
            	continue;
            }
            
            String key = type + "#" + name;
            if (!entities.containsKey(key)) {
            	NamedEntity entity = new NamedEntity(name, type);
            	entities.put(key, entity);
            }
            else {
            	NamedEntity entity = entities.get(key);
            	entity.setCount(entity.getCount() + 1);
            }
        }
	}
	
	public static void main(String...args) {
		Configuration conf = new Configuration();
		conf.setParameter("serializedClassifier", "english.all.3class.distsim.crf.ser.gz");
		NamedEntitiesDetector detector = new NamedEntitiesDetector(conf);
		
		Item item = new Item();
		item.setTitle("David Cameron &amp; <a>Barack Obama</a>: pensioner benefits protected if Tories win election - video");
		//item.setDescription("A new Conservative government would make unemployed young people work for benefits, David Cameron says on Tuesday. In a speech in Hove, East Sussex, the prime minister says that under Tory plans 18 to 21-year-olds who have been out of work, education or training for six months would have to take on unpaid community work if they want to claim benefits. 'From day one they should make an effort', he says Continue reading...");
		
		detector.process(item);
		
		System.out.println(item.getEntities());
	}
}
