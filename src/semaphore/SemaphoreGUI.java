package semaphore;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.imgscalr.Scalr;

public class SemaphoreGUI implements java.util.Observer {
	
	private JFrame frame = new JFrame("Semaphore");
	private String currentImage = new String("");
	private HashMap<String, JComponent> interactables = new HashMap<String, JComponent>();
	
	private int imgSize_ = 200;
	
	private boolean newAnimation = false;
	
	//Options
	private JLabel speedLabel = new JLabel("Speed");
	private ArrayList<JRadioButton> speedButton = new ArrayList<JRadioButton>();
	public static String[] speeds = { "Beginner", "Medium", "Advanced", "Professional", };
	
	private JLabel letterLabel = new JLabel("Letters");
	private ArrayList<JRadioButton> letterButton = new ArrayList<JRadioButton>();
	public static String[] letters = { "3", "4", "5", "Any",};
	
	//Central Animation
	private BufferedImage image_;
	private JLabel animation = new JLabel();
	
	//User Input
	private JButton guessButton = new JButton("Guess");
	private JButton newWordButton = new JButton("New Word");
	private JTextField guessInput = new JTextField();
	private final JButton replayButton = new JButton("Replay Word");
	
	//Score
	private JLabel scoreLabel = new JLabel("Score");
	private JLabel currentScoreLabel = new JLabel("Score");
	private JLabel highScoreLabel = new JLabel("High Score");
	private JLabel winLabel = new JLabel("Wins");
	private JLabel lossLabel = new JLabel("Losses");
	private JLabel winDivider = new JLabel("/");
	
	private JLabel scoreCount = new JLabel("0");
	private JLabel winCount = new JLabel("0");
	private JLabel highScoreCount = new JLabel("0");
	private JLabel lossCount = new JLabel("0");
	
	private final JPanel wordPanel = new JPanel();
	private final JLabel correctWordLabel = new JLabel();

	private enum Style {
		Input,
		Label,
		SmallLabel,
		Title,
	}
	
