/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author LostMekka
 */
public class WordStorage implements Serializable {
	
	private HashMap<String, Integer> words = new HashMap<>();
	private int wordCount = 0;
	private boolean finalized = false;
	private String locale;

	public WordStorage(String locale) {
		this.locale = locale;
	}

	public String getLocale() {
		return locale;
	}
	
	public int getIndex(String word){
		Integer ans = words.get(word);
		if(ans == null){
			return -1;
		} else {
			return ans;
		}
	}
	
	public int addWord(String word){
		if(finalized) throw new RuntimeException("cannot add word \"" + word + "\", word storage is finalized!");
		Integer ans = words.get(word);
		if(ans == null){
			words.put(word, wordCount);
			wordCount++;
			return wordCount - 1;
		} else {
			return ans;
		}
	}
	
	public int getWordCount(){
		return wordCount;
	}

	public boolean isFinalized() {
		return finalized;
	}

	public void finalizeStorage() {
		finalized = true;
	}
	
}
