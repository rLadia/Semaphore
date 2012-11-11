package semaphore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class Semaphore extends java.util.Observable {	
	private WordAnimation animation_;
	private Random random = new Random();
	
	//words organized by number of letters
	private HashMap<Integer, ArrayList<Word>> 
		wordBank_ = new HashMap<Integer, ArrayList<Word>>();
	
	//private HashMap<Integer, ArrayList<Word>> 
	//	usedWords = new HashMap<Integer, ArrayList<Word>>();
	
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
	
	private int DEFAULT_SPEED = 1500; //milliseconds
	
	//Directories
	private static final String IMAGE_DIR = "images/"; //image directory
	private static final String WORD_BANK_LOCATION = "data/data.kyt";

	/**
	 * Entry point
	 * @param args
	 * @throws IOException 
	 * @throws SecurityException 
	 */
	public static void main(String[] args) throws SecurityException, IOException {
		Semaphore model = new Semaphore();
		StateManager controller = new StateManager(model);
		new SemaphoreGUI(model, controller);		
	}
	
	private BufferedReader loadFile(String pathLocation) {
		InputStream stream = getClass().getResourceAsStream(pathLocation);
		return new BufferedReader(new InputStreamReader(stream));
	}
	
	/**
	 * Returns a list of strings pulled from a file
	 * @param pathLocation - the location of the file
	 * @return
	 * @throws IOException if unable to load the file at pathLocation
	 */
	private List<String> createListFromFile(String pathLocation) 
			throws IOException {
		List<String> wordList = new ArrayList<String>();
		BufferedReader file = loadFile(pathLocation);
		String line;
		while ((line = file.readLine()) != null) {
			wordList.addAll(Arrays.asList(line.split(" ")));
		} 
		file.close();
		return wordList;
	}
	
	/**
	 * Fills the Word Bank using words from file at WORD_BANK_LOCATION
	 * Words are randomly added to the word bank, sorted by how many letters 
	 * contained in each word 
	 */
	private void populatewordBank_() {
		List<String> file;
		try {
			file = createListFromFile(WORD_BANK_LOCATION);
			
			//goes through each line in the file and adds to wordBank_
			for(String line : file) {
				List<String> wordArray = Arrays.asList(line.split(" "));
				for(String word : wordArray) {
					putWord(this.wordBank_, new Word(word));
				}
			}
			
			for(Integer i : wordBank_.keySet()) {
				Collections.shuffle(wordBank_.get(i));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds word to the proper array in wordBank_. If there is no existing  
	 * array, one is created and the word is added to it
	 * @param wordBank_
	 * @param word
	 */
	private void putWord(HashMap<Integer, ArrayList<Word>> wordBank_, Word word) {
		int letterCount = word.word.length();
		if(!wordBank_.containsKey(letterCount))
			wordBank_.put(letterCount, new ArrayList<Word>());
		wordBank_.get(letterCount).add(word);
	}
	
	/**
	 * Sole constructor
	 */
	public Semaphore() {
		this.setImage("splash.gif");
		populatewordBank_();
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
		animation_ = 
				new WordAnimation(this, word_, DEFAULT_SPEED - 350*(speed_-1));
	}
	
	/**
	 * Restarts the current animation. If program is idle, this function 
	 * does nothing
	 */
	public void replayWord() {
		if(idleState_ == true) //no word to replay 
			return;
		
		animation_ = 
				new WordAnimation(this, word_, DEFAULT_SPEED - 350*(speed_-1));
	}
	
	/**
	 * 
	 * @return
	 */
	/*
	private String randomWord() {		
		if(letters_ != 0) {
			return chooseWord(letters_);
		} else { //can use any set of letters
			int totalWords = 0;
			for (Integer i : wordBank_.keySet()) {
				totalWords += wordBank_.get(i).size();
			}
			int index = random.nextInt(totalWords);
			for (Integer i : wordBank_.keySet()) {
				ArrayList<Word> wordList = wordBank_.get(i);
				
				if(index < wordList.size()) 
					return chooseWord(i);
				
				index -= wordList.size();
			}
		}
		return chooseRandomWord(1); //should never reach here
	}*/
	
	//*TODO* improve this process
	/** Returns a random word from the list of words with an amount of 
	 * letters equal to letterIndex. Adds the word to the usedWord bank
	 * preventing it from being used again
	 * @param letterIndex, the number of letters in the word
	 * @return
	 */
	/*
	private String chooseRandomWord(int letterIndex) {
		System.out.println("Letter Index: "+ letterIndex);
		int wordIndex = random.nextInt(wordBank_.get(letterIndex).size());
		Word word = wordBank_.get(letterIndex).get(wordIndex);
		putWord(usedWords, word);
		wordBank_.get(letterIndex).remove(wordIndex);
		checkwordBank_();
		System.out.println("Word: " + word.word);
		return word.word;
	}*/
	
	/*
	private void checkwordBank_() {
		for(Integer i : wordBank_.keySet()) {
			if(wordBank_.get(i).isEmpty()) {
				wordBank_.put(i, usedWords.get(i));
				Collections.shuffle(wordBank_.get(i));
				usedWords.put(i, new ArrayList<Word>());
			}
		}
	}
	*/
	
	/**
	 * 
	 * @return
	 */
	private String randomWord() {			
		int letters = letters_;
		if(letters == 0) { //can use any set of letters
			int size = wordBank_.keySet().size();
			int randomIndex = random.nextInt(size);
			int index = 0;
			for(Integer i : wordBank_.keySet()) {
				if (index == randomIndex){
					letters = i;
					break;
				}
				index ++;
			}
		}
		
		int wordIndex = (int) (Math.random() * wordBank_.get(letters).size());
		String word = wordBank_.get(letters).get(wordIndex).word;
		if(word.equals(word_)) //no duplicate words
			return randomWord();
		return word;
	}
	
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
		animation_.cancel();
		if(guess.equalsIgnoreCase(word_)) {
			updateScore(true);
		} else {
			updateScore(false);
		}
	}
	
	/**
	 * Wins increases score by speed*letters. Random number of letters 
	 * multiplies score by 10. 
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
