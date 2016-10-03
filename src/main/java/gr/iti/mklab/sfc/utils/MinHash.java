package gr.iti.mklab.sfc.utils;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

public class MinHash {

    private Analyzer analyzer;

	private MinHash() {
    	
    }

	public static MinHash getInstance(int hashBit, int num) {
		MinHash mh = new MinHash();
		mh.analyzer = new MinHashAnalyzer (hashBit, 0, num);
		
		return mh;
    }
	
	
    /**
     * Compare base64 strings for MinHash.
     *
     * @param numOfBits The number of MinHash bits
     * @param str1 MinHash base64 string
     * @param str2 MinHash base64 string
     * 
     * @return similarity (0 to 1.0f)
     */
    public static float compare(final int numOfBits, final String str1, final String str2) {
        return compare(numOfBits, BaseEncoding.base64().decode(str1), BaseEncoding.base64().decode(str2));
    }

    /**
     * Compare base64 strings for MinHash.
     *
     * @param str1 MinHash base64 string
     * @param str2 MinHash base64 string
     * 
     * @return similarity (0 to 1.0f)
     */
    public static float compare(final String str1, final String str2) {
        return compare(BaseEncoding.base64().decode(str1), BaseEncoding.base64().decode(str2));
    }

    /**
     * Compare bytes for MinHash.
     *
     * @param data1 MinHash bytes
     * @param data2 MinHash bytes
     * @return similarity (0 to 1.0f)
     */
    public static float compare(final byte[] data1, final byte[] data2) {
        return compare(data1.length * 8, data1, data2);
    }

    /**
     * Compare bytes for MinHash.
     *
     * @param numOfBits The number of MinHash bits
     * @param data1 MinHash bytes
     * @param data2 MinHash bytes
     * @return similarity (0 to 1.0f)
     */
    public static float compare(final int numOfBits, final byte[] data1, final byte[] data2) {
    	
        if (data1.length != data2.length) {
            return 0;
        }
        
        final int count = countSameBits(data1, data2);
        return (float) count / (float) numOfBits;
    }

    protected static int countSameBits(final byte[] data1, final byte[] data2) {
        int count = 0;
        for (int i = 0; i < data1.length; i++) {
            byte b1 = data1[i];
            byte b2 = data2[i];
            for (int j = 0; j < 8; j++) {
                if ((b1 & 1) == (b2 & 1)) {
                    count++;
                }
                b1 >>= 1;
                b2 >>= 1;
            }
        }
        return count;
    }

    /**
     * Create hash functions.
     * 
     * @param seed a base seed
     * @param num the number of hash functions.
     * @return
     */
    public static HashFunction[] createHashFunctions(final int seed, final int num) {
        final HashFunction[] hashFunctions = new HashFunction[num];
        for (int i = 0; i < num; i++) {
            hashFunctions[i] = Hashing.murmur3_128(seed + i);
        }
        
        return hashFunctions;
    }

    /**
     * Calculates MinHash value.
     * 
     * @param text a target text
     * @return MinHash value
     * @throws IOException
     */
    public byte[] calculate(final String text) throws IOException {
        byte[] value = null;
        try (TokenStream stream = analyzer.tokenStream("minhash", text)) {
            final CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
            stream.reset();
            if (stream.incrementToken()) {
                final String minhashValue = termAtt.toString();
                value = BaseEncoding.base64().decode(minhashValue);
            }
            stream.end();
        }
        return value;
    }
    
    /**
     * Calculates MinHash value.
     * 
     * @param analyzer analyzer to parse a text
     * @param text a target text
     * @return MinHash value
     * @throws IOException
     */
    public static byte[] calculate(final Analyzer analyzer, final String text) throws IOException {
        byte[] value = null;
        try (TokenStream stream = analyzer.tokenStream("minhash", text)) {
            final CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
            stream.reset();
            if (stream.incrementToken()) {
                final String minhashValue = termAtt.toString();
                value = BaseEncoding.base64().decode(minhashValue);
            }
            stream.end();
        }
        return value;
    }

