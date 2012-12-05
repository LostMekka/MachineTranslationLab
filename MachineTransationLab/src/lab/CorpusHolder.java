/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

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
public class CorpusHolder {
	
	private final static Charset ENCODING = StandardCharsets.UTF_8;
	
	private Corpus corpus;
	private WordStorage storage;

	public Corpus getCorpus() {
		return corpus;
	}

	public WordStorage getStorage() {
		return storage;
	}

	public CorpusHolder(String base, String locale) {
		storage = new WordStorage(base, locale);
		String fileName = base + "." + locale + ".txt";
		System.out.println("reading corpus file \"" + fileName + "\"...");
		int wordCount = 0;
		int sentenceCount = 0;
		int maxSentenceLength = 0;
		LinkedList<int[]> lines = new LinkedList<>();
		try {
			Path path = Paths.get(fileName);
			try (Scanner scanner = new Scanner(path, ENCODING.name())) {
				while (scanner.hasNextLine()) {
					String[] words = scanner.nextLine().split(" ");
					int[] indexedWords = new int[words.length];
					for(int i=0; i<words.length; i++){
						indexedWords[i] = storage.addWord(words[i]);
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
		int[][] sentences = new int[sentenceCount][];
		int i = 0;
		for(int[] line : lines){
			sentences[i] = line;
			i++;
		}
		corpus = new Corpus(base, locale, sentences, wordCount, maxSentenceLength);
		System.out.println("done reading. sentences: " + sentenceCount + 
				", max sentence length: " + maxSentenceLength + 
				", words: " + wordCount + 
				", distinct words:" + storage.getWordCount());
	}
	
}
