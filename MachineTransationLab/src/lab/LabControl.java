/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

/**
 *
 * @author LostMekka
 */
public class LabControl {
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		ShutdownManager sd = new ShutdownManager();
		sd.beginShutdownInjection();
		try{
		if(args.length < 4){
			System.err.println("wrong parameter count!");
			System.exit(1);
		}
		LabControl control = new LabControl(sd);
		if(args[3].equalsIgnoreCase("train")) control.executeTrainAction(args[0], args[1], args[2]);
		if(args[3].equalsIgnoreCase("resumetrain")) control.executeResumeTrainAction(args[0], args[1], args[2]);
		if(args[3].equalsIgnoreCase("lookup")){
			if(args.length < 5){
				System.err.println("wrong parameter count!");
				System.exit(1);
			}
			control.executeLookupAction(args[0], args[1], args[2], args[4]);
		}
		} catch (Exception e){
			System.err.println("ERROR! aborting all actions!");
			e.printStackTrace();
		}
		sd.endShutdownInjection();
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
			model.addBigram(LanguageModel.SENTENCE_END_WORD, words[words.length-1]);
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
		System.out.println("(you can stop the training with ctrl-c and resume later with the \"resumetrain\" command)");
		float delta = 1000f;
		int i = 1;
		while(delta > 0.0001f){
			delta = dictionary.iter(sourceCorpus.getSentences(), targetCorpus.getSentences());
			System.out.format("    iteration %4d - delta = %13.10f\n", i, delta);
			i++;
			if(shutdownManager.isShutdownRequested()) break;
		}
	}
	
	private void lookup(Corpus sourceCorpus, Corpus targetCorpus, Dictionary dictionary, String word){
		int sourceWord = sourceCorpus.getWordStorage().getIndex(word);
		int[] bestWords = dictionary.getBestTranslations(sourceWord, 10);
		float[] scores = dictionary.getTranslationScores(sourceWord, bestWords);
		for(int i=0; i<bestWords.length; i++){
			String targetWord = targetCorpus.getWordStorage().getWord(bestWords[i]);
			System.out.format("%4d: %10.8f - %s\n", i, scores[i], targetWord);
		}
		System.out.format("%s\n", targetCorpus.getWordStorage().getWord(dictionary.getBestTranslation(sourceWord)));
	}
	
	private void print(WordStorage storage){
		for(int i=0; i<storage.getWordCount(); i++){
			System.out.println(storage.getWord(i));
		}
	}
	
	public void executeTrainAction(String base, String sourceLocale, String targetLocale){
		System.out.println("training of base \"" + base + "\" from locale \"" + sourceLocale + "\" to locale \"" + targetLocale + "\":");
		System.out.println();
		
		System.out.println("creating corpus objects...");
		Corpus sourceCorpus = new Corpus(base, sourceLocale);
		Corpus targetCorpus = new Corpus(base, targetLocale);		
		System.out.println("reading source corpus file...");
		sourceCorpus.readFile();
		System.out.println("reading target corpus file...");
		targetCorpus.readFile();
		System.out.println("extracting word storages from corpora...");
		WordStorage sourceStorage = sourceCorpus.getWordStorage();
		WordStorage targetStorage = targetCorpus.getWordStorage();
		System.out.println("writing source corpus data and word storage to disk...");
		sourceCorpus.writeToFile(base);
		System.out.println("writing target corpus data and word storage to disk...");
		targetCorpus.writeToFile(base);
		System.out.println();
		
		System.out.println("creating language model objects...");
		LanguageModel sourceModel = new LanguageModel(sourceStorage.getWordCount(), sourceLocale);
		LanguageModel targetModel = new LanguageModel(targetStorage.getWordCount(), targetLocale);
		System.out.println("training source language model...");
		trainLanguageModel(sourceCorpus, sourceModel);
		System.out.println("writing source language model to disk...");
		sourceModel.writeToFile(base);
		System.out.println("training target language model...");
		trainLanguageModel(targetCorpus, targetModel);
		System.out.println("writing target language model to disk...");
		targetModel.writeToFile(base);
		System.out.println();
		
		System.out.println("creating length model object...");
		LengthModel lengthModel = new LengthModel(sourceLocale, targetLocale, sourceCorpus.getMaxSentenceLength(), targetCorpus.getMaxSentenceLength());
		System.out.println("training length model...");
		trainLengthModel(sourceCorpus, targetCorpus, lengthModel);
		System.out.println("writing length model to disk...");
		lengthModel.writeToFile(base);
		System.out.println();
		
		System.out.println("creating dictionary object...");
		Dictionary dictionary = new Dictionary(sourceStorage.getWordCount(), targetStorage.getWordCount(), sourceLocale, targetLocale);
		System.out.println("training dictionary...");
		trainDictionary(sourceCorpus, targetCorpus, dictionary);
		System.out.println("writing dictionary to disk...");
		dictionary.writeToFile(base);
		System.out.println();
		
		System.out.println("training of base \"" + base + "\" from locale \"" + sourceLocale + "\" to locale \"" + targetLocale + "\" done.\n");
	}
	
	public void executeResumeTrainAction(String base, String sourceLocale, String targetLocale){
		System.out.println("resume training of base \"" + base + "\" from locale \"" + sourceLocale + "\" to locale \"" + targetLocale + "\":");
		System.out.println();
		
		System.out.println("creating corpus objects...");
		Corpus sourceCorpus = new Corpus(base, sourceLocale);
		Corpus targetCorpus = new Corpus(base, targetLocale);		
		System.out.println("reading source corpus data and word storage from disk...");
		sourceCorpus = (Corpus)sourceCorpus.loadFromFile(base);
		System.out.println("reading target corpus data and word storage from disk...");
		targetCorpus = (Corpus)targetCorpus.loadFromFile(base);
		System.out.println("extracting word storages from corpora...");
		WordStorage sourceStorage = sourceCorpus.getWordStorage();
		WordStorage targetStorage = targetCorpus.getWordStorage();
		System.out.println();
		
		System.out.println("creating dictionary object...");
		Dictionary dictionary = new Dictionary(sourceStorage.getWordCount(), targetStorage.getWordCount(), sourceLocale, targetLocale);
		System.out.println("reading dictionary from disk...");
		dictionary = (Dictionary)dictionary.loadFromFile(base);
		System.out.println("resume training dictionary...");
		trainDictionary(sourceCorpus, targetCorpus, dictionary);
		System.out.println("writing dictionary to disk...");
		dictionary.writeToFile(base);
		System.out.println();
		
		System.out.println("resume training of base \"" + base + "\" from locale \"" + sourceLocale + "\" to locale \"" + targetLocale + "\" done.\n");
	}
	
	public void executeLookupAction(String base, String sourceLocale, String targetLocale, String lookupWord){
		System.out.println("lookup of word \"" + lookupWord + "\" in base \"" + base + "\" from locale \"" + sourceLocale + "\" to locale \"" + targetLocale + "\":");
		System.out.println();
		
		System.out.println("creating corpus objects...");
		Corpus sourceCorpus = new Corpus(base, sourceLocale);
		Corpus targetCorpus = new Corpus(base, targetLocale);		
		System.out.println("reading source corpus data and word storage from disk...");
		sourceCorpus = (Corpus)sourceCorpus.loadFromFile(base);
		System.out.println("reading target corpus data and word storage from disk...");
		targetCorpus = (Corpus)targetCorpus.loadFromFile(base);
		System.out.println("extracting word storages from corpora...");
		WordStorage sourceStorage = sourceCorpus.getWordStorage();
		WordStorage targetStorage = targetCorpus.getWordStorage();
		System.out.println();
		
		System.out.println("creating dictionary object...");
		Dictionary dictionary = new Dictionary(sourceStorage.getWordCount(), targetStorage.getWordCount(), sourceLocale, targetLocale);
		System.out.println("reading dictionary from disk...");
		dictionary = (Dictionary)dictionary.loadFromFile(base);
		lookup(sourceCorpus, targetCorpus, dictionary, lookupWord);
		System.out.println();
		
		System.out.println("lookup of word \"" + lookupWord + "\" in base \"" + base + "\" from locale \"" + sourceLocale + "\" to locale \"" + targetLocale + "\" done.");
	}

}