	/**
	 * Sole constructor
	 * @param model
	 * @param controller
	 */
	public SemaphoreGUI(Semaphore model, StateManager controller) {
		model.addObserver(this); //pulls updates from model
							
		createButtons(controller);
		
		frame.setSize(500, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		createMainLayout();
		model.notifyObservers();
	}

	private void createButtons(StateManager controller) {
		for(String s : speeds) {
			JRadioButton speed = new JRadioButton(s);
			speed.setActionCommand(s);
			speedButton.add(speed);
		}
		
		speedButton.get(0).setSelected(true); //defaults to beginner speed
		
		for(String s : letters) {
			JRadioButton letter = new JRadioButton(s);
			letter.setActionCommand(s);
			letterButton.add(letter);
		}
		
		letterButton.get(0).setSelected(true); //defaults to 3 letters
		
		replayButton.setEnabled(false);
		guessButton.setEnabled(false);
		
		interactables.put("Input", guessInput);
		interactables.put("Replay", replayButton);
		interactables.put("NewWord", newWordButton);
		interactables.put("Guess", guessButton);
		
		for(JRadioButton d : speedButton) {
			interactables.put(d.getText(), d);
		}
		
		for(JRadioButton l : letterButton) {
			interactables.put(l.getText(), l);
		}
		
		controller.addInteractables(interactables);
	}
	
	/**
	 * Creates and sets fonts and alignments
	 */
	private void setStyle(Component item, Style style) {
		Font input = new Font("Arial", Font.BOLD, 16);
		Font label = new Font("Arial", Font.BOLD, 16);
		Font smallLabel = new Font("Arial", Font.PLAIN, 16);
		
		switch(style) {
		case Input:
			item.setFont(input);
			break;
		case Label:
			item.setFont(label);
			break;
			
		case SmallLabel:
			item.setFont(smallLabel);
		default:
			break;
		}
	}
		
	JPanel scorePanel() {
		
		JPanel score = new JPanel();
		score.setBorder(null);
		GridBagLayout gbl_score = new GridBagLayout();
		gbl_score.columnWidths = new int[] {40, 10, 50};
		gbl_score.rowHeights = new int[] {14, 14, 14, 14};
		gbl_score.columnWeights = new double[]{0.0, 0.0, 0.0};
		gbl_score.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		score.setLayout(gbl_score);
		
		setStyle(scoreLabel, Style.Label);
		
		GridBagConstraints gbc_scoreLabel = new GridBagConstraints();
		gbc_scoreLabel.gridwidth = 3;
		gbc_scoreLabel.gridx = 0;
		gbc_scoreLabel.gridy = 0;
		score.add(scoreLabel, gbc_scoreLabel);
		//scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		setStyle(highScoreLabel, Style.SmallLabel);
		GridBagConstraints gbc_highScoreLabel = new GridBagConstraints();
		gbc_highScoreLabel.anchor = GridBagConstraints.WEST;
		gbc_highScoreLabel.gridx = 0;
		gbc_highScoreLabel.gridy = 1;
		score.add(highScoreLabel, gbc_highScoreLabel);
		setStyle(highScoreCount, Style.SmallLabel);
		GridBagConstraints gbc_highScoreCount = new GridBagConstraints();
		gbc_highScoreCount.anchor = GridBagConstraints.WEST;
		gbc_highScoreCount.gridx = 2;
		gbc_highScoreCount.gridy = 1;
		score.add(highScoreCount, gbc_highScoreCount);
		setStyle(currentScoreLabel, Style.SmallLabel);
		GridBagConstraints gbc_currentScoreLabel = new GridBagConstraints();
		gbc_currentScoreLabel.anchor = GridBagConstraints.WEST;
		gbc_currentScoreLabel.gridx = 0;
		gbc_currentScoreLabel.gridy = 2;
		score.add(currentScoreLabel, gbc_currentScoreLabel);
		setStyle(scoreCount, Style.SmallLabel);
		GridBagConstraints gbc_scoreCount = new GridBagConstraints();
		gbc_scoreCount.anchor = GridBagConstraints.WEST;
		gbc_scoreCount.gridx = 2;
		gbc_scoreCount.gridy = 2;
		score.add(scoreCount, gbc_scoreCount);
		GridBagConstraints gbc_winLabel = new GridBagConstraints();
		gbc_winLabel.gridx = 0;
		gbc_winLabel.gridy = 3;
		score.add(winLabel, gbc_winLabel);
		setStyle(winLabel, Style.SmallLabel);
		GridBagConstraints gbc_lossLabel = new GridBagConstraints();
		gbc_lossLabel.gridx = 2;
		gbc_lossLabel.gridy = 3;
		score.add(lossLabel, gbc_lossLabel);
		setStyle(lossLabel, Style.SmallLabel);
		
		setStyle(winCount, Style.SmallLabel);
		GridBagConstraints gbc_winCount = new GridBagConstraints();
		gbc_winCount.gridx = 0;
		gbc_winCount.gridy = 4;
		score.add(winCount, gbc_winCount);
		setStyle(winDivider, Style.SmallLabel);
		GridBagConstraints gbc_winDivider = new GridBagConstraints();
		gbc_winDivider.gridx = 1;
		gbc_winDivider.gridy = 4;
		score.add(winDivider, gbc_winDivider);
		setStyle(lossCount, Style.SmallLabel);		
		GridBagConstraints gbc_lossCount = new GridBagConstraints();
		gbc_lossCount.gridx = 2;
		gbc_lossCount.gridy = 4;
		score.add(lossCount, gbc_lossCount);
				
		return score;
	}
	
	/**
	 * Options are aligned vertically in a column
	 * @return
	 */
	JPanel optionPanel() {
		JPanel option = new JPanel();
		option.setLayout(new BoxLayout(option, BoxLayout.PAGE_AXIS));
		
		ButtonGroup letterGroup = new ButtonGroup();
		
		setStyle(letterLabel, Style.Label);
		option.add(letterLabel, 0);
		for(JRadioButton l : letterButton) {
			letterGroup.add(l);
			option.add(l);
		}
		
		option.add(Box.createRigidArea(new Dimension(0, 10)));
		
		ButtonGroup speedGroup = new ButtonGroup();
		
		setStyle(speedLabel, Style.Label);
		option.add(speedLabel);
		
		for(JRadioButton d : speedButton) {
			speedGroup.add(d);
			option.add(d);
		}
		
		return option;
	}
	
	/**
	 * New Word button is at the top 
	 * User input and Guess button are side by side
	 * @return
	 */
	JPanel userPanel() {
		
		JPanel userPanel = new JPanel();
		userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.PAGE_AXIS));
		
		userPanel.add(wordPanel);
		wordPanel.add(newWordButton);
		newWordButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		wordPanel.add(replayButton);
		
		JPanel inputPanel = new JPanel(new FlowLayout());
		
		setStyle(guessInput, Style.Input);
		guessInput.setColumns(25);
		
		inputPanel.add(guessInput);
		inputPanel.add(guessButton);
		
		userPanel.add(inputPanel);
		
