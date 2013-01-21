/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

/**
 *
 * @author LostMekka
 */
public class LanguageModel extends Writable {
	
	public static String getFileName(String base, String locale) {
		return base + "." + locale + ".langMod";
	}
	
	public static final int SENTENCE_BEGIN_WORD = -2;
	public static final int SENTENCE_END_WORD = -1;
	
	private static final int ARRAY_OFFSET = 2;
	
	private int[][] bigrams;
	private int wordCount;
	private String locale;

	public LanguageModel(String base,String locale, int wordCount) {
		super(base);
		this.wordCount = wordCount;
		this.locale = locale;
		bigrams = new int[wordCount + ARRAY_OFFSET][wordCount + ARRAY_OFFSET];
	}

	public String getLocale() {
		return locale;
	}

	public void addBigram(int word1, int word2){
		bigrams[word1 + ARRAY_OFFSET][word2 + ARRAY_OFFSET]++;
	}
	
	public double getBigramProbability(int word1, int word2){
		int sum = 1;
		for(int i=0; i<wordCount+ARRAY_OFFSET; i++){
			sum += bigrams[word1 + ARRAY_OFFSET][i];
		}
		return (double)(bigrams[word1 + ARRAY_OFFSET][word2 + ARRAY_OFFSET] + 1) / (double)sum;
	}

}
