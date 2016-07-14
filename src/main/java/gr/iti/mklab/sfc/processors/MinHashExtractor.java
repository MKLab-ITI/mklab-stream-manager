package gr.iti.mklab.sfc.processors;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gr.iti.mklab.framework.common.domain.Item;
import gr.iti.mklab.framework.common.domain.config.Configuration;
import gr.iti.mklab.sfc.utils.MinHash;
import gr.iti.mklab.sfc.utils.TextUtils;

public class MinHashExtractor extends Processor {
	
	private Logger logger = LogManager.getLogger(MinHashExtractor.class);
	
	private MinHash minHash = null;
	private MinHash singatureHash = null;
	
	public MinHashExtractor(Configuration configuration) {
		super(configuration);
		
		logger.info("minhash length: " + configuration.getParameter("minhashNum", "32") + ", singature length: " + configuration.getParameter("singatureNum", "128"));
		
		int minhashNum = Integer.parseInt(configuration.getParameter("minhashNum", "32"));
		int singatureNum = Integer.parseInt(configuration.getParameter("singatureNum", "128"));
		
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
				
				String minhash = MinHash.toString(hashdata);
				String signature = MinHash.toString(signaturedata);
				
				item.setMinhash(minhash);
				item.setSignature(signature);
				
				item.setCleanTitle(title);
				
			} catch (IOException e) {
				logger.error("Exception in minhash extractor for items " + item.getId(), e);
			}
			
		}
	}

}
