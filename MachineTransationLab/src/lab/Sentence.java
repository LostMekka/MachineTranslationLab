/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author LostMekka
 */
public final class Sentence{
	
	private static final Random random = new Random();
	
	public int[] sentence;
	public double score;
	
	private DecodeHelper helper;

	/**
	 * copies an existing sentence
	 */
	public Sentence(Sentence s) {
		helper = s.helper;
		sentence = s.sentence.clone();
		score = s.score;
	}
	
	/**
	 * creates a new fixed sentence
	 */
	public Sentence(int[] sentence, DecodeHelper helper) {
		this.helper = helper;
		this.sentence = sentence;
		recalculateScore();
	}
	
	/**
	 * creates a new random sentence
	 */
	public Sentence(DecodeHelper helper) {
		this(helper, helper.getWeightedRandomSentenceLength());
	}
	
	/**
	 * creates a new random sentence with fixed length
	 */
	public Sentence(DecodeHelper helper, int length) {
		this.helper = helper;
		// create sentence
		sentence = new int[length];
		for(int i=0; i<sentence.length; i++) sentence[i] = helper.getWeightedRandomWord();
		// score
		recalculateScore();
	}
	
	/**
	 * creates a new sentence offspring based off the two parent sentence and mutates it
	 */
	public Sentence(Sentence parent1, Sentence parent2, int mutationCount) {
		helper = parent1.helper;
		// create sentence
		int l1 = parent1.sentence.length;
		int l2 = parent2.sentence.length;
		if((l1 <= 0) || (l2 <= 0)){
			int i = 0;
		}
		int lmax = l1;
		if(l2 > lmax) lmax = l2;
		ArrayList<Integer> sentenceArray = new ArrayList<>(lmax);
		boolean p1isDominant = random.nextBoolean();
		int loc = 0;
		for(;;){
			// choose parent
			int[] s;
			if(p1isDominant){
				s = parent1.sentence;
			} else {
				s = parent2.sentence;
			}
			// choose length of dominant part
			int len = 1;
			if(lmax > 1) len += random.nextInt(lmax - 1);
			if(loc + len > s.length) len = s.length - loc;
			// write dominant part
			for(int i=0; i<len; i++) sentenceArray.add(s[loc + i]);
			loc += len;
			// if dominant parent has reached its end, we are done.
			if(loc >= s.length) break;
			// swap dominant parent for next dominant part
			p1isDominant = !p1isDominant;
		}
		// convert arraylist to array
		sentence = new int[sentenceArray.size()];
		for(int i=0; i<sentenceArray.size(); i++){
			sentence[i] = sentenceArray.get(i);
		}
		// mutate
		for(int i=0; i<mutationCount; i++) mutate();
		// score
		recalculateScore();
	}
	
	public void recalculateScore(){
		score = helper.getScore(sentence);
	}

	public void mutateOrdering(){
		if(sentence.length < 2) return;
		int pos1 = random.nextInt(sentence.length);
		int pos2 = random.nextInt(sentence.length - 1);
		if(pos1 >= pos2) pos2++;
		mutateOrdering(pos1, pos2);
	}
	
	public void mutateOrdering(int pos1){
		if(sentence.length < 2) return;
		int pos2 = random.nextInt(sentence.length - 1);
		if(pos1 >= pos2) pos2++;
		mutateOrdering(pos1, random.nextInt(sentence.length));
	}
	
	public void mutateOrdering(int pos1, int pos2){
		int tmp = sentence[pos1];
		sentence[pos1] = sentence[pos2];
		sentence[pos2] = tmp;
	}
	
	public void mutateWord(){
		if(sentence.length <= 0) return;
		mutateWord(random.nextInt(sentence.length));
	}
	
	public void mutateWord(int pos){
		sentence[pos] = helper.getWeightedRandomWord();
	}
	
	public void deleteWord(){
		if(sentence.length <= 1) return;
		deleteWord(random.nextInt(sentence.length));
	}
	
	public void deleteWord(int pos){
		if(sentence.length <= 1) return;
		int[] newSentence = new int[sentence.length - 1];
		System.arraycopy(sentence, 0, newSentence, 0, pos);
		System.arraycopy(sentence, pos + 1, newSentence, pos, sentence.length - pos - 1);
		sentence = newSentence;
	}
	
	public void addWord(){
		addWord(random.nextInt(sentence.length), helper.getWeightedRandomWord());
	}
	
	public void addWord(int pos){
		addWord(pos, helper.getWeightedRandomWord());
	}
	
	public void addWord(int pos, int word){
		int[] newSentence = new int[sentence.length + 1];
		System.arraycopy(sentence, 0, newSentence, 0, pos);
		newSentence[pos] = word;
		System.arraycopy(sentence, pos, newSentence, pos + 1, sentence.length - pos);
		sentence = newSentence;
	}
	
	public void mutate(){
		switch(random.nextInt(4)){
			case 0: mutateOrdering(); break;
			case 1: mutateWord(); break;
			case 2: deleteWord(); break;
			case 3: addWord(); break;
		}
	}
	
}
