/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

/**
 *
 * @author LostMekka
 */
public class Dictionary extends Writable {
	
	public static String getFileName(String base, String sourceLocale, String targetLocale){
		return base + "." + sourceLocale + "_to_" + targetLocale + ".dict";
	}
	
	private double[][] translations, tmpTrans;
	private double lastDiff;
	private int sourceWordCount, targetWordCount;
	private String sourceLocale, targetLocale;

	public Dictionary(String base, 
			String sourceLocale, String targetLocale, 
			int sourceWordCount, int targetWordCount){
		super(base);
		this.sourceWordCount = sourceWordCount;
		this.targetWordCount = targetWordCount;
		this.sourceLocale = sourceLocale;
		this.targetLocale = targetLocale;
		translations = new double[sourceWordCount][targetWordCount];
		tmpTrans = new double[sourceWordCount][targetWordCount];
		tmpTrans = null;
		for(int s=0; s<sourceWordCount; s++){
			for(int t=0; t<targetWordCount; t++){
				translations[s][t] = 1d;
			}
		}
		lastDiff = -1d;
	}
	
	public String getSourceLocale() {
		return sourceLocale;
	}

	public String getTargetLocale() {
		return targetLocale;
	}

	public double iter(int[][] sourceSentences, int[][] targetSentences){
		tmpTrans = new double[sourceWordCount][targetWordCount];
		for(int s=0; s<sourceWordCount; s++){
			for(int t=0; t<targetWordCount; t++){
				tmpTrans[s][t] = 0f;
			}
		}
		for(int si=0; si<sourceSentences.length; si++){
			for(int sw : sourceSentences[si]){
				double skalar = 0d;
				for(int tw : targetSentences[si]) skalar += translations[sw][tw];
				for(int tw : targetSentences[si]) tmpTrans[sw][tw] += translations[sw][tw] / skalar;
				}
		}
		// normalize tmp
		for(int tw=0; tw<targetWordCount; tw++){
			double sum = 1d;
			for(int sw=0; sw<sourceWordCount; sw++) sum += tmpTrans[sw][tw];
			for(int sw=0; sw<sourceWordCount; sw++) tmpTrans[sw][tw] = tmpTrans[sw][tw] / sum;
		}
		// take over values and calc diff
		lastDiff = 0d;
		for(int sw=0; sw<sourceWordCount; sw++){
			for(int tw=0; tw<targetWordCount; tw++){
				lastDiff += Math.abs(translations[sw][tw] - tmpTrans[sw][tw]);
				translations[sw][tw] = tmpTrans[sw][tw];
			}
		}
		tmpTrans = null;
		return lastDiff;
	}

	public int getBestTranslation(int sourceWord){
		int ans = 0;
		double bestScore = translations[sourceWord][0];
		for(int i=1; i<translations[sourceWord].length; i++){
			if(bestScore < translations[sourceWord][i]){
				ans = i;
				bestScore = translations[sourceWord][i];
			}
		}
		return ans;
	}
	
	public int[] getBestTranslations(int sourceWord, int transCount){
		int totalTransCount = translations[sourceWord].length;
		if(totalTransCount < transCount) transCount = totalTransCount;
		int[] bestTranslation = new int[transCount];
		double[] bestScores = new double[transCount];
		for(int i=0; i<transCount; i++){
			bestTranslation[i] = -1;
			bestScores[i] = -1f;
		}
		for(int i=0; i<totalTransCount; i++){
			double s = translations[sourceWord][i];
			for(int a=0; a<transCount; a++){
				if(s > bestScores[a]){
					for(int a2=transCount-1; a2>a; a2--){
						bestTranslation[a2] = bestTranslation[a2-1];
						bestScores[a2] = bestScores[a2-1];
					}
					bestTranslation[a] = i;
					bestScores[a] = s;
					break;
				}
			}
		}
		return bestTranslation;
	}
	
	public double getTranslationScore(int sourceWord, int targetWord){
		return translations[sourceWord][targetWord];
	}
	
	public double[] getTranslationScores(int sourceWord, int[] targetWords){
		double[] ans = new double[targetWords.length];
		for(int i=0; i<targetWords.length; i++){
			ans[i] = translations[sourceWord][targetWords[i]];
		}
		return ans;
	}
	
}
