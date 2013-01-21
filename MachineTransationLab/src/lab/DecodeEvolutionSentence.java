/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author LostMekka
 */
public final class DecodeEvolutionSentence{
	
	private static final Random random = new Random();
	
	public int[] sentence;
	public double score;
	
	public String string;
	
	private DecodeEvolutionController ctrl;

	/**
	 * creates a new random sentence
	 */
	public DecodeEvolutionSentence(DecodeEvolutionController ctrl) {
		this.ctrl = ctrl;
		// create sentence
		sentence = new int[getRandomSentenceLength()];
		for(int i=0; i<sentence.length; i++) sentence[i] = getRandomWord();
		// score
		calcScore();
		string = ctrl.targetWordStorage.getString(sentence);
	}
	
	/**
	 * creates a new sentence offspring based off the two parent sentence and mutates it
	 */
	public DecodeEvolutionSentence(DecodeEvolutionSentence parent1, DecodeEvolutionSentence parent2, int mutationCount) {
		ctrl = parent1.ctrl;
		// create sentence
		int l1 = parent1.sentence.length;
		int l2 = parent2.sentence.length;
		if((l1 <= 0) || (l2 <= 0)){
			int i = 0;
		}
		int lmax = l1;
		if(l2 > lmax) lmax = l2;
		ArrayList<Integer> sentenceArray = new ArrayList<>(lmax);
		boolean p1isDominant = random.nextBoolean();
		int loc = 0;
		for(;;){
			// choose parent
			int[] s;
			if(p1isDominant){
				s = parent1.sentence;
			} else {
				s = parent2.sentence;
			}
			// choose length of dominant part
			int len = 1;
			if(lmax > 1) len += random.nextInt(lmax - 1);
			if(loc + len > s.length) len = s.length - loc;
			// write dominant part
			for(int i=0; i<len; i++) sentenceArray.add(s[loc + i]);
			loc += len;
			// if dominant parent has reached its end, we are done.
			if(loc >= s.length) break;
			// swap dominant parent for next dominant part
			p1isDominant = !p1isDominant;
		}
		// convert arraylist to array
		sentence = new int[sentenceArray.size()];
		for(int i=0; i<sentenceArray.size(); i++){
			sentence[i] = sentenceArray.get(i);
		}
		// mutate
		for(int i=0; i<mutationCount; i++) mutate();
		// score
		calcScore();
	}
	
	public void printDictDistribution(){
		int[] source = ctrl.sourceSentence;
		float[][] array = new float[source.length][sentence.length];
		for(int x=0; x<source.length; x++){
			for(int y=0; y<sentence.length; y++){
				array[x][y] = ctrl.dictionary.getTranslationScore(source[x], sentence[y]);
			}
		}
		int[][] arr2 = new int[source.length][sentence.length];
		float[] maxX = new float[sentence.length];
		for(int x=0; x<source.length; x++){
			float maxY = array[x][0];
			for(int y=0; y<sentence.length; y++){
				if(array[x][y] <= 0d) continue;
				// x
				if(x == 0) maxX[y] = array[0][y];
				if(array[x][y] > maxX[y]){
					maxX[y] = array[x][y];
					for(int i=0; i<source.length; i++) arr2[i][y] &= 1;
				}
				if(array[x][y] >= maxX[y]){
					arr2[x][y] |= 2;
				}
				// y
				if(array[x][y] > maxY){
					maxY = array[x][y];
					for(int i=0; i<sentence.length; i++) arr2[x][i] &= 2;
				}
				if(array[x][y] >= maxY){
					arr2[x][y] |= 1;
				}
			}
			
		}
		for(int y=0; y< sentence.length; y++){
			System.out.print("#");
			for(int x=0; x<source.length; x++){
				switch(arr2[x][y]){
					case 0: System.out.print(" "); break;
					case 1: System.out.print("|"); break;
					case 2: System.out.print("-"); break;
					case 3: System.out.print("+"); break;
				}
			}
			System.out.println("# " + ctrl.targetWordStorage.getWord(sentence[y]));
		}
		System.out.println("score: " + score);
		System.out.println("--------------------------");
	}
	
	private int getRandomWord(){
		double r = random.nextDouble() * ctrl.wordScoreSum;
		for(int i=0; i<ctrl.wordScores.length; i++){
			if(r < ctrl.wordScores[i]) return i;
			r -= ctrl.wordScores[i];
		}
		return ctrl.wordScores.length - 1;
	}
	
	private int getRandomSentenceLength(){
		double r = random.nextDouble() * ctrl.lengthScoreSum;
		int max = ctrl.lengthModel.getHighestPossibleTargetLength(ctrl.sourceSentence.length);
		for(int i=1; i<=max; i++){
			double d = ctrl.lengthModel.getLengthPairProbability(ctrl.sourceSentence.length, i);
			if(r < d) return i;
			r -= d;
		}
		return max;
	}
	