    /**
     * Calculates MinHash value.
     * 
     * @param data data with analyzer, text and the number of bits
     * @return MinHash value
     * @throws IOException
     */
    public static byte[] calculate(final Data data) throws IOException {
        return calculate(data.analyzer, data.text);
    }

    /**
     * Calculates MinHash value.
     * 
     * @param data data with analyzer, text and the number of bits
     * @return MinHash value
     * @throws IOException
     */
    public static byte[] calculate(final Data[] data) throws IOException {
        int bitSize = 0;
        for (final Data target : data) {
            bitSize += target.numOfBits;
        }
        int pos = 0;
        final BitSet bitSet = new BitSet(bitSize);
        for (final Data target : data) {
            int count = 0;
            final byte[] bytes = calculate(target);
            for (final byte b : bytes) {
                byte bits = b;
                for (int j = 0; j < 8; j++) {
                    bitSet.set(pos, (bits & 0x1) == 0x1);
                    pos++;
                    count++;
                    if (count >= target.numOfBits) {
                        break;
                    }
                    bits >>= 1;
                }
            }
        }
        return bitSet.toByteArray();
    }

    /**
     * Returns a string formatted by bits.
     * 
     * @param data
     * @return
     */
    public static String toBinaryString(final byte[] data) {
        if (data == null) {
            return null;
        }
        final StringBuilder buf = new StringBuilder(data.length * 8);
        for (final byte element : data) {
            byte bits = element;
            for (int j = 0; j < 8; j++) {
                if ((bits & 0x80) == 0x80) {
                    buf.append('1');
                } else {
                    buf.append('0');
                }
                bits <<= 1;
            }
            buf.append(' ');
        }
        return buf.toString();
    }

    /**
     * Returns a string formatted by bits.
     * 
     * @param data
     * @return
     */
    public static String toString(final byte[] data) {
        if (data == null) {
            return null;
        }
        
        return BaseEncoding.base64().encode(data);
    }
    
    /**
     * Count the number of true bits.
     * 
     * @param data a target data
     * @return the number of true bits
     */
    public static int bitCount(final byte[] data) {
        int count = 0;
        for (final byte element : data) {
            byte bits = element;
            for (int j = 0; j < 8; j++) {
                if ((bits & 1) == 1) {
                    count++;
                }
                bits >>= 1;
            }
        }
        return count;
    }

    /**
     * Create a target data which has analyzer, text and the number of bits.
     * 
     * @param analyzer
     * @param text
     * @param numOfBits
     * @return
     */
    public static Data newData(final Analyzer analyzer, final String text, final int numOfBits) {
        return new Data(analyzer, text, numOfBits);
    }

    public static class Data {
        final int numOfBits;

        final String text;

        final Analyzer analyzer;

        Data(final Analyzer analyzer, final String text, final int numOfBits) {
            this.numOfBits = numOfBits;
            this.text = text;
            this.analyzer = analyzer;
        }
    }
    
    public static class BitSet {
    	
        final byte[] data;

        final int nbit;

        public BitSet(final int nbit) {
            this.nbit = nbit;
            if (nbit == 0) {
                throw new IllegalArgumentException("nbit is above 0.");
            }

            data = new byte[(nbit - 1) / 8 + 1];
        }

