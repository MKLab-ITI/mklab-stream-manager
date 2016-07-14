package gr.iti.mklab.sfc.processors;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.config.Configuration;
import gr.iti.mklab.sfc.utils.MinHash;
import gr.iti.mklab.sfc.utils.TextUtils;

public class MinHashExtractor extends Processor {

	
	private MinHash minHash;
	private MinHash singatureHash;
	
	public MinHashExtractor(Configuration configuration) {
		super(configuration);
		
		int minhashNum = Integer.getInteger(configuration.getParameter("minhashNum", "32"));
		int singatureNum = Integer.getInteger(configuration.getParameter("singatureNum", "128"));
		
		this.minHash = MinHash.getInstance(1, minhashNum);
		this.singatureHash = MinHash.getInstance(1, singatureNum);
	}

	@Override
	public void process(Item item) {
		String title = item.getTitle();
		if(title != null) {
			try {
				
				title = TextUtils.clean(title);
				title = title.toLowerCase();
				title = TextUtils.normalize(title);
				
				List<String> tokens = TextUtils.tokenize(title);
				TextUtils.cleanTokens(tokens);
				
				title = StringUtils.join(tokens, " ");
				
				byte[] hashdata = minHash.calculate(title);
				byte[] signaturedata = singatureHash.calculate(title);
				
				String minhash = MinHash.toBinaryString(hashdata);
				String signature = MinHash.toBinaryString(signaturedata);
				
				
				item.setMinhash(minhash);
				item.setSignature(signature);
				
			} catch (IOException e) {
				
			}
			
		}
	}

}