		return userPanel;
	}
	
	/**
	 * Organizes the frame's layout
	 */
	private void createMainLayout() {
		
		JPanel line_start = new JPanel();
		line_start.add(optionPanel());
		
		JPanel line_end = new JPanel();
		line_end.add(scorePanel());
		
		JPanel page_end = new JPanel();
		page_end.add(userPanel());
		
		JPanel center = new JPanel(new BorderLayout());
		animation.setHorizontalAlignment(SwingConstants.CENTER);
				
		center.add(BorderLayout.CENTER, animation);
		correctWordLabel.setHorizontalAlignment(SwingConstants.CENTER);
		center.add(BorderLayout.PAGE_END, correctWordLabel);
		
		frame.getContentPane().add(BorderLayout.LINE_START, line_start);
		frame.getContentPane().add(BorderLayout.CENTER, center);
		frame.getContentPane().add(BorderLayout.LINE_END, line_end);
		frame.getContentPane().add(BorderLayout.PAGE_END, page_end);
		
		frame.setVisible(true);
		
		imgSize_ = smallestSide(center);
		center.addComponentListener(new FrameResize());
	}
	
	
	/**
	 * returns the smaller of the component's width and height
	 * @param c
	 * @return
	 */
	private int smallestSide(Component c) {
		int width = c.getWidth();
		int height = c.getHeight();
		return width < height ? width : height;
	}
	
	private class FrameResize implements ComponentListener {
		@Override
		public void componentResized(ComponentEvent e) {
			//*TODO* return size needed to fill the component
			imgSize_ = smallestSide(e.getComponent());
			setImage(image_);
		}

		@Override
		public void componentHidden(ComponentEvent arg0) {}
		@Override
		public void componentMoved(ComponentEvent arg0) {}
		@Override
		public void componentShown(ComponentEvent arg0) {}
		
	}
	
	private BufferedImage loadImage(String path) {
        InputStream imageStream = this.getClass().getResourceAsStream(path);
        BufferedImage img = null;
		try {
			img = ImageIO.read(imageStream);
		} catch (IOException e) {
			e.printStackTrace();
			img = null;
		}
        return img;
    }
	
	/*
	private BufferedImage loadImage(String pathLocation) {
		URL url = getClass().getResource(pathLocation);
		try {
			//BufferedImage img = ImageIO.read(new File(pathLocation));
			BufferedImage img = ImageIO.read(url);
			return img;
		} catch (IOException e) {
			System.out.println(pathLocation + " not found.");
			e.printStackTrace();
		}
		return null;
	}
	*/
	
	//*TODO* thread the loading of the image
	/**
	 * Changes the icon of a label
	 * @param label
	 * @param path
	 */
	private void setDrawing(String pathLocation) {
		BufferedImage img = loadImage(pathLocation);
		if (img == null) 
			return; //*TODO* log error, load default image
		image_ = img;
		setImage(image_);
	}

	private void setImage(BufferedImage img) {
		img = Scalr.resize(img, imgSize_);
		animation.setIcon(new ImageIcon(img));
	}
	
	private void toggleOptions(boolean status) {
		for(JRadioButton l : letterButton) {
			l.setEnabled(status);
		}
		
		for(JRadioButton s : speedButton) {
			s.setEnabled(status);
		}
	}
	
	@Override
	public void update(Observable observable, Object o) {
		Semaphore.State state = (Semaphore.State) o; 
		if(!state.image.equals(currentImage)) {
			this.setDrawing(state.image);
		}
		
		if(state.isIdle) { 
			if(!state.word.equals("")) {
				correctWordLabel.setText("The correct word is: " + state.word);
			}
			replayButton.setEnabled(false);
			guessButton.setEnabled(false);
			newWordButton.setEnabled(true);
			newWordButton.requestFocusInWindow();
			this.newAnimation = false;
			toggleOptions(true);
		}
		else {
			correctWordLabel.setText(" ");
			toggleOptions(false);
			guessButton.setEnabled(true);
			replayButton.setEnabled(true);
			newWordButton.setEnabled(false);
			guessInput.requestFocusInWindow();
			
			if(newAnimation == false) 
				guessInput.setText("");
			this.newAnimation = true;
		}
		
		if(state.letters == 0)
			letterButton.get(3).setSelected(true); 
		else
			letterButton.get(state.letters-3).setSelected(true);
		speedButton.get(state.speed-1).setSelected(true);
		
		winCount.setText(Integer.toString(state.wins));
		lossCount.setText(Integer.toString(state.losses));
		
		scoreCount.setText(Integer.toString(state.score));
		highScoreCount.setText(Integer.toString(state.highScore));
	}
	
}