        public void set(final int bitIndex, final boolean value) {
            final int bytePos = bitIndex / 8;
            final int bitPos = bitIndex % 8;

            if (bytePos >= data.length) {
                return;
            }

            switch (bitPos) {
            case 0:
                data[bytePos] = (byte) (data[bytePos] & 0xfe);
                if (value) {
                    data[bytePos] = (byte) (data[bytePos] | 0x01);
                }
                break;
            case 1:
                data[bytePos] = (byte) (data[bytePos] & 0xfd);
                if (value) {
                    data[bytePos] = (byte) (data[bytePos] | 0x02);
                }
                break;
            case 2:
                data[bytePos] = (byte) (data[bytePos] & 0xfb);
                if (value) {
                    data[bytePos] = (byte) (data[bytePos] | 0x04);
                }
                break;
            case 3:
                data[bytePos] = (byte) (data[bytePos] & 0xf7);
                if (value) {
                    data[bytePos] = (byte) (data[bytePos] | 0x08);
                }
                break;
            case 4:
                data[bytePos] = (byte) (data[bytePos] & 0xef);
                if (value) {
                    data[bytePos] = (byte) (data[bytePos] | 0x10);
                }
                break;
            case 5:
                data[bytePos] = (byte) (data[bytePos] & 0xdf);
                if (value) {
                    data[bytePos] = (byte) (data[bytePos] | 0x20);
                }
                break;
            case 6:
                data[bytePos] = (byte) (data[bytePos] & 0xbf);
                if (value) {
                    data[bytePos] = (byte) (data[bytePos] | 0x40);
                }
                break;
            case 7:
                data[bytePos] = (byte) (data[bytePos] & 0x7f);
                if (value) {
                    data[bytePos] = (byte) (data[bytePos] | 0x80);
                }
                break;
            default:
                break;
            }
        }

        public byte[] toByteArray() {
            return data;
        }
    }
    
	public static class MinHashTokenFilter extends TokenFilter {

	    private final CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);
	    private final PositionIncrementAttribute positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);

	    private HashFunction[] hashFunctions;

	    private int hashBit;

	    private long[] minHashValues;

	    private String minHash;

	    public MinHashTokenFilter(final TokenStream input, final HashFunction[] hashFunctions, final int hashBit) {
	        super(input);
	        
	        this.hashFunctions = hashFunctions;
	        this.hashBit = hashBit;
	        this.minHashValues = new long[hashFunctions.length];
	    }

	    @Override
	    public final boolean incrementToken() throws IOException {
	        final int functionsSize = hashFunctions.length;
	        while (input.incrementToken()) {
	            final String term = charTermAttribute.toString();
	            for (int i = 0; i < functionsSize; i++) {
	            	final HashCode hashCode = hashFunctions[i].hashString(term);
	                final long value = hashCode.asLong();
	                if (value < minHashValues[i]) {
	                    minHashValues[i] = value;
	                }
	            }
	        }

	        if (minHash != null) {
	            return false;
	        }

	        minHash = BaseEncoding.base64().encode(calculateMinHash(minHashValues, hashBit));
	        
	        charTermAttribute.setEmpty().append(minHash);
	        positionIncrementAttribute.setPositionIncrement(1);

	        return true;
	    }

	    @Override
	    public void reset() throws IOException {
	        super.reset();
	        Arrays.fill(minHashValues, Long.MAX_VALUE);
	        minHash = null;
	    }

	    protected static byte[] calculateMinHash(final long[] minHashValues, final int hashBit) {
	        final int shift = 1;
	        final int radix = 1 << shift;
	        final long mask = radix - 1;
	        int pos = 0;
	        final int nbits = minHashValues.length * hashBit;
	        final BitSet bitSet = new BitSet(nbits);
	        for (long i : minHashValues) {
	            for (int j = 0; j < hashBit; j++) {
	                bitSet.set(pos, (int) (i & mask) == 1);
	                pos++;
	                i >>>= shift;
	            }
	        }
	        return bitSet.toByteArray();
	    }

	}
	
	public static class MinHashAnalyzer extends Analyzer {

		private HashFunction[] hashFunctions;
		private int hashBit;
		
	    public MinHashAnalyzer (final int hashBit, final int seed, final int num) {
	    	this.hashFunctions = MinHash.createHashFunctions(seed, num);
	    	this.hashBit = hashBit;
	    }

		@Override
		protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
	        final Tokenizer tokenizer = new WhitespaceTokenizer(reader);
	        final TokenStream stream = new MinHashTokenFilter(tokenizer, hashFunctions, hashBit);
	        
	        return new TokenStreamComponents(tokenizer, stream);
		}

	}
}