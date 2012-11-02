/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

import java.io.Serializable;

/**
 *
 * @author LostMekka
 */
public class LanguageModel implements Serializable {
	
	private int[][] bigrams;
	private int wordCount;
	private WordStorage wordStorage;

	public LanguageModel(WordStorage wordStorage) {
		this.wordStorage = wordStorage;
		wordCount = wordStorage.getWordCount();
		bigrams = new int[wordCount][wordCount];
	}

	public String getLocale() {
		return wordStorage.getLocale();
	}

	public void addBigram(String word1, String word2){
		int i1 = wordStorage.getIndex(word1);
		int i2 = wordStorage.getIndex(word2);
		if(i1 < 0) throw new RuntimeException("word \"" + word1 + "\" not recognized!");
		if(i2 < 0) throw new RuntimeException("word \"" + word2 + "\" not recognized!");
		bigrams[i1][i2]++;
	}
	
	public float getBigramProbability(String word1, String word2){
		int i1 = wordStorage.getIndex(word1);
		int i2 = wordStorage.getIndex(word2);
		if(i1 < 0) throw new RuntimeException("word \"" + word1 + "\" not recognized!");
		if(i2 < 0) throw new RuntimeException("word \"" + word2 + "\" not recognized!");
		int sum = 1;
		for(int i=0; i<wordCount; i++){
			sum += bigrams[i1][i];
		}
		return (float)(bigrams[i1][i2] + 1) / (float)sum;
	}
	
}
