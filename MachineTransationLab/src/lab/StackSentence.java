/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;


/**
 *
 * @author LostMekka
 */
public class StackSentence {
	
	public int[] words;
	public float score;		

	public StackSentence(
			StackSentence parent, int newWord, int[] sourceSentence, 
			Dictionary dict, LanguageModel langMod, LengthModel lenMod) {
		words = new int[parent.words.length + 1];
		System.arraycopy(parent.words, 0, words, 0, parent.words.length);
		float bigramScore = 1f;
		for(int i=0; i<words.length-1; i++){
			bigramScore *= langMod.getBigramProbability(words[i], words[i+1]);
		}
	}
	
	
}
