package gr.iti.mklab.sfc.filters;

import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.config.Configuration;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class TokensItemFilter  extends ItemFilter {

	private int minTokens;
	
	public TokensItemFilter(Configuration configuration) {
		super(configuration);
		String lenStr =configuration.getParameter("minTokens", "6");
		this.minTokens  = Integer.parseInt(lenStr);
		
		LogManager.getLogger(TokensItemFilter.class).info("Initialized. Min Number of Tokens: " + minTokens);
	}

	@Override
	public boolean accept(Item item) {
		
		try {
			String title = item.getTitle();
			if(title == null) {
				incrementDiscarded();
				return false;
			}
		
			Reader reader = new StringReader(title);
			WhitespaceTokenizer tokenizer = new WhitespaceTokenizer();
			tokenizer.setReader(reader);
			
			List<String> tokens = new ArrayList<String>();
			CharTermAttribute charTermAtt = tokenizer.addAttribute(CharTermAttribute.class);
			tokenizer.reset();
			while (tokenizer.incrementToken()) {
				String token = charTermAtt.toString();
				if(token.contains("http") || token.contains(".") || token.length() <= 1)
					continue;
				tokens.add(token);
			}
			tokenizer.end();  
			tokenizer.close();
			
			if(tokens.size() < minTokens) {
				incrementDiscarded();
				return false;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			incrementDiscarded();
			return false;
		}
		
		incrementAccepted();
		return true;
	}

	@Override
	public String name() {
		return "TokensItemFilter";
	}
	
}
