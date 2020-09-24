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
		Redwood.stop();

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
		text = StringEscapeUtils.escapeXml(text);
		
		text = text.replaceAll("&#55356", " "); 
		text = text.replaceAll("&#55357", " "); 
		text = text.replaceAll("&#55358", " "); 
		text = text.replaceAll("&#56842", " "); 
		text = text.replaceAll("&#56399", " "); 
		text = text.replaceAll("&#56589", " "); 
		text = text.replaceAll("&#56596", " "); 
		text = text.replaceAll("&#56808", " "); 
		text = text.replaceAll("&#56860", " ");
		text = text.replaceAll("&#56810", " ");
		text = text.replaceAll("&#56657", " ");
		text = text.replaceAll("&#56391", " ");
		text = text.replaceAll("&#56628", " ");
		text = text.replaceAll("&#56397", " ");
		text = text.replaceAll("&#56725", " ");
		text = text.replaceAll("&#56611", " ");
		text = text.replaceAll("&#56725", " ");
		text = text.replaceAll("&#56376", " ");
		text = text.replaceAll("&#56424", " ");
		text = text.replaceAll("&#56547", " ");
		text = text.replaceAll("&#57173", " ");
		text = text.replaceAll("&#57332", " ");
		text = text.replaceAll("&#56613", " ");
		text = text.replaceAll("&#56546", " ");
		text = text.replaceAll("&#56650", " ");
		text = text.replaceAll("&#57018", " ");
		text = text.replaceAll("&#56824", " ");
		text = text.replaceAll("&#56826", " ");
		text = text.replaceAll("&#56814", " ");
		text = text.replaceAll("&#56887", " ");
		text = text.replaceAll("&#56821", " ");
		text = text.replaceAll("&#56833", " ");
		text = text.replaceAll("&#56841", " ");
		text = text.replaceAll("&#56394", " ");
		text = text.replaceAll("&#57320", " ");
		text = text.replaceAll("&#56476", " ");
		text = text.replaceAll("&#56485", " ");
		text = text.replaceAll("&#56806", " ");
		text = text.replaceAll("&#56839", " ");
		text = text.replaceAll("&#56570", " ");
		text = text.replaceAll("&#56803", " ");
		text = text.replaceAll("&#57010", " ");
		text = text.replaceAll("&#57118", " ");
		text = text.replaceAll("&#57145", " ");
		text = text.replaceAll("&#56846", " ");
		text = text.replaceAll("&#56834", " ");
		text = text.replaceAll("&#56845", " ");
		text = text.replaceAll("&#56838", " ");
		text = text.replaceAll("&#56881", " ");
		text = text.replaceAll("&#56688", " ");
		
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
		
		String title = "David Cameron & <a href='/test'>&Barack Obama</a>: pensioner benefits protected if Tories win election - video";
		Item item = new Item();
		item.setTitle(title);
		//item.setDescription("A new Conservative government would make unemployed young people work for benefits, David Cameron says on Tuesday. In a speech in Hove, East Sussex, the prime minister says that under Tory plans 18 to 21-year-olds who have been out of work, education or training for six months would have to take on unpaid community work if they want to claim benefits. 'From day one they should make an effort', he says Continue reading...");
		
		detector.process(item);
		System.out.println("TEST@@@");
		System.out.println(item.getEntities());
		 
	}
}
