/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

import java.util.Arrays;


/**
 *
 * @author LostMekka
 */
public class StackSentence implements Comparable<StackSentence> {
	
	public int[] words;
	public double score;

	public StackSentence(
			StackSentence parent, int newWord, int[] sourceSentence, 
			Dictionary dict, LanguageModel langMod, LengthModel lenMod) {
		if(parent == null){
			words = new int[1];
		} else {
			words = new int[parent.words.length + 1];
			System.arraycopy(parent.words, 0, words, 0, parent.words.length);
		}
		words[words.length - 1] = newWord;
		// TODO: calc score from previous score
		double bigramScore = 1f;
		for(int i=0; i<words.length-1; i++){
			bigramScore *= langMod.getBigramProbability(words[i], words[i+1]);
		}
		double lengthScore = lenMod.getLengthPairProbability(words.length, sourceSentence.length);
		
		double dictScore = 1f;
		for(int f=0; f<sourceSentence.length; f++){
			if(sourceSentence[f] < 0) continue;
			double targetWordScore = 0.001f;
			for(int e=0; e<words.length; e++){
				if(words[e] < 0) continue;
				targetWordScore += dict.getTranslationScore(sourceSentence[f], words[e]);
			}
			dictScore *= targetWordScore;
		}
		
		score = bigramScore * lengthScore * dictScore * (double)(Math.pow(sourceSentence.length, words.length));
	}

	@Override
	public int compareTo(StackSentence o) {
		if(score > o.score) return 1;
		if(Arrays.equals(words, o.words)) return 0;
		return -1;
	}

	public String toStringWithStorage(WordStorage wordStorage){
		String line = "";
		for(int i=0; i<words.length; i++){
			switch(words[i]){
				case LanguageModel.SENTENCE_BEGIN_WORD: line = line + "#># "; break;
				case LanguageModel.SENTENCE_END_WORD: line = line + "#<# "; break;
				default: line = line + wordStorage.getWord(words[i]) + " "; break;
			}
		}
		return line + " : " + score;
	}
	
	@Override
	public String toString() {
		String ans = "";
		for(int i=0; i<words.length; i++){
			ans = ans + words[i] + " ";
		}
		return ans + ": " + score;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + Arrays.hashCode(this.words);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final StackSentence other = (StackSentence) obj;
		return Arrays.equals(this.words, other.words);
	}

}
