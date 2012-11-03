/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

/**
 *
 * @author LostMekka
 */
public class LengthModel extends Writable {
	
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
		lengthPairs[sourceLength-1][targetLength-1]++;
	}
	
	public float getLengthPairProbability(int sourceLength, int targetLength){
		int sum = 1;
		for(int i=0; i<targetMaxSentenceLenght; i++){
			sum += lengthPairs[sourceLength-1][i];
		}
		return (float)(lengthPairs[sourceLength-1][targetLength-1] + 1) / (float)sum;
	}

	@Override
	public String getFileName(String base) {
		return base + "." + sourceLocale + "_to_" + targetLocale + ".lenMod";
	}
	
}
