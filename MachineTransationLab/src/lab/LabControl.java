/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

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
		LabControl control = get();
		if(args.length < 4){
			control.err("wrong parameter count!");
			control.halt();
		}
		if(args[3].equalsIgnoreCase("train")) control.executeTrainAction(args[0], args[1], args[2]);
		if(args[3].equalsIgnoreCase("resumetrain")) control.executeResumeTrainAction(args[0], args[1], args[2]);
		if(args[3].equalsIgnoreCase("lookup")) control.executeLookupAction(args[0], args[1], args[2]);
		if(args[3].equalsIgnoreCase("decode")) control.executeDecodeAction(args[0], args[1], args[2]);
	}
	
	private ShutdownManager shutdownManager = new ShutdownManager();
	
	private static LabControl instance = null;
	public static LabControl get(){
		if(instance == null) instance = new LabControl();
		return instance;
	}
	private LabControl() {}
	
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
	
	private void trainLengthModel(Corpus sourceCorpus, Corpus targetCorpus, LengthModel lengthModel){
		int[][] sourceSentences = sourceCorpus.getSentences();
		int[][] targetSentences = targetCorpus.getSentences();
		for(int sentenceIndex=0; sentenceIndex<sourceSentences.length; sentenceIndex++){
			int sl = sourceSentences[sentenceIndex].length;
			int tl = targetSentences[sentenceIndex].length;
			lengthModel.addLenghtPair(sl, tl);
		}
	}
	
	private void trainDictionary(WordStorage sourceWordStorage, WordStorage targetWordStorage, Corpus sourceCorpus, Corpus targetCorpus, Dictionary dictionary){
		log("(you can stop the training with ctrl-c and resume later with the \"resumetrain\" command)");
		double delta = 1000f;
		int i = 1;
		while(delta > 0.000001f){
			delta = dictionary.iter(sourceCorpus.getSentences(), targetCorpus.getSentences());
			System.out.format("    iteration %4d - delta = %13.10f\n", i, delta);
			i++;
			lookup("needs", sourceWordStorage, targetWordStorage, dictionary);
			if(shutdownManager.isShutdownRequested()) break;
		}
	}
	
	private void lookup(String word, WordStorage sourceWordStorage, WordStorage targetWordStorage, Dictionary dictionary){
		int sourceWord = sourceWordStorage.getIndex(word);
		if(sourceWord < 0){
			System.err.format("ERROR: \"%s\" is not contained in learned language.\n", word);
			return;
		}
		int[] bestWords = dictionary.getBestTranslations(sourceWord, 10);
		double[] scores = dictionary.getTranslationScores(sourceWord, bestWords);
		System.out.format("best translations for \"%s\":\n", word);
		for(int i=0; i<bestWords.length; i++){
			String targetWord = targetWordStorage.getWord(bestWords[i]);
			System.out.format("%4d: %10.8f - %s\n", i+1, scores[i], targetWord);
		}
	}
	
	private void decode(DecodeHelper helper){
		err();
		err(helper.sourceWordStorage.getString(helper.sourceSentence));
		//greedyDecode(helper);
		// get sentence
		int[] targetSentence = hillClimbingDecode(helper);
		//int[] targetSentence = evolutionDecode(sourceSentence, 1000, 600);
		//int[] targetSentence = simulatedAnnealingDecode(helper, 20, 200);
		// print sentence
		log(helper.targetWordStorage.getString(targetSentence));
	}

	private void greedyDecode(DecodeHelper helper){
		int[][] greedy = new int[helper.sourceSentence.length][5];
		for(int i=0; i<helper.sourceSentence.length; i++){
			greedy[i] = helper.dictionary.getBestTranslations(helper.sourceSentence[i], 5);
		}
		for(int j=0; j<5; j++){
			for(int i=0; i<greedy.length; i++){
				System.out.print(helper.targetWordStorage.getWord(greedy[i][j]));
			}
			System.out.println();
		}
	}
	
	private int[] hillClimbingDecode(DecodeHelper helper){
		Sentence bestSentence = new Sentence(helper);
		int count = 0;
		while(count < 5){
			Sentence currSentence = new Sentence(helper);
			while(true){
				Sentence newSentence = currSentence;
				// word replacement
				for(int si=0; si<helper.sourceSentence.length; si++){
					for(int ti=0; ti<currSentence.sentence.length; ti++){
						for(int w : helper.dictionary.getBestTranslations(helper.sourceSentence[si], 50)){
							Sentence s = new Sentence(currSentence);
							s.sentence[ti] = w;
							s.recalculateScore();
							if(s.score > currSentence.score) currSentence = s;
						}
					}
				}
				// word reordering
				for(int i1=0; i1<currSentence.sentence.length; i1++){
					for(int i2=0; i2<currSentence.sentence.length; i2++){
						if(i1 == i2) continue;
						Sentence s = new Sentence(currSentence);
						s.mutateOrdering(i1, i2);
						s.recalculateScore();
						if(s.score > currSentence.score) currSentence = s;
					}
				}
				// ading words
				for(int si=0; si<helper.sourceSentence.length; si++){
					for(int ti=0; ti<currSentence.sentence.length+1; ti++){
						for(int w : helper.dictionary.getBestTranslations(helper.sourceSentence[si], 50)){
							if(currSentence.sentence.length < 40){
								Sentence s = new Sentence(currSentence);
								s.addWord(ti, w);
								s.recalculateScore();
								if(s.score > currSentence.score) currSentence = s;
							}
						}
					}
				}
				// deleting words
				for(int ti=0; ti<currSentence.sentence.length; ti++){
					if(currSentence.sentence.length > 1){
						Sentence s = new Sentence(currSentence);
						s.deleteWord(ti);
						s.recalculateScore();
						if(s.score > currSentence.score) currSentence = s;
					}
				}
				if(newSentence == currSentence) break;
			}
			if(currSentence.score > bestSentence.score){
				count = 0;
				bestSentence = currSentence;
			} else {
				count++;
			}
		}
		return bestSentence.sentence;
	}
	
	private int[] simulatedAnnealingDecode(DecodeHelper helper, int steps, int tries){
		Sentence globalBest = null;
		for(int n=0; n<tries; n++){
			Sentence curr = new Sentence(helper);
			if(n == 0) globalBest = curr;
			boolean betterSentenceFound = true;
			while(betterSentenceFound){
				betterSentenceFound = false;
				for(int i=0; i<steps; i++){
					Sentence s = new Sentence(curr);
					for(int m=0; m<5; m++){
						s.mutate();
						s.recalculateScore();
						if(s.score > curr.score){
							betterSentenceFound = true;
							curr = s;
							if(curr.score > globalBest.score){
								globalBest = curr;
								//helper.printAlignmentArray(curr.sentence);
							}
							break;
						}
					}
					if(betterSentenceFound) break;
				}
			}		
		}
		return globalBest.sentence;
	}
	
	private int[] evolutionDecode(DecodeHelper helper, int populationSize, int generationCount){
		DecodeEvolutionController ctrl = new DecodeEvolutionController(helper, 
				populationSize, generationCount);
		while(!ctrl.isDone()) ctrl.evolveGeneration();
		return ctrl.getBestSentence();
	}
	
	private int[] stackDecode(DecodeHelper helper){
		// init sentences
		SortedSet<StackSentence> list = new ConcurrentSkipListSet<>();
		StackSentence hypo = new StackSentence(null, 
				LanguageModel.SENTENCE_BEGIN_WORD, helper.sourceSentence,
				helper.dictionary, helper.targetLanguageModel, helper.lengthModel);
		// stack generation loop
		for(;;){
			// add sentence end
			addNewStackSentenceToList(list, hypo, LanguageModel.SENTENCE_END_WORD, helper);
			// add all other target words
			for(int w=0; w<helper.targetWordStorage.getWordCount(); w++){
				addNewStackSentenceToList(list, hypo, w, helper);
			}
			//printStack(list, targetWordStorage);
			// promote best sentence to hypothesis
			hypo = list.last();
			list.remove(hypo);
			// check if hypothesis is complete
			if(hypo.words.length > 0) if(hypo.words[hypo.words.length-1] == LanguageModel.SENTENCE_END_WORD) break;
		}
		// hypo is the target sentence.
		return hypo.words;
	}
	
	private void addNewStackSentenceToList(SortedSet<StackSentence> list, 
			StackSentence parent, int newWord, DecodeHelper helper){
		StackSentence s = new StackSentence(parent, newWord, helper.sourceSentence, 
				helper.dictionary, helper.targetLanguageModel, helper.lengthModel);
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
			trainDictionary(sourceStorage, targetStorage, sourceCorpus, targetCorpus, dictionary);
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
			trainDictionary(null, null, sourceCorpus, targetCorpus, dictionary);
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
		WordStorage sourceWordStorage = (WordStorage)WordStorage.loadFromFile(WordStorage.getFileName(base, sourceLocale));
		if(sourceWordStorage == null) err("could not read target word storage!");
		log("reading target word storage from disk...");
		WordStorage targetWordStorage = (WordStorage)WordStorage.loadFromFile(WordStorage.getFileName(base, targetLocale));
		if(targetWordStorage == null) err("could not read target word storage!");
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
				lookup(s, sourceWordStorage, targetWordStorage, dictionary);
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
		
		err("reading source word storage from disk...");
		WordStorage sourceWordStorage = (WordStorage)WordStorage.loadFromFile(WordStorage.getFileName(base, sourceLocale));
		if(sourceWordStorage == null) err("could not read source word storage!");
		err("reading target word storage from disk...");
		WordStorage targetWordStorage = (WordStorage)WordStorage.loadFromFile(WordStorage.getFileName(base, targetLocale));
		if(targetWordStorage == null) err("could not read target word storage!");
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
				// generate source sentence as int array
				String[] strings = s.split(" ");
				int[] sourceSentence = new int[strings.length];
				for(int i=0; i<strings.length; i++){
					sourceSentence[i] = sourceWordStorage.getIndex(strings[i]);
					if(sourceSentence[i] == -1) err("WARNING: unknown word: " + strings[i]);
				}
				DecodeHelper helper = new DecodeHelper(targetLanguageModel, 
						lengthModel, dictionary, 
						sourceWordStorage, targetWordStorage, sourceSentence);
				decode(helper);
			}
		} catch (IOException ex) {
			err("could not read from stdin!");
		}
		
		err("decoding in base \"" + base + "\" from locale \"" + sourceLocale + "\" to locale \"" + targetLocale + "\" done.");
	}
	
}
