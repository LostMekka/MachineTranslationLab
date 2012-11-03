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
	
	private int[][] bigrams;
	private int wordCount;
	private String locale;

	public LanguageModel(int wordCount, String locale) {
		this.wordCount = wordCount;
		this.locale = locale;
		bigrams = new int[wordCount][wordCount];
	}

	public String getLocale() {
		return locale;
	}

	public void addBigram(int word1, int word2){
		bigrams[word1][word2]++;
	}
	
	public float getBigramProbability(int word1, int word2){
		int sum = 1;
		for(int i=0; i<wordCount; i++){
			sum += bigrams[word1][i];
		}
		return (float)(bigrams[word1][word2] + 1) / (float)sum;
	}

	@Override
	public String getFileName(String base) {
		return base + "." + locale + ".langMod";
	}
	
}
