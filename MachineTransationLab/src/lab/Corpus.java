/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author LostMekka
 */
public class Corpus extends Writable {
	
	final static Charset ENCODING = StandardCharsets.UTF_8;
	
	private String base, locale;
	private int[][] sentences;
	private WordStorage wordStorage = null;
	private int wordCount, sentenceCount, maxSentenceLength;

	public Corpus(String base, String locale) {
		this.base = base;
		this.locale = locale;
	}
	
	public void readFile(){
		if(wordStorage != null) return;
		String fileName = base + "." + locale + ".txt";
		System.out.println("reading corpus file \"" + fileName + "\"...");
		wordCount = 0;
		sentenceCount = 0;
		maxSentenceLength = 0;
		LinkedList<int[]> lines = new LinkedList<>();
		wordStorage = new WordStorage(locale);
		try {
			Path path = Paths.get(fileName);
			try (Scanner scanner = new Scanner(path, ENCODING.name())) {
				while (scanner.hasNextLine()) {
					String[] words = scanner.nextLine().split(" ");
					int[] indexedWords = new int[words.length];
					for(int i=0; i<words.length; i++){
						indexedWords[i] = wordStorage.addWord(words[i]);
					}
					lines.add(indexedWords);
					wordCount += words.length;
					sentenceCount++;
					if(words.length > maxSentenceLength) maxSentenceLength = words.length;
				}
			}
		} catch (Exception ex) {
			Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
		}
		sentences = new int[sentenceCount][];
		int i = 0;
		for(int[] line : lines){
			sentences[i] = line;
			i++;
		}
		System.out.println("done reading. sentences: " + sentenceCount + 
				", max sentence length: " + maxSentenceLength + 
				", words: " + wordCount + 
				", distinct words:" + wordStorage.getWordCount());
	}
	
	public String getBase() {
		return base;
	}

	public String getLocale() {
		return locale;
	}

	public int[][] getSentences() {
		if(wordStorage == null) throw new RuntimeException("readFile not performed yet");
		return sentences;
	}

	public WordStorage getWordStorage() {
		if(wordStorage == null) throw new RuntimeException("readFile not performed yet");
		return wordStorage;
	}

	public int getSentenceCount() {
		if(wordStorage == null) throw new RuntimeException("readFile not performed yet");
		return sentenceCount;
	}

	public int getWordCount() {
		if(wordStorage == null) throw new RuntimeException("readFile not performed yet");
		return wordCount;
	}
	
	public int getDistinctWordCount() {
		if(wordStorage == null) throw new RuntimeException("readFile not performed yet");
		return wordStorage.getWordCount();
	}

	public int getMaxSentenceLength() {
		if(wordStorage == null) throw new RuntimeException("readFile not performed yet");
		return maxSentenceLength;
	}

	@Override
	public String getFileName(String base) {
		return base + "." + locale + ".corpDat";
	}
	
}
