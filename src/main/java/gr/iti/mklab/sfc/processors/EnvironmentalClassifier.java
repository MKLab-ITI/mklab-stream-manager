package gr.iti.mklab.sfc.processors;

import java.util.Map;

import gr.iti.mklab.classifiers.ClassificationResult;
import gr.iti.mklab.classifiers.EnvironmentalClassification;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.config.Configuration;

public class EnvironmentalClassifier extends Processor {

	private EnvironmentalClassification ec;
	private double threshold;
	
	public EnvironmentalClassifier(Configuration configuration) {
		super(configuration);
		
		threshold = Double.parseDouble(configuration.getParameter("threshold", "0.2"));
		
		ec = new EnvironmentalClassification();
	}

	@Override
	public void process(Item item) {
		String lang = item.getLanguage();
		
		String text = "";
		String title = item.getTitle();
		String description = item.getDescription();
		
		if(title != null) {
			text += " " + title;
		}
		if (description != null) {
			text +=  " " + title;
		}

		try {
			ClassificationResult result = ec.classify(text, lang, false);
		
			Map<String, Double> probMap = result.getFirstClassifierProbabilityMap();		
			
			if(probMap.get("Relevant") > 0.5) {
				item.addTopic("environment", probMap.get("Relevant"));
				Map<String, Double> subTopics = result.getSecondClassifierProbabilitityMap();
				for(String subTopic : subTopics.keySet()) {
					if(subTopics.get(subTopic) > threshold) {
						item.addTopic(subTopic, subTopics.get(subTopic));
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(item.toString());
	}

	public static void main(String...args) {
		System.out.println("TEST");
		
		EnvironmentalClassifier clssf = new EnvironmentalClassifier(null);
		
		Item item = new Item();
		item.setLanguage("en");
		item.setTitle("it was first held in 1974 to raise awareness about marine pollution human overpopulation& global warming #WorldEnvironmentDay");
		
		clssf.process(item);
		
	}
}
