package semaphore;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class Semaphore extends java.util.Observable {
	
	private WordAnimation animation;
	private Random random = new Random();
	
	private HashMap<Integer, ArrayList<Word>> 
		wordBank = new HashMap<Integer, ArrayList<Word>>();
	
	private HashMap<Integer, ArrayList<Word>> 
		usedWords = new HashMap<Integer, ArrayList<Word>>();
	
	//State Variables
	private boolean idleState_ = true;
	private String picture_ = new String("");
	private String word_ = new String("");
	private int highScore_ = 0;
	private int score_ = 0;
	private int wins_ = 0;
	private int losses_ = 0;
	private int speed_ = 1;
	private int letters_ = 3;
	
	private int DEFAULT_SPEED = 1500; //1 second per letter
	
	//Directories
	private static final String IMAGE_DIR = "images/"; //image directory
	private static final String WORD_BANK_LOCATION = "data/data.kyt";
	
	/**
	 * Entry point
	 * @param args
	 */
	public static void main(String[] args) {
		Semaphore model = new Semaphore();
		StateManager controller = new StateManager(model);
		new SemaphoreGUI(model, controller);
	}
	
	/**
	 * Returns a list of strings pulled from a file
	 * @param pathLocation - the location of the file
	 * @return
	 */
	private List<String> loadFile(String pathLocation) {
		List<String> file = new ArrayList<String>();
		try {
			Path path = Paths.get(pathLocation);
			file = Files.readAllLines(path, Charset.forName("US-ASCII"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return file;
	}
	
	/**
	 * Fills the Word Bank using words from file at WORD_BANK_LOCATION
	 * Words are randomly added to the word bank, sorted by how many letters 
	 * contained in each word 
	 */
	private void populateWordBank() {
		
		List<String> file = loadFile(Semaphore.WORD_BANK_LOCATION);
		
		//goes through each line in the file and adds to wordbank
		for(String line : file) {
			List<String> wordArray = Arrays.asList(line.split(" "));
			for(String word : wordArray) {
				putWord(this.wordBank, new Word(word));
			}
		}
		
		for(Integer i : wordBank.keySet()) {
			Collections.shuffle(wordBank.get(i));
		}
	}
	
	/**
	 * Adds word to the proper array in wordBank. If there is no existing  
	 * array, one is created and the word is added to it
	 * @param wordBank
	 * @param word
	 */
	private void putWord(HashMap<Integer, ArrayList<Word>> wordBank, Word word) {
		int letterCount = word.word.length();
		if(!wordBank.containsKey(letterCount))
			wordBank.put(letterCount, new ArrayList<Word>());
		wordBank.get(letterCount).add(word);
	}
	
	/**
	 * Sole constructor
	 */
	public Semaphore() {
		populateWordBank();
		this.setImage("splash.gif");
	}
	
	/**
	 * Removes idle state and begins animation of a random word from the word
	 * bank.
	 */
	public void setupNewWord() {
		if(idleState_ == false) 
			return;
					
		idleState_ = false;
		
		word_ = randomWord();
		animation = 
				new WordAnimation(this, word_, DEFAULT_SPEED - 350*(speed_-1));
	}
	
	/**
	 * 
	 * 
	 */
	public void replayWord() {
		if(idleState_ == true) //no word to replay 
			return;
		
		animation = 
				new WordAnimation(this, word_, DEFAULT_SPEED - 350*(speed_-1));
	}
	private String randomWord() {		
		if(letters_ != 0) {
			return chooseWord(letters_);
		} else { //can use any set of letters
			int totalWords = 0;
			for (Integer i : wordBank.keySet()) {
				totalWords += wordBank.get(i).size();
			}
			int index = random.nextInt(totalWords);
			for (Integer i : wordBank.keySet()) {
				ArrayList<Word> wordList = wordBank.get(i);
				
				if(index < wordList.size()) 
					return chooseWord(i);
				
				index -= wordList.size();
			}
		}
		return chooseWord(1); //should never reach here
	}
	
	//*TODO* improve this process
	/** Returns a random word from the list of words with an amount of 
	 * letters equal to letterIndex. Adds the word to the usedWord bank
	 * preventing it from being used again
	 * @param letterIndex, the number of letters in the word
	 * @return
	 */
	private String chooseWord(int letterIndex) {
		System.out.println("Letter Index: "+ letterIndex);
		int wordIndex = random.nextInt(wordBank.get(letterIndex).size());
		Word word = wordBank.get(letterIndex).get(wordIndex);
		putWord(usedWords, word);
		wordBank.get(letterIndex).remove(wordIndex);
		checkWordBank();
		return word.word;
	}
	
	private void checkWordBank() {
		for(Integer i : wordBank.keySet()) {
			if(wordBank.get(i).isEmpty()) {
				wordBank.put(i, usedWords.get(i));
				Collections.shuffle(wordBank.get(i));
				usedWords.put(i, new ArrayList<Word>());
			}
		}
	}
	
	/*
	private String chooseWord() {			
		int letters = this.letters;
		if(this.letters == 0) { //can use any set of letters
			int size = wordBank.keySet().size();
			int randomIndex = random.nextInt(size);
			int index = 0;
			for(Integer i : wordBank.keySet()) {
				if (index == randomIndex){
					letters = i;
					break;
				}
				index ++;
			}
		}
		
		int wordIndex = (int) (Math.random() * wordBank.get(letters).size());
		String word = wordBank.get(letters).get(wordIndex).word;
		if(word.equals(this.word)) //no duplicate words
			return chooseWord();
		return word;
	}*/
	
	/**
	 * Compares the guess to the actual word. Updates the score and causes the
	 * program to enter the idle state. If the program is idle, this function
	 * does nothing
	 * 
	 * @param guess - compared to the actual word 
	 */
	public void checkGuess(String guess) {
		if(idleState_)
			return; 
		
		idleState_ = true;
		//*TODO* add checks for invalid guesses, incorrect letters etc.
		animation.cancel();
		if(guess.equalsIgnoreCase(word_)) {
			updateScore(true);
		} else {
			updateScore(false);
		}
	}
	
	/**
	 * Wins increases score by speed*letters. Random number of letters 
	 * multiplies score by 10. Losses decrease score by 2 
	 * @param won
	 */
	private void updateScore(boolean won) {
		String image;
		if(won) {
			wins_ ++;
			
			int letterMultiplier = letters_;
			if(letterMultiplier == 0) //any # of letters
				letterMultiplier = 6;
			score_ += speed_ * letterMultiplier;
			image = new String("correct.gif");
		} else {
			losses_ ++;
			image = new String("incorrect.gif");
		}
		
		checkHighScore();
		this.setImage(image);
	}
	
	/**
	 * Updates high score
	 */
	private void checkHighScore() {
		if(score_ > highScore_)
			highScore_ = score_; 
	}
	
	private class Word implements Comparable<Word> {
		public String word;
		int occurences;
		
		public Word(String word) {
			this.word = word;
			this.occurences = 0;
		}
		@Override
		public int compareTo(Word o) {
			return Integer.compare(this.occurences, o.occurences);
		}
	}
	
	/**
	 * state accessible by observers
	 */
	public class State {
		public String image;
		public String word;
		public int highScore;
		public int score;
		public int wins;
		public int losses;
		public int speed;
		public int letters;
		public boolean isIdle;
		
		public State(Semaphore model) {
			image = model.picture_;
			word = model.word_;
			highScore = model.highScore_;
			score = model.score_;
			wins = model.wins_;
			losses = model.losses_;
			speed = model.speed_;
			letters = model.letters_;
			isIdle = model.isIdle();
		}
	}
	
	private void update() {
		this.setChanged();
		this.notifyObservers(new State(this));
	}
	
	public void notifyObservers() {
		update();
	}
	
	private void setImage(String imageLocation) {
		picture_ = IMAGE_DIR + imageLocation;
		update();
	}
		
	private class WordAnimation extends TimerTask {
		private String word;
		private int index;
		Semaphore model;		
		
		public WordAnimation(Semaphore model, String word, long period) {
			this.word = word;
			this.model = model;
			index = 0;
			
			Timer timer = new Timer();
			timer.schedule(this, 0, period);
		}
		
		/**
		 * Cycles through the word's letters
		 */
		@Override
		public void run() {	
			if(index >= word.length()) {
				model.setImage("guess.png");
				this.cancel();
			} else {
				char letter = word.charAt(index);
				String imageLocation = letter + ".gif";
				model.setImage(imageLocation);
				index++;
			}
		}
	}
	
	//Getters and Setter
	
	public void setSpeed(int speed) {
		if(idleState_)
			speed_ = speed;
		update();
	}
	
	public void setLetters(int letters) {
		if(idleState_)
			letters_ = letters;
		update();
	}
	
	public boolean isIdle() {
		return idleState_;
	}
}
