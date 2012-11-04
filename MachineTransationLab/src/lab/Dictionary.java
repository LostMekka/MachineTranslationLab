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
				skalar = 1f/skalar;
				for(int twi=0; twi<targetSentences[si].length; twi++){
					int targetWord = targetSentences[si][twi];
					tmpTrans[sourceWord][targetWord] += translations[sourceWord][targetWord] * skalar;
				}
			}
		}
		// normalize tmp
		for(int t=0; t<targetWordCount; t++){
			float sum = 0f;
			for(int s=0; s<sourceWordCount; s++){
				sum += tmpTrans[s][t];
			}
			for(int s=0; s<targetWordCount; s++){
				tmpTrans[s][t] /= sum;
			}
		}
//		for(int s=0; s<sourceWordCount; s++){
//			float sum = 0f;
//			for(int t=0; t<targetWordCount; t++){
//				sum += tmpTrans[s][t];
//			}
//			for(int t=0; t<targetWordCount; t++){
//				tmpTrans[s][t] /= sum;
//			}
//		}
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
		int ans = 0;
		float bestScore = translations[sourceWord][0];
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
		float[] bestScores = new float[transCount];
		for(int i=0; i<transCount; i++){
			bestTranslation[i] = -1;
			bestScores[i] = -1f;
		}
		for(int i=0; i<totalTransCount; i++){
			float s = translations[sourceWord][i];
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
