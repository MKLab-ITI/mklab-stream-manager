package gr.iti.mklab.sfc.filters;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.config.Configuration;

public class SwearItemFilter extends ItemFilter {

	private Set<String> swearwords = new HashSet<String>();
	
	public SwearItemFilter(Configuration configuration) {
		super(configuration);
		
	    List<String> swearWords = Arrays.asList("anal","anus","arse","ar5e","ass","assfucker","assfukka","asshole","assfuck",
	    		"ballsack","balls","bastard","bitch","biatch","bigtits","blowjob","bollock","bollok","boner","boob","bugger","bum","butt","buttplug","clitoris",
	    		"cock","cocksuck","cocksucker","cocksucking","cockface","cockhead","cockmunch","c0cksucker","czechporn",
	    		"coon","crap","cum","cumshot","cummer","cunt","cuntlick","cuntlicking","damn","dick","dlck","dildo","dyke","ejaculate","ejaculation",
	    		"fag","faggot","feck","fellate","fellatio","felching","fingerfuck","fistfuck","fuck","fuckme","fudgepacker","flange",
	    		"gangbang","goddamn","handjob","homo","horny","jerk","jizz","knobend","labia","lmao","lmfao","muff","nigger","nigga","niggah","porn", "penis","pigfucker","piss","poop",
	    		"prick","pube","pussy","queer","scrotum","sexxx","shemale","shit","sh1t","shitdick","shiting","shitter","slut","smegma","spunk","tit","titfuck","tittywank","tosser",
	    		"teensporn","teensfuck","turd","twat","vagina","vulva","wank","wanker","whore","wtf","xxx");
	    
		swearwords.addAll(swearWords);
	}
	
	@Override
	public boolean accept(Item item) {
		
		try {
			String title = item.getTitle();
			if(title == null) {
				incrementDiscarded();
				return false;
			}
		
			title = title.replaceAll("\"", " ");
			title = title.replaceAll("'", " ");
			title = title.replaceAll(".", " ");
			
			Reader reader = new StringReader(title);
			TokenStream tokenizer = new WhitespaceTokenizer(reader);
			TokenStream stream = new LowerCaseFilter(tokenizer);
			
			List<String> tokens = new ArrayList<String>();
			CharTermAttribute charTermAtt = stream.addAttribute(CharTermAttribute.class);
			stream.reset();
			while (stream.incrementToken()) {
				String token = charTermAtt.toString();
				if(token.contains("http") || token.contains("https") || token.length() <= 1) {
					continue;
				}
				if(token.contains("#")) {
					token = token.replaceAll("#", "");
				}
				tokens.add(token);
			}
			stream.end();  
			stream.close();
			
			for(String token : tokens) {
				if(swearwords.contains(token)) {
					incrementDiscarded();
					return false;
				}
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
		return "SwearItemFilter";
	}

}
