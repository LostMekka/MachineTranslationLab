/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

import java.util.Random;

/**
 *
 * @author LostMekka
 */
public class DecodeHelper {

	private static final Random random = new Random();
	
	public LanguageModel targetLanguageModel;
	public LengthModel lengthModel;
	public Dictionary dictionary;
	public WordStorage sourceWordStorage, targetWordStorage;
	public Corpus sourceCorpus, targetCorpus;
	public int[] sourceSentence;
	public double[] wordScores;
	public double wordScoreSum, lengthScoreSum;

	public DecodeHelper(LanguageModel targetLanguageModel, LengthModel lengthModel, Dictionary dictionary, WordStorage sourceWordStorage, WordStorage targetWordStorage, int[] sourceSentence) {
		this.targetLanguageModel = targetLanguageModel;
		this.lengthModel = lengthModel;
		this.dictionary = dictionary;
		this.sourceWordStorage = sourceWordStorage;
		this.targetWordStorage = targetWordStorage;
		this.sourceSentence = sourceSentence;
		// init score helpers
		wordScores = new double[targetWordStorage.getWordCount()];
		wordScoreSum = 0d;
		for(int i=0; i<wordScores.length; i++){
			wordScores[i] = 0d;
			for(int j=0; j<sourceSentence.length; j++){
				wordScores[i] += dictionary.getTranslationScore(sourceSentence[j], i);
			}
			wordScoreSum += wordScores[i];
		}
		lengthScoreSum = 0d;
		for(int i=1; i<=lengthModel.getHighestPossibleTargetLength(sourceSentence.length); i++){
			lengthScoreSum += lengthModel.getLengthPairProbability(sourceSentence.length, i);
		}
	}
	
	public int[][] getAlignmentArray(int[] targetSentence) {
		double[][] array = new double[sourceSentence.length][targetSentence.length];
		for(int x=0; x<sourceSentence.length; x++){
			for(int y=0; y<targetSentence.length; y++){
				array[x][y] = dictionary.getTranslationScore(sourceSentence[x], targetSentence[y]);
			}
		}
		int[][] alignmentArray = new int[sourceSentence.length][targetSentence.length];
		double[] maxX = new double[targetSentence.length];
		for(int x=0; x<sourceSentence.length; x++){
			double maxY = array[x][0];
			for(int y=0; y<targetSentence.length; y++){
				if(array[x][y] <= 0d) continue;
				// x
				if(x == 0) maxX[y] = array[0][y];
				if(array[x][y] > maxX[y]){
					maxX[y] = array[x][y];
					for(int i=0; i<sourceSentence.length; i++) alignmentArray[i][y] &= 1;
				}
				if(array[x][y] >= maxX[y]){
					alignmentArray[x][y] |= 2;
				}
				// y
				if(array[x][y] > maxY){
					maxY = array[x][y];
					for(int i=0; i<targetSentence.length; i++) alignmentArray[x][i] &= 2;
				}
				if(array[x][y] >= maxY){
					alignmentArray[x][y] |= 1;
				}
			}
		}
		return alignmentArray;
	}
	
	public void printAlignmentArray(int[] targetSentence){
		int[][] alignmentArray = getAlignmentArray(targetSentence);
		for(int y=0; y< targetSentence.length; y++){
			System.out.print("#");
			int ix = -1;
			for(int x=0; x<sourceSentence.length; x++){
				switch(alignmentArray[x][y]){
					case 0: System.out.print(" "); break;
					case 1: System.out.print("|"); break;
					case 2: System.out.print("-"); break;
					case 3: System.out.print("+"); ix = x; break;
				}
			}
			String s = "# " + targetWordStorage.getWord(targetSentence[y]) + "(" + targetSentence[y];
			if(ix >= 0) s += ", " + dictionary.getTranslationScore(sourceSentence[ix], targetSentence[y]);
			s += ")";
			System.out.println(s);
		}
		System.out.println("--------------------------");
	}
	
	public double getBigramScore(int[] targetSentence){
		double bigramScore = 1d;
		for(int i=1; i<targetSentence.length; i++){
			bigramScore *= targetLanguageModel.getBigramProbability(targetSentence[i-1], targetSentence[i]);
		}
		bigramScore *= targetLanguageModel.getBigramProbability(LanguageModel.SENTENCE_BEGIN_WORD, targetSentence[0]);
		bigramScore *= targetLanguageModel.getBigramProbability(targetSentence[targetSentence.length-1], LanguageModel.SENTENCE_END_WORD);
		bigramScore = Math.pow(bigramScore, 0.1d / (targetSentence.length + 1));
		return bigramScore;
	}
	
	public double getLengthScore(int[] targetSentence){
		return lengthModel.getLengthPairProbability(sourceSentence.length, targetSentence.length);
	}
	
	public double getDictionaryScore(int[] targetSentence){
//		double dictScore = 0d;
//		int[][] alignmentArray = getAlignmentArray(targetSentence);
//		for(int y=0; y< targetSentence.length; y++){
//			for(int x=0; x<sourceSentence.length; x++){
//				if(alignmentArray[x][y] == 3){
//					dictScore += dictionary.getTranslationScore(sourceSentence[x], targetSentence[y]);
//					for(int y2=y+1; y2<targetSentence.length; y2++) alignmentArray[x][y2] = 0;
//					break;
//				}
//			}
//		}
//		dictScore /= sourceSentence.length;
		double dictScore = 1d;
		for(int j=0; j<sourceSentence.length; j++){
			double d = 0d;
			for(int i=0; i<targetSentence.length; i++){
				d += dictionary.getTranslationScore(sourceSentence[j], targetSentence[i]);
			}
			dictScore *= d;
		}
		//dictScore = Math.pow(dictScore, 1d / sourceSentence.length);
		dictScore /= Math.pow(targetSentence.length, sourceSentence.length);
		return dictScore;
	}

	public double getScore(int[] targetSentence){
		return getBigramScore(targetSentence) * 
				getLengthScore(targetSentence) * 
				getDictionaryScore(targetSentence);
	}
	
	public int getWeightedRandomWord(){
		double r = random.nextDouble() * wordScoreSum;
		for(int i=0; i<wordScores.length; i++){
			if(r < wordScores[i]) return i;
			r -= wordScores[i];
		}
		return wordScores.length - 1;
	}
	
	public int getWeightedRandomWord(int sourceWord){
		double scoreSum = 0d;
		for(int targetWord=0; targetWord<targetWordStorage.getWordCount(); targetWord++){
			scoreSum += dictionary.getTranslationScore(sourceWord, targetWord);
		}
		double r = random.nextDouble() * scoreSum;
		for(int targetWord=0; targetWord<targetWordStorage.getWordCount(); targetWord++){
			double s = dictionary.getTranslationScore(sourceWord, targetWord);
			if(r < s) return targetWord;
			r -= s;
		}
		return targetWordStorage.getWordCount() - 1;
	}
	
	public int getWeightedRandomSentenceLength(){
		double r = random.nextDouble() * lengthScoreSum;
		int max = lengthModel.getHighestPossibleTargetLength(sourceSentence.length);
		for(int i=1; i<=max; i++){
			double d = lengthModel.getLengthPairProbability(sourceSentence.length, i);
			if(r < d) return i;
			r -= d;
		}
		return max;
	}
	
}
