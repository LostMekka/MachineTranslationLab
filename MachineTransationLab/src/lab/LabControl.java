/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author LostMekka
 */
public class LabControl {
	
	public static void log(String msg){
		System.out.println(msg);
	}
			
	public static void log() {
		System.out.println();
	}
	
	public static void err(String msg){
		System.err.println(msg);
	}
			
	public static void err() {
		System.err.println();
	}

	public void halt() {
		if(shutdownManager != null) shutdownManager.endShutdownInjection();
		System.exit(1);
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		LabControl control = new LabControl(new ShutdownManager());
		if(args.length < 4){
			control.err("wrong parameter count!");
			control.halt();
		}
		if(args[3].equalsIgnoreCase("train")) control.executeTrainAction(args[0], args[1], args[2]);
		if(args[3].equalsIgnoreCase("resumetrain")) control.executeResumeTrainAction(args[0], args[1], args[2]);
		if(args[3].equalsIgnoreCase("lookup")) control.executeLookupAction(args[0], args[1], args[2]);
		if(args[3].equalsIgnoreCase("decode")) control.executeDecodeAction(args[0], args[1], args[2]);
	}
	
	private ShutdownManager shutdownManager;

	public LabControl(ShutdownManager shutdownManager) {
		this.shutdownManager = shutdownManager;
	}
	
	private void trainLanguageModel(Corpus corpus, LanguageModel model){
		int[][] sentences = corpus.getSentences();
		for(int[] words : sentences){
			for(int i=0; i<words.length-1; i++){
				model.addBigram(words[i], words[i+1]);
			}
			model.addBigram(LanguageModel.SENTENCE_BEGIN_WORD, words[0]);
			model.addBigram(words[words.length-1], LanguageModel.SENTENCE_END_WORD);
		}
	}
	
	private void trainLengthModel(Corpus sourceCorpus, Corpus targetCorpus, LengthModel model){
		int[][] sourceSentences = sourceCorpus.getSentences();
		int[][] targetSentences = targetCorpus.getSentences();
		for(int sentenceIndex=0; sentenceIndex<sourceSentences.length; sentenceIndex++){
			int sl = sourceSentences[sentenceIndex].length;
			int tl = targetSentences[sentenceIndex].length;
			model.addLenghtPair(sl, tl);
		}
	}
	
	private void trainDictionary(Corpus sourceCorpus, Corpus targetCorpus, Dictionary dictionary){
		log("(you can stop the training with ctrl-c and resume later with the \"resumetrain\" command)");
		float delta = 1000f;
		int i = 1;
		while(delta > 0.000001f){
			delta = dictionary.iter(sourceCorpus.getSentences(), targetCorpus.getSentences());
			System.out.format("    iteration %4d - delta = %13.10f\n", i, delta);
			i++;
			if(shutdownManager.isShutdownRequested()) break;
		}
	}
	
	private void lookup(WordStorage sourceWordStorage, WordStorage tagetWordStorage, Dictionary dictionary, String word){
		int sourceWord = sourceWordStorage.getIndex(word);
		if(sourceWord < 0){
			System.err.format("ERROR: \"%s\" is not contained in learned language.\n", word);
			return;
		}
		int[] bestWords = dictionary.getBestTranslations(sourceWord, 10);
		float[] scores = dictionary.getTranslationScores(sourceWord, bestWords);
		System.out.format("best translations for \"%s\":\n", word);
		for(int i=0; i<bestWords.length; i++){
			String targetWord = tagetWordStorage.getWord(bestWords[i]);
			System.out.format("%4d: %10.8f - %s\n", i+1, scores[i], targetWord);
		}
	}
	
	private void addNewStackSentenceToList(SortedSet<StackSentence> list, 
			StackSentence parent, int newWord, int[] sourceSentence, 
			Dictionary dict, LanguageModel langMod, LengthModel lenMod){
		StackSentence s = new StackSentence(parent, newWord, sourceSentence, dict, langMod, lenMod);
		list.add(s);
		if(list.size() > 10){
			list.remove(list.first());
		}
	}
	
	private void printStack(SortedSet<StackSentence> list, WordStorage targetStorage){
		System.out.flush();
		System.err.flush();
		for(StackSentence s:list){
			err(s.toStringWithStorage(targetStorage));
		}
		err();
	}
	
