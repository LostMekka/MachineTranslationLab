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
	
	private DecodeHelper helper;
	private Sentence[] population;
	private int populationSize, generationCount, generationsDone = 0, maxCrossings;
	private Sentence bestSentence = null;
	private Random random = new Random();

	public DecodeEvolutionController(DecodeHelper helper, int populationSize, int generationCount) {
		// init general vars
		this.helper = helper;
		if(populationSize % 2 != 0) populationSize++;
		this.populationSize = populationSize;
		this.generationCount = generationCount;
		maxCrossings = (int)((double)populationSize / CHILD_COUNT);
		// init population
		population = new Sentence[populationSize];
		Sentence s1 = new Sentence(helper);
		population[0] = s1;
		bestSentence = s1;
		for(int i=1; i<populationSize; i++){
			Sentence s = new Sentence(helper);
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
			Sentence p1 = population[indices[0]];
			Sentence p2 = population[indices[1]];
			for(int i=2; i<CHILD_COUNT; i++){
				Sentence s = population[indices[i]];
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
				Sentence s;
				if(i == 0){
					s = new Sentence(helper);
				} else {
					s = new Sentence(p1, p2, mutationCount);
				}
				population[indices[i]] = s;
				// if the offspring sentence is better than all others, crown it king!!!
				if(s.score > bestSentence.score){
					bestSentence = s;
					System.out.println(generationsDone + ":");
					helper.printAlignmentArray(s.sentence);
				}
			}
		}
		// the generation is over
		generationsDone++;
//		if(generationsDone % 50 == 0){
//			DecodeEvolutionSentence best = population[0];
//			for(int i=1; i<population.length; i++){
//				if(population[i].score > best.score) best = population[i];
//			}
//			best.printAlignmentArray();
//		}
	}
	
	public int[] getBestSentence(){
		if(bestSentence == null) return null;
		return bestSentence.sentence;
	}
	
}
