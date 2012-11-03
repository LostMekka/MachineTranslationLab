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
	
	private Thread shutdownThread;
	private float[][] translations;
	private float lastDiff;
	private int sourceWordCount, targetWordCount;
	private String sourceLocale, targetLocale;

	public Dictionary(int sourceWordCount, int targetWordCount, String sourceLocale, String targetLocale) {
		this.sourceWordCount = sourceWordCount;
		this.targetWordCount = targetWordCount;
		this.sourceLocale = sourceLocale;
		this.targetLocale = targetLocale;
		translations = new float[sourceWordCount][targetWordCount];
		for(int s=0; s<sourceWordCount; s++){
			for(int t=0; t<targetWordCount; t++){
				translations[s][t] = 1f;
			}
		}
		lastDiff = -1f;
	}

	public String getSourceLocale() {
		return sourceLocale;
	}

	public String getTargetLocale() {
		return targetLocale;
	}

	public float iter(int[][] sourceSentences, int[][] targetSentences){
		float[][] tmpTrans = new float[sourceWordCount][targetWordCount];
		for(int s=0; s<sourceWordCount; s++){
			for(int t=0; t<targetWordCount; t++){
				tmpTrans[s][t] = 0f;
			}
		}
		for(int si=0; si<sourceSentences.length; si++){
			for(int swi=0; swi<sourceSentences[si].length; swi++){
				int sourceWord = sourceSentences[si][swi];
				float skalar = 0f;
				for(int twi=0; twi<targetSentences[si].length; twi++){
					int targetWord = targetSentences[si][twi];
					skalar += translations[sourceWord][targetWord];
				}
				if(skalar == 0f){
					System.out.print("");
				}
				skalar = 1/skalar;
				for(int twi=0; twi<targetSentences[si].length; twi++){
					int targetWord = targetSentences[si][twi];
					tmpTrans[swi][twi] += translations[sourceWord][targetWord] * skalar;
				}
			}
		}
		// normalize tmp
		for(int sourceWord=0; sourceWord<sourceWordCount; sourceWord++){
			float sum = 0f;
			for(int targetWord=0; targetWord<targetWordCount; targetWord++){
				sum += tmpTrans[sourceWord][targetWord];
			}
			for(int targetWord=0; targetWord<targetWordCount; targetWord++){
				tmpTrans[sourceWord][targetWord] /= sum;
			}
		}
		// take over values and calc diff
		lastDiff = 0f;
		for(int sourceWord=0; sourceWord<sourceWordCount; sourceWord++){
			for(int targetWord=0; targetWord<targetWordCount; targetWord++){
				lastDiff += Math.abs(translations[sourceWord][targetWord] - tmpTrans[sourceWord][targetWord]);
				translations[sourceWord][targetWord] = tmpTrans[sourceWord][targetWord];
			}
		}
		return lastDiff;
	}

	public int getBestTranslation(int sourceWord){
		float[] allTranslations = translations[sourceWord];
		int ans = 0;
		float bestScore = allTranslations[0];
		for(int i=1; i<allTranslations.length; i++){
			if(bestScore < allTranslations[i]){
				ans = i;
				bestScore = allTranslations[i];
			}
		}
		return ans;
	}
	
	public int[] getBestTranslations(int sourceWord, int translationCount){
		int transCount = translations[sourceWord].length;
		if(transCount < translationCount) translationCount = transCount;
		int[] bestTranslation = new int[translationCount];
		float[] bestScores = new float[translationCount];
		for(int i=0; i<translationCount; i++){
			bestTranslation[i] = i;
			bestScores[i] = translations[sourceWord][i];
		}
		for(int i=translationCount; i<transCount; i++){
			float s = translations[sourceWord][i];
			for(int a=0; a<translationCount; a++){
				if(s > bestScores[a]){
					for(int a2=translationCount-1; a2>a; a2--){
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
	
	public float getTranslationScore(int sourceWord, int targetWord){
		return translations[sourceWord][targetWord];
	}
	
	public float[] getTranslationScores(int sourceWord, int[] targetWords){
		float[] ans = new float[targetWords.length];
		for(int i=0; i<targetWords.length; i++){
			ans[i] = translations[sourceWord][targetWords[i]];
		}
		return ans;
	}
	
	@Override
	public String getFileName(String base) {
		return base + "." + sourceLocale + "_to_" + targetLocale + ".dict";
	}
	
}