	private void decode(WordStorage sourceWordStorage, WordStorage targetWordStorage, 
			LengthModel lengthModel, LanguageModel targetLanguageModel, 
			Dictionary dictionary, String[] sentence){
		// generate source sentence as int array
		int[] sourceSentence = new int[sentence.length];
		for(int i=0; i<sentence.length; i++){
			sourceSentence[i] = sourceWordStorage.getIndex(sentence[i]);
			if(sourceSentence[i] == -1) err("WARNING: unknown word: " + sentence[i]);
		}
		// init sentences
		SortedSet<StackSentence> list = new ConcurrentSkipListSet<>();
		StackSentence hypo = new StackSentence(null, 
				LanguageModel.SENTENCE_BEGIN_WORD, sourceSentence,
				dictionary, targetLanguageModel, lengthModel);
		// stack generation loop
		for(;;){
			// add sentence end
			addNewStackSentenceToList(list, hypo, LanguageModel.SENTENCE_END_WORD, 
					sourceSentence, dictionary, targetLanguageModel, lengthModel);
			// add all other target words
			for(int w=0; w<targetWordStorage.getWordCount(); w++){
				addNewStackSentenceToList(list, hypo, w, 
						sourceSentence, dictionary, targetLanguageModel, lengthModel);
			}
			printStack(list, targetWordStorage);
			// promote best sentence to hypothesis
			hypo = list.last();
			list.remove(hypo);
			// check if hypothesis is complete
			if(hypo.words.length > 0) if(hypo.words[hypo.words.length-1] == LanguageModel.SENTENCE_END_WORD) break;
		}
		// hypo is the target sentence. print it!
		String s = "";
		for(int i=1; i<hypo.words.length-1; i++){
			s = s + targetWordStorage.getWord(hypo.words[i]) + " ";
		}
		log(s);
	}
	
	private void print(WordStorage storage){
		for(int i=0; i<storage.getWordCount(); i++){
			log(storage.getWord(i));
		}
	}
	
	public void executeTrainAction(String base, String sourceLocale, String targetLocale){
		log("training of base \"" + base + "\" from locale \"" + sourceLocale + "\" to locale \"" + targetLocale + "\":");
		log();
		
		log("reading source corpus file...");
		CorpusHolder sourceHolder = new CorpusHolder(base, sourceLocale);
		log("reading target corpus file...");
		CorpusHolder targetHolder = new CorpusHolder(base, targetLocale);
		Corpus sourceCorpus = sourceHolder.getCorpus();
		Corpus targetCorpus = targetHolder.getCorpus();	
		log("extracting word storages from corpora...");
		WordStorage sourceStorage = sourceHolder.getStorage();
		WordStorage targetStorage = targetHolder.getStorage();
		log("writing source corpus data to disk...");
		sourceCorpus.writeToFile(Corpus.getFileName(base, sourceLocale));
		log("writing source word storage to disk...");
		sourceStorage.writeToFile(WordStorage.getFileName(base, sourceLocale));
		log("writing target corpus data to disk...");
		targetCorpus.writeToFile(Corpus.getFileName(base, targetLocale));
		log("writing target word storage to disk...");
		targetStorage.writeToFile(WordStorage.getFileName(base, targetLocale));
		log();
		
		log("creating target language model object...");
		LanguageModel targetModel = new LanguageModel(base, targetLocale, targetStorage.getWordCount());
		log("training target language model...");
		trainLanguageModel(targetCorpus, targetModel);
		log("writing target language model to disk...");
		targetModel.writeToFile(LanguageModel.getFileName(base, targetLocale));
		log();
		
		log("creating length model object...");
		LengthModel lengthModel = new LengthModel(base, 
				sourceLocale, targetLocale, 
				sourceCorpus.getMaxSentenceLength(), 
				targetCorpus.getMaxSentenceLength());
		log("training length model...");
		trainLengthModel(sourceCorpus, targetCorpus, lengthModel);
		log("writing length model to disk...");
		lengthModel.writeToFile(LengthModel.getFileName(base, sourceLocale, targetLocale));
		log();
		
		log("creating dictionary object...");
		Dictionary dictionary = new Dictionary(base, 
				sourceLocale, targetLocale, 
				sourceStorage.getWordCount(), targetStorage.getWordCount());
		try{
			shutdownManager.beginShutdownInjection();
			log("training dictionary...");
			trainDictionary(sourceCorpus, targetCorpus, dictionary);
			log("writing dictionary to disk...");
			dictionary.writeToFile(Dictionary.getFileName(base, sourceLocale, targetLocale));
		} finally {
			shutdownManager.endShutdownInjection();
		}
		log();
		
		log("training of base \"" + base + "\" from locale \"" + sourceLocale + "\" to locale \"" + targetLocale + "\" done.\n");
	}
	