	private void calcScore(){
		int[] source = ctrl.sourceSentence;
		double bigramScore = 0d;
		// bigrams
		for(int i=1; i<sentence.length; i++){
			bigramScore += ctrl.targetLanguageModel.getBigramProbability(sentence[i-1], sentence[i]);
		}
		bigramScore += ctrl.targetLanguageModel.getBigramProbability(LanguageModel.SENTENCE_BEGIN_WORD, sentence[0]);
		bigramScore += ctrl.targetLanguageModel.getBigramProbability(sentence[sentence.length-1], LanguageModel.SENTENCE_END_WORD);
		bigramScore /= sentence.length + 1;
		// length
		double lengthScore = ctrl.lengthModel.getLengthPairProbability(source.length, sentence.length);
		// dictionary
		double dictScore = 0d;
		float[][] array = new float[source.length][sentence.length];
		for(int x=0; x<source.length; x++){
			for(int y=0; y<sentence.length; y++){
				array[x][y] = ctrl.dictionary.getTranslationScore(source[x], sentence[y]);
			}
		}
		int[][] arr2 = new int[source.length][sentence.length];
		float[] maxX = new float[sentence.length];
		for(int x=0; x<source.length; x++){
			float maxY = array[x][0];
			for(int y=0; y<sentence.length; y++){
				if(array[x][y] <= 0d) continue;
				// x
				if(x == 0) maxX[y] = array[0][y];
				if(array[x][y] > maxX[y]){
					maxX[y] = array[x][y];
					for(int i=0; i<source.length; i++) arr2[i][y] &= 1;
				}
				if(array[x][y] >= maxX[y]){
					arr2[x][y] |= 2;
				}
				// y
				if(array[x][y] > maxY){
					maxY = array[x][y];
					for(int i=0; i<sentence.length; i++) arr2[x][i] &= 2;
				}
				if(array[x][y] >= maxY){
					arr2[x][y] |= 1;
				}
			}
		}
//		for(int y=0; y< sentence.length; y++){
//			System.out.print("#");
//			for(int x=0; x<source.length; x++){
//				switch(arr2[x][y]){
//					case 0: System.out.print(" "); break;
//					case 1: System.out.print("|"); break;
//					case 2: System.out.print("-"); break;
//					case 3: System.out.print("+"); break;
//				}
//			}
//			System.out.println("#");
//		}
//		System.out.println(ctrl.targetWordStorage.getString(sentence));
		for(int y=0; y< sentence.length; y++){
			for(int x=0; x<source.length; x++){
				if(arr2[x][y] == 3){
					dictScore += arr2[x][y];
					for(int y2=y+1; y2<sentence.length; y2++) arr2[x][y2] = 0;
					break;
				}
			}
		}
		dictScore /= sentence.length;
//		for(int j=0; j<source.length; j++){
//			double max = 0d;
//			for(int i=0; i<sentence.length; i++){
//				double d = ctrl.dictionary.getTranslationScore(source[j], sentence[i]);
//				if(d > max) max = d;
//			}
//			if(max < dictScore) dictScore = max;
//		}
//		dictScore /= (double)source.length;
		//dictScore /= Math.pow(sourceSentence.length, sentence.length);
		// overall score
		score = 1.2d * bigramScore;
		score += 1d * lengthScore;
		score += 1.2d * dictScore;
	}
	
	private void mutate(){
		// select action
		int action = random.nextInt(4);
		int i = random.nextInt(sentence.length);
		switch(action){
			case 0:
				// swap 2 words
				if(sentence.length < 2) return;
				int i1 = i;
				while(i1 == i) i1 = random.nextInt(sentence.length);
				int tmp = sentence[i1];
				sentence[i1] = sentence[i];
				sentence[i] = tmp;
				break;
			case 1:
				// exchange a word with another word in target language
				if(sentence.length <= 0) return;
				sentence[i] = getRandomWord();
				break;
			case 2:
				// delete a word
				if(sentence.length <= 1) return;
				int[] newSentence1 = new int[sentence.length - 1];
				System.arraycopy(sentence, 0, newSentence1, 0, i);
				System.arraycopy(sentence, i + 1, newSentence1, i, sentence.length - i - 1);
				sentence = newSentence1;
				break;
			case 3:
				// add a word
				int i2 = random.nextInt(sentence.length + 1);
				int[] newSentence2 = new int[sentence.length + 1];
				System.arraycopy(sentence, 0, newSentence2, 0, i);
				newSentence2[i] = getRandomWord();
				System.arraycopy(sentence, i, newSentence2, i + 1, sentence.length - i);
				sentence = newSentence2;
				break;
		}
	}
	
}
