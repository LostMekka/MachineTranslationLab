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
	
	public static String getFileName(String base, String sourceLocale, String targetLocale) {
		return base + "." + sourceLocale + "_to_" + targetLocale + ".lenMod";
	}
	
	private int[][] lengthPairs;
	private String sourceLocale, targetLocale;

	public LengthModel(String base, 
			String sourceLocale, String targetLocale, 
			int sourceMaxSentenceLenght, int targetMaxSentenceLenght){
		super(base);
		this.sourceLocale = sourceLocale;
		this.targetLocale = targetLocale;
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
		for(int i=0; i<lengthPairs[sourceLength-1].length; i++){
			sum += lengthPairs[sourceLength-1][i];
		}
		return (float)(lengthPairs[sourceLength-1][targetLength-1] + 1) / (float)sum;
	}

	public int getHighestPossibleTargetLength(int sourceLength){
		for(int l=lengthPairs[sourceLength-1].length-1; l>0; l--){
			if(lengthPairs[sourceLength][l] > 0) return l + 1;
		}
		return 1;
	}
	
}