	public void executeResumeTrainAction(String base, String sourceLocale, String targetLocale){
		log("resume training of base \"" + base + "\" from locale \"" + sourceLocale + "\" to locale \"" + targetLocale + "\":");
		log();
		
		log("reading source corpus data from disk...");
		Corpus sourceCorpus = (Corpus)Corpus.loadFromFile(Corpus.getFileName(base, sourceLocale));
		if(sourceCorpus == null) err("could not read source corpus data!");
		log("reading target corpus data from disk...");
		Corpus targetCorpus = (Corpus)Corpus.loadFromFile(Corpus.getFileName(base, targetLocale));
		if(targetCorpus == null) err("could not read target corpus data!");
		log();
		
		log("reading dictionary from disk...");
		Dictionary dictionary = (Dictionary)Dictionary.loadFromFile(Dictionary.getFileName(base, sourceLocale, targetLocale));
		if(dictionary == null) err("could not read dictionary data!");
		try{
			shutdownManager.beginShutdownInjection();
			log("resume training dictionary...");
			trainDictionary(sourceCorpus, targetCorpus, dictionary);
			log("writing dictionary to disk...");
			dictionary.writeToFile(base);
		} finally {
			shutdownManager.endShutdownInjection();
		}
		log();
		
		log("resume training of base \"" + base + "\" from locale \"" + sourceLocale + "\" to locale \"" + targetLocale + "\" done.\n");
	}
	
	public void executeLookupAction(String base, String sourceLocale, String targetLocale){
		log("lookup in base \"" + base + "\" from locale \"" + sourceLocale + "\" to locale \"" + targetLocale + "\":");
		log();
		
		log("reading target word storage from disk...");
		WordStorage sourceStorage = (WordStorage)WordStorage.loadFromFile(WordStorage.getFileName(base, sourceLocale));
		if(sourceStorage == null) err("could not read target word storage!");
		log("reading target word storage from disk...");
		WordStorage targetStorage = (WordStorage)WordStorage.loadFromFile(WordStorage.getFileName(base, targetLocale));
		if(targetStorage == null) err("could not read target word storage!");
		log();
		
		log("reading dictionary from disk...");
		Dictionary dictionary = (Dictionary)Dictionary.loadFromFile(Dictionary.getFileName(base, sourceLocale, targetLocale));
		if(dictionary == null) err("could not read dictionary data!");
		log();

		log("listening on stdin... (type words here)\n");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String s;
		try {
			while ((s = in.readLine()) != null && s.length() != 0){
				lookup(sourceStorage, targetStorage, dictionary, s);
				log();
			}
		} catch (IOException ex) {
			err("could not read from stdin!");
		}
		
		log("lookup in base \"" + base + "\" from locale \"" + sourceLocale + "\" to locale \"" + targetLocale + "\" done.");
	}
	
	public void executeDecodeAction(String base, String sourceLocale, String targetLocale){
		err("decoding in base \"" + base + "\" from locale \"" + sourceLocale + "\" to locale \"" + targetLocale + "\":");
		err();
		
		err("reading target word storage from disk...");
		WordStorage sourceStorage = (WordStorage)WordStorage.loadFromFile(WordStorage.getFileName(base, sourceLocale));
		if(sourceStorage == null) err("could not read target word storage!");
		err("reading target word storage from disk...");
		WordStorage targetStorage = (WordStorage)WordStorage.loadFromFile(WordStorage.getFileName(base, targetLocale));
		if(targetStorage == null) err("could not read target word storage!");
		err();
		
		err("reading target language model from disk...");
		LanguageModel targetLanguageModel = (LanguageModel)LanguageModel.loadFromFile(LanguageModel.getFileName(base, targetLocale));
		if(targetLanguageModel == null) err("could not read dictionary data!");
		err();

		err("reading length model from disk...");
		LengthModel lengthModel = (LengthModel)LengthModel.loadFromFile(LengthModel.getFileName(base, sourceLocale, targetLocale));
		if(lengthModel == null) err("could not read length model!");
		err();

		err("reading dictionary from disk...");
		Dictionary dictionary = (Dictionary)Dictionary.loadFromFile(Dictionary.getFileName(base, sourceLocale, targetLocale));
		if(dictionary == null) err("could not read dictionary data!");
		err();

		err("listening on stdin... (type words here)\n");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String s;
		try {
			while ((s = in.readLine()) != null && s.length() != 0){
				decode(sourceStorage, targetStorage, lengthModel, targetLanguageModel, dictionary, s.split(" "));
				err();
			}
		} catch (IOException ex) {
			err("could not read from stdin!");
		}
		
		err("decoding in base \"" + base + "\" from locale \"" + sourceLocale + "\" to locale \"" + targetLocale + "\" done.");
	}
	
}
