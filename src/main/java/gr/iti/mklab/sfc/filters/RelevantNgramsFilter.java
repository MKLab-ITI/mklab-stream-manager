package gr.iti.mklab.sfc.filters;

import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.config.Configuration;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class RelevantNgramsFilter  extends ItemFilter {

	private List<String[]> ngrams = new ArrayList<String[]>();
	
	public RelevantNgramsFilter(Configuration configuration) {
		super(configuration);
		
		String filename = configuration.getParameter("RelevantTermsFilename");
		try {
			Iterable<String> lines = IOUtils.readLines(new FileReader(filename));
			for(String line : lines) {
				String[] ngram = line.split("\t");
				ngrams.add(ngram);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean accept(Item item) {
		
		try {
			String title = item.getTitle();
			if(title == null) {
				incrementDiscarded();
				return false;
			}
		
			title = title.toLowerCase();

			for(String[] ngram : ngrams) {
				boolean accept = true;
				for(String token : ngram) {
					if(!title.contains(token)) {
						accept = false;
						break;
					}
				}
				
				if(accept) {
					incrementAccepted();
					return true;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			incrementDiscarded();
			return false;
		}
		
		incrementDiscarded();
		return false;
	}

	@Override
	public String name() {
		return "TokensItemFilter";
	}
	
}
