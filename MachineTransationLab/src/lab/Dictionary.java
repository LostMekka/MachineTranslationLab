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
		for(int sentence=0; sentence<sourceSentences.length; sentence++){
			for(int sourceWord=0; sourceWord<sourceSentences[sentence].length; sourceWord++){
				float skalar = 0f;
				for(int targetWord=0; targetWord<targetSentences[sentence].length; targetWord++){
					skalar += translations[sourceWord][targetWord];
				}
				for(int targetWord=0; targetWord<targetSentences[sentence].length; targetWord++){
					tmpTrans[sourceWord][targetWord] += translations[sourceWord][targetWord] / skalar;
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

	@Override
	public String getFileName(String base) {
		return base + "." + sourceLocale + "_to_" + targetLocale + ".dict";
	}
	
}
