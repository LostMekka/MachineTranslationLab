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
public class LengthModel implements Serializable {
	
	private int[][] lengthPairs;
	private String sourceLocale, targetLocale;
	private int sourceMaxSentenceLenght, targetMaxSentenceLenght;

	public LengthModel(String sourceLocale, String targetLocale, int sourceMaxSentenceLenght, int targetMaxSentenceLenght) {
		this.sourceLocale = sourceLocale;
		this.targetLocale = targetLocale;
		this.sourceMaxSentenceLenght = sourceMaxSentenceLenght;
		this.targetMaxSentenceLenght = targetMaxSentenceLenght;
		lengthPairs = new int[sourceMaxSentenceLenght][targetMaxSentenceLenght];
	}

	public String getSourceLocale() {
		return sourceLocale;
	}

	public String getTargetLocale() {
		return targetLocale;
	}

	public void addLenghtPair(int sourceLength, int targetLength){
		lengthPairs[sourceLength][targetLength]++;
	}
	
	public float getLengthPairProbability(int sourceLength, int targetLength){
		int sum = 1;
		for(int i=0; i<targetMaxSentenceLenght; i++){
			sum += lengthPairs[sourceLength][i];
		}
		return (float)(lengthPairs[sourceLength][targetLength] + 1) / (float)sum;
	}
	
}
