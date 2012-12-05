/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

/**
 *
 * @author LostMekka
 */
public class Corpus extends Writable {
	
	public static String getFileName(String base, String locale) {
		return base + "." + locale + ".corpDat";
	}
	
	private String locale;
	private int[][] sentences;
	private int wordCount, maxSentenceLength;

	public Corpus(String base, String locale) {
		super(base);
		this.locale = locale;
	}
	
	public Corpus(String base, String locale, int[][] sentences, int wordCount, int maxSentenceLength) {
		super(base);
		this.locale = locale;
		this.sentences = sentences;
		this.wordCount = wordCount;
		this.maxSentenceLength = maxSentenceLength;
	}
	
	public String getLocale() {
		return locale;
	}

	public int[][] getSentences() {
		return sentences;
	}

	public int getSentenceCount() {
		return sentences.length;
	}

	public int getWordCount() {
		return wordCount;
	}
	
	public int getMaxSentenceLength() {
		return maxSentenceLength;
	}

}
