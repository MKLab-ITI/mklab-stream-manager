package gr.iti.mklab.sfc.utils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

import cmu.arktweetnlp.Twokenize;


public class TextUtils {

	public static String clean(String text) {
		
		String resultString = Normalizer.normalize(text, Normalizer.Form.NFD);
		//resultString = resultString.replaceAll("[^\\x00-\\x7F]", "");
		resultString = resultString.replaceAll("\\p{Cntrl}", "");
		resultString = resultString.replaceAll("\\n", " ");
		resultString = resultString.replaceAll("\\.{2,}", ". ");
		
		return resultString;
	}
	
	public static String normalize(String text) {
		String resultString = text.replaceAll("i'm", "i am");
		resultString = resultString.replaceAll("it's", "it is");
		resultString = resultString.replaceAll("what's", "what is");
		resultString = resultString.replaceAll("don't", "do not");
		resultString = resultString.replaceAll("dont ", "do not ");
		
		
		return resultString;
	}
	
	public static List<String> tokenize(String text) {
		List<String> tokens = Twokenize.tokenize(text);
		return tokens;
	}
	
	public static void cleanTokens(List<String> tokens) {
		CollectionUtils.filter(tokens, new Predicate<String>() {
			@Override
			public boolean evaluate(String token) {
				return (token.length() > 2) 
						&& !token.startsWith("https://") 
						&& !token.startsWith("@");
			}
		});
	}
	
	public static List<String> urlsAndMentions(List<String> tokens) {
		List<String> toKeep = new ArrayList<String>(tokens);
		CollectionUtils.filter(toKeep, new Predicate<String>() {
			@Override
			public boolean evaluate(String token) {
				return token.startsWith("https://") || token.startsWith("@");
			}
		});
		return toKeep;
	}
	
}
