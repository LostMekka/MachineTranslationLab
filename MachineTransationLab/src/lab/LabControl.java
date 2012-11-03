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
		if(args.length < 4){
			System.err.println("wrong parameter count!");
			System.exit(1);
		}
		LabControl control = new LabControl(args[0], args[1], args[2]);
		if(args[3].equalsIgnoreCase("train")) control.train();
		if(args[3].equalsIgnoreCase("resumetrain")) control.resumeTrain();
		if(args[3].equalsIgnoreCase("lookup")) control.lookup();
	}
	
	private String base, sourceLocale, targetLocale;

	public LabControl(String base, String sourceLocale, String targetLocale) {
		this.base = base;
		this.sourceLocale = sourceLocale;
		this.targetLocale = targetLocale;
	}
	
	private void trainLanguageModel(Corpus corpus, LanguageModel model){
		
	}
	
	private void trainLengthModel(Corpus sourceCorpus, Corpus targetCorpus, LengthModel model){
		
	}
	
	private void trainDictionary(Corpus sourceCorpus, Corpus targetCorpus, Dictionary dictionary){
		
	}
	
	public void train(){
		System.out.println("training base \"" + base + "\" from locale \"" + sourceLocale + "\" to locale \"" + targetLocale + "\":");
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
		System.out.println("writing source word storage to disk...");
		sourceStorage.writeToFile(base);
		System.out.println("writing target word storage to disk...");
		targetStorage.writeToFile(base);
		System.out.println();
		
		System.out.println("creating language models...");
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
		
		System.out.println("creating length model...");
		LengthModel lengthModel = new LengthModel(sourceLocale, targetLocale, sourceCorpus.getMaxSentenceLength(), targetCorpus.getMaxSentenceLength());
		System.out.println("training length model...");
		trainLengthModel(sourceCorpus, targetCorpus, lengthModel);
		System.out.println("writing length model to disk...");
		lengthModel.writeToFile(base);
		System.out.println();
		
		System.out.println("creating dictionary...");
		Dictionary dictionary = new Dictionary(sourceStorage.getWordCount(), targetStorage.getWordCount(), sourceLocale, targetLocale);
		System.out.println("training dictionary...");
		System.out.println("(press any key to stop. you can resume training with the resumetrain command)");
		trainDictionary(sourceCorpus, targetCorpus, dictionary);
		System.out.println("writing dictionary to disk...");
		dictionary.writeToFile(base);
		System.out.println();
		
		System.out.println("training base \"" + base + "\" from locale \"" + sourceLocale + "\" to locale \"" + targetLocale + "\" done.\n");
	}
	
	public void resumeTrain(){
		
	}
	
	public void lookup(){
		
	}
		
}
