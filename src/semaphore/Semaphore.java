package semaphore;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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
	private boolean idleState = true;
	private String image = new String("");
	private String word = "";
	private int highScore = 0;
	private int score = 0;
	private int wins = 0;
	private int losses = 0;
	private int speed = 1;
	private int letters = 3;
	
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
		Path path = FileSystems.getDefault().getPath(pathLocation);
		List<String> file = new ArrayList<String>();
		try {
			file = Files.readAllLines(path, Charset.forName("US-ASCII"));
		} catch (IOException e) {
			System.out.println(path.toString() + " not found!");
			e.printStackTrace();
		}
		
		return file;
	}
	
	/**
	 * Fills the Word Bank using words from file at WORD_BANK_LOCATION
	 * Words are randomly added to the word bank, sorted by how many letters 
	 * are in each word 
	 */
	private void populateWordBank() {
		
		List<String> file = loadFile(Semaphore.WORD_BANK_LOCATION);
		
		//goes through each line in the file and adds to wordbank
		for(String line : file) {
			List<String> wordArray = Arrays.asList(line.split(" "));
			for(String word : wordArray) {
				assignWord(this.wordBank, new Word(word));
			}
		}
		
		for(Integer i : wordBank.keySet()) {
			Collections.shuffle(wordBank.get(i));
		}
	}
	
	private void assignWord(HashMap<Integer, ArrayList<Word>> wordBank, Word word) {
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
		if(idleState == false) 
			return;
					
		idleState = false;
		
		word = randomWord();
		animation = 
				new WordAnimation(this, word, DEFAULT_SPEED - 350*(speed-1));
	}
	
	/**
	 * 
	 * 
	 */
	public void replayWord() {
		if(idleState == true) //no word to replay 
			return;
		
		animation = 
				new WordAnimation(this, word, DEFAULT_SPEED - 350*(speed-1));
	}
	private String randomWord() {		
		if(this.letters != 0) {
			return chooseWord(this.letters);
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
		assignWord(usedWords, word);
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
	
	
	
	public void checkGuess(String guess) {
		if(idleState)
			return; 
		
		idleState = true;
		//*TODO* add checks for invalid guesses, incorrect letters etc.
		animation.cancel();
		if(guess.equalsIgnoreCase(this.word)) {
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
			this.wins ++;
			
			int letterMultiplier = this.letters;
			if(letterMultiplier == 0) //any # of letters
				letterMultiplier = 6;
			score += speed * letterMultiplier;
			image = new String("correct.gif");
		} else {
			this.losses ++;
			image = new String("incorrect.gif");
		}
		
		checkHighScore();
		this.setImage(image);
	}
	
	private void checkHighScore() {
		if(this.score > this.highScore)
			this.highScore = this.score; 
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
			image = model.image;
			word = model.word;
			highScore = model.highScore;
			score = model.score;
			wins = model.wins;
			losses = model.losses;
			speed = model.speed;
			letters = model.letters;
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
		this.image = IMAGE_DIR + imageLocation;
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
				model.setImage("guess.gif");
				this.cancel();
			} else {
				char letter = word.charAt(index);
				String imageLocation = "img" + letter + ".gif";
				model.setImage(imageLocation);
				index++;
			}
		}
	}
	
	//Getters and Setter
	
	public void setSpeed(int speed) {
		if(idleState)
			this.speed = speed;
		update();
	}
	
	public void setLetters(int letters) {
		if(idleState)
			this.letters = letters;
		update();
	}
	
	public boolean isIdle() {
		return idleState;
	}
}
