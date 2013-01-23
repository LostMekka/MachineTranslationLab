/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

import java.util.ArrayList;

/**
 *
 * @author LostMekka
 */
public class WordStorage extends Writable {
	
	public static String getFileName(String base, String locale) {
		return base + "." + locale + ".words";
	}
	
	private ArrayList<String> words = new ArrayList<>();
	private int wordCount = 0;
	private boolean finalized = false;
	private String locale;

	public WordStorage(String base, String locale) {
		super(base);
		this.locale = locale;
	}

	public String getLocale() {
		return locale;
	}
	
	public String[] getStrings(int[] indices){
		String[] ans = new String[indices.length];
		for(int i=0; i<indices.length; i++) ans[i] = getWord(indices[i]);
		return ans;
	}
	
	public String getString(int[] indices){
		String ans = "";
		for(int i=0; i<indices.length; i++){
			if(i > 0) ans += " ";
			ans += getWord(indices[i]);
		}
		return ans;
	}
	
	public String getWord(int index){
		return words.get(index);
	}
	
	public int getIndex(String word){
		for(int i=0; i<wordCount; i++){
			if(words.get(i).equalsIgnoreCase(word)) return i;
		}
		return -1;
	}
	
	public int addWord(String word){
		if(finalized) throw new RuntimeException("cannot add word \"" + word + "\", word storage is finalized!");
		for(int i=0; i<wordCount; i++){
			if(words.get(i).equalsIgnoreCase(word)) return i;
		}
		words.add(word);
		wordCount++;
		return wordCount-1;
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
