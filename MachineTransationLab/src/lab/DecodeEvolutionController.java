/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

import java.util.Random;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author LostMekka
 */
public class DecodeEvolutionController {
	
	private final static int CHILD_COUNT = 2;
	private final static double START_MUTATION_RATE = 40d;
	private final static double END_MUTATION_RATE = 40d;
	
	public WordStorage targetWordStorage;
	public LengthModel lengthModel;
	public LanguageModel targetLanguageModel;
	public Dictionary dictionary;
	public int[] sourceSentence;
	public double[] wordScores;
	public double wordScoreSum, lengthScoreSum;
	
	private DecodeEvolutionSentence[] population;
	private int populationSize, generationCount, generationsDone = 0, maxCrossings;
	private DecodeEvolutionSentence bestSentence = null;
	private Random random = new Random();

	public DecodeEvolutionController(LengthModel lengthModel, LanguageModel targetLanguageModel, Dictionary dictionary, WordStorage targetWordStorage, int[] sourceSentence, int populationSize, int generationCount) {
		// init general vars
		if(populationSize % 2 != 0) populationSize++;
		this.lengthModel = lengthModel;
		this.targetLanguageModel = targetLanguageModel;
		this.dictionary = dictionary;
		this.sourceSentence = sourceSentence;
		this.populationSize = populationSize;
		this.generationCount = generationCount;
		this.targetWordStorage = targetWordStorage;
		maxCrossings = (int)((double)populationSize / CHILD_COUNT);
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
		// init population
		population = new DecodeEvolutionSentence[populationSize];
		DecodeEvolutionSentence s1 = new DecodeEvolutionSentence(this);
		population[0] = s1;
		bestSentence = s1;
		for(int i=1; i<populationSize; i++){
			DecodeEvolutionSentence s = new DecodeEvolutionSentence(this);
			population[i] = s;
			if(s.score > bestSentence.score) bestSentence = s;
		}
	}
	
	public boolean isDone(){
		return (generationsDone >= generationCount);
	}
	
	public void evolveGeneration(){
		// calculate mutation rate
		double mutationRate = START_MUTATION_RATE - (double)generationsDone / generationCount * 
				(START_MUTATION_RATE - END_MUTATION_RATE);
		if(mutationRate < 0d) mutationRate = 0d;
		double mutationCounter = 0d;
		// do generation crossing
		for(int crossing=0; crossing<maxCrossings; crossing++){
			// get <CHILD_COUNT> !different! indices
			int[] indices = new int[CHILD_COUNT];
			for(int i=0; i<CHILD_COUNT; i++){
				boolean b = false;
				while(!b){
					b = true;
					indices[i] = random.nextInt(populationSize);
					for(int i2=0; i2<i; i2++){
						if(indices[i] == indices[i2]){
							b = false;
							break;
						}
					}
				}
			}
			// get the 2 best sentences corresponding to the indices
			DecodeEvolutionSentence p1 = population[indices[0]];
			DecodeEvolutionSentence p2 = population[indices[1]];
			for(int i=2; i<CHILD_COUNT; i++){
				DecodeEvolutionSentence s = population[indices[i]];
				if(s.score > p1.score){
					p2 = p1;
					p1 = s;
					continue;
				}
				if(s.score > p2.score) p2 = s;
			}
			// overwrite all the selected sentences with offsprings of the 2 parents
			for(int i=0; i<CHILD_COUNT; i++){
				mutationCounter += mutationRate;
				int mutationCount = (int)mutationCounter;
				mutationCounter -= mutationCount;
				DecodeEvolutionSentence s;
				if(i == 0){
					s = new DecodeEvolutionSentence(this);
				} else {
					s = new DecodeEvolutionSentence(p1, p2, mutationCount);
				}
				population[indices[i]] = s;
				// if the offspring sentence is better than all others, crown it king!!!
				if(s.score > bestSentence.score) bestSentence = s;
			}
		}
		// the generation is over
		generationsDone++;
		if(generationsDone % 100 == 0){
			bestSentence.printDictDistribution();
		}
	}
	
	public int[] getBestSentence(){
		if(bestSentence == null) return null;
		return bestSentence.sentence;
	}
	
}
