package gr.iti.mklab.sfc.filters;

import java.util.Map;

import gr.iti.mklab.classifiers.ClassificationResult;
import gr.iti.mklab.classifiers.EnvironmentalClassification;
import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.config.Configuration;
import gr.iti.mklab.sfc.filters.ItemFilter;

public class EnvironmentalClassifier extends ItemFilter {

	private EnvironmentalClassification ec;
	private double threshold;
	
	public EnvironmentalClassifier(Configuration configuration) {
		super(configuration);
		
		threshold = Double.parseDouble(configuration.getParameter("threshold", "0.3"));
		
		ec = new EnvironmentalClassification();
	}

	@Override
	public boolean accept(Item item) {
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
				if(result.getSecondClassifierProbabilitityMap() != null) {
					Map<String, Double> subTopics = result.getSecondClassifierProbabilitityMap();
					for(String subTopic : subTopics.keySet()) {
						if(subTopics.get(subTopic) > threshold) {
							item.addTopic("environment." + subTopic, subTopics.get(subTopic));
						}
					}
				}
				
				return true;
			}
			else {
				if(probMap.get("Relevant") > threshold) {
					return true;
				}
				
				return false;
			}
			
		} catch (Exception e) { 
			return false;
		}
	}

	@Override
	public String name() {
		return "EnvironmentalClassifier";
	}
}
