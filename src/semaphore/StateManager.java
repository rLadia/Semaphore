package semaphore;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class StateManager {
	
	private Semaphore model;
	private HashMap<String, JComponent> components;
	
	public StateManager(Semaphore model) {
		this.model = model;
	}
	
	public void addInteractables(HashMap<String, JComponent> interactables) {
		this.components = interactables;
		
		JTextField input = (JTextField) components.get("Input");
		input.addKeyListener(new KeyboardInput());
		
		JButton guess = (JButton) components.get("Guess");
		guess.addActionListener(new GuessButton());
		
		JButton replay = (JButton) components.get("Replay");
		replay.addActionListener(new ReplayButton());
		
		JButton newWord = (JButton) components.get("NewWord");
		newWord.addActionListener(new NewWordButton());
		newWord.addKeyListener(new KeyboardButtonInput());
				
		for(String s : SemaphoreGUI.speeds) {
			JRadioButton speed = (JRadioButton) components.get(s);
			speed.addActionListener(new SpeedButton());
		}
		
		for(String s : SemaphoreGUI.letters) {
			JRadioButton letter = (JRadioButton) components.get(s);
			letter.addActionListener(new LetterButton());
		}
		
	}
	
	/**
	 * Requests the input data from the GUI and passes it to the model
	 */
	private void makeGuess() {
		JTextField input = (JTextField) components.get("Input");
		String guess = input.getText();
		model.checkGuess(guess);
	}
	
	
	private class GuessButton implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			makeGuess();
		}
	}
	
	private class NewWordButton implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			model.setupNewWord();
		}
	}
	
	private class ReplayButton implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			model.replayWord();
		}
	}
	
	/**
	 * Defines reaction to user selecting a speed option
	 * @author Rodelle
	 *
	 */
	private class SpeedButton implements ActionListener {
		public void actionPerformed(ActionEvent e) {			
			String buttonSpeed = e.getActionCommand();
			int modelSpeed = 1;
			
			if(buttonSpeed.equalsIgnoreCase("Professional"))
				modelSpeed = 4;
			else if(buttonSpeed.equalsIgnoreCase("Advanced"))
				modelSpeed = 3;
			else if(buttonSpeed.equalsIgnoreCase("Medium"))
				modelSpeed = 2;
			else if(buttonSpeed.equalsIgnoreCase("Beginner"))
				modelSpeed = 1;

			model.setSpeed(modelSpeed);
		}
	}
	
	/**
	 * Defines reaction to user selecting the amount of letters 
	 * @author Rodelle
	 *
	 */
	private class LetterButton implements ActionListener {
		public void actionPerformed(ActionEvent e) {			
			String buttonSpeed = e.getActionCommand();
			int letters = 1;
			
			if(buttonSpeed.equalsIgnoreCase("Any"))
				letters = 0;
			else if(buttonSpeed.equalsIgnoreCase("3"))
				letters = 3;
			else if(buttonSpeed.equalsIgnoreCase("4"))
				letters = 4;
			else if(buttonSpeed.equalsIgnoreCase("5"))
				letters = 5;

			model.setLetters(letters);
		}
	}
	
	/**
	 * Allows user to hit 'Enter' when making a guess instead of clicking on
	 * the guess button
	 * @author Rodelle
	 *
	 */
	private class KeyboardInput implements KeyListener {
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case (KeyEvent.VK_ENTER):
				makeGuess();
				break;
			}	
		}
		
		public void keyReleased(KeyEvent arg0) {}
		public void keyTyped(KeyEvent arg0) {}
	}
	
	/**
	 * Allows user to hit 'Enter' to create a new word instead of clicking on
	 * the new word button
	 * @author Rodelle
	 *
	 */
	private class KeyboardButtonInput implements KeyListener {
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case (KeyEvent.VK_ENTER):
				model.setupNewWord();
				break;
			}	
		}
		
		public void keyReleased(KeyEvent arg0) {}
		public void keyTyped(KeyEvent arg0) {}
	}
}
