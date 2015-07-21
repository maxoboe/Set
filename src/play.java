import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import acm.graphics.*;
import acm.gui.*;
import acm.program.*;
import acm.util.RandomGenerator;

public class play extends Program {
	// private GCompound graphics;
	private JSlider delay; // Slider to control the delay
	private ArrayList<ArrayList<GCompound>> cards; // This grid stores the
													// graphics for the cards
	private ArrayList<ArrayList<String>> cardLabels; // This is maybe necessary
														// to update the
														// graphics?
	private ArrayList<ArrayList<GCompound>> cardMemory;
	private boolean humanFound; // This says whether or not a human has found a
								// valid set
	private boolean computerFound; // to prevent human selected cards from being deleted when the 
	  							   // computer tries to remove a set
	private JLabel userScore; // Label of the user's score
	private JLabel compScore; // Label of the computer's score
	private GCanvas canvas; // The canvas that contains the card images, in the
							// center
	private JFrame frame; // Not sure if I need this, but this would pop out
							// with messages if necessary.
	private Vector<card> humanClicked; // Vector of clicked cards
	private int userCount; // Int of the user's score
	private int compCount; // Int of the computer's score
	private Vector<card> deck; // Starts at 81 cards, decreases
	private ArrayList<ArrayList<card>> board; // Board is 3 rows tall, starts 4
												// col. but col. counts are
												// flexible.

	public void init() {
		initializeDeck();
		cardLabels = new ArrayList<ArrayList<String>>();
		cards = new ArrayList<ArrayList<GCompound>>();
		cardMemory = new ArrayList<ArrayList<GCompound>>();
		humanClicked = new Vector<card>();
		board = new ArrayList<ArrayList<card>>();
		humanFound = false;
		computerFound = false; 
		userCount = 0;
		compCount = 0;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// empty
		}
		add(new JLabel("Set by Max, Cards by Alena"), NORTH);
		canvas = new GCanvas();
		add(canvas, CENTER);
		userScore = new JLabel("" + userCount);
		compScore = new JLabel("" + compCount);
		add(new JLabel("User score:"), WEST);
		add(userScore, WEST);
		add(new JLabel("Computer score:"), WEST);
		add(compScore, WEST);
		makeSlider();
		frame = new JFrame("");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
	}

	private void initializeDeck() {
		deck = new Vector<card>();
		Vector<Character> shades = new Vector<Character>();
		shades.add('s');
		shades.add('m');
		shades.add('e');
		Vector<Character> shapes = new Vector<Character>();
		shapes.add('d');
		shapes.add('w');
		shapes.add('o');
		Vector<Character> colors = new Vector<Character>();
		colors.add('p');
		colors.add('g');
		colors.add('r');
		for (int i = 0; i < 3; i++) { // For three numbers of cards
			for (int j = 0; j < 3; j++) { // For three shades
				for (int k = 0; k < 3; k++) { // For three shapes
					for (int l = 0; l < 3; l++) { // For three colors
						deck.add(new card(i, shades.elementAt(j), shapes
								.elementAt(k), colors.elementAt(l)));
					} // Add the element to the deck. It now has all 81 cards.
				}
			}
		}
	}

	private void makeSlider() {
		add(new JLabel("Computer play time (seconds): "), WEST);
		delay = new JSlider(0, 120);
		delay.setValue(2);
		delay.setMajorTickSpacing(60);
		delay.setMinorTickSpacing(5);
		delay.setPaintLabels(true);
		delay.setPaintTicks(true);
		delay.setSnapToTicks(false);
		add(delay, WEST);
	}

	public void run() {
		setSize(APPLICATION_WIDTH, APPLICAITON_HEIGHT);
		RandomGenerator rando = new RandomGenerator();
		for (int i = 0; i < 3; i++) { // Makes each data structure the right #
										// of rows
			board.add(new ArrayList<card>());
			cards.add(new ArrayList<GCompound>());
			cardLabels.add(new ArrayList<String>());
			cardMemory.add(new ArrayList<GCompound>());
		}
		for (int i = 0; i < 12; i++) { // Make board size 12 cards
			card temp = deck.remove(rando.nextInt(0, deck.size() - 1));
			board.get(i % 3).add(temp);
			// cardLabels.get(i % 3).add("");
		}
		pause(3000); // Wait for application to load
		updateGUI1(); // Sets up initial cards
		canvas.addMouseListener(this);
		//Problem: Must add new cards immediately
		while (!deck.isEmpty()) { // Run until run out of cards
			boolean result = findSet(); // Recursively finds sets
			if (!result) { // If no set found, you need more cards.
				addCards();
				updateGUI1();
			} else { // Otherwise,
				println(delay.getValue());
				pause(delay.getValue() * 1000);
				// Gives the human a chance to find a set.
				if (humanFound) {
					humanFound = false;
					resetBoard();
					result = false;
				}
				if (result) {
					computerFound = true;
					compCount++;
					replaceCards(); // Replaces taken cards
					resetBoard(); // Resets the "used" values in board
				}
				computerFound = false;
				updateGUI1();
			}
		}
		while (findSet()) { // While a valid set is present
			boolean result = true;
			pause(delay.getValue() * 1000);
			if (humanFound) {
				humanFound = false;
				result = false;
			}
			if (result) {
				compCount++;
				replaceCards();
			}
			updateGUI1();
			resetBoard();
		}
		if (compCount >= userCount) {
			JOptionPane.showMessageDialog(frame, "You have been defeated.", "Game Over",
					JOptionPane.PLAIN_MESSAGE);
		} else if (compCount < userCount) {
			JOptionPane.showMessageDialog(frame, "Victory is fleeting. Yay.", "Game Over",
					JOptionPane.PLAIN_MESSAGE);
		}
	}

	// Resets all used values to false
	public void resetBoard() {
		for (int i = 0; i < board.size(); i++) {
			for (int j = 0; j < board.get(i).size(); j++) {
				board.get(i).get(j).setUsed(false);
			}
		}
	}

	public void mouseClicked(MouseEvent event) {
		double x = event.getX();
		double y = event.getY();
		GObject object = canvas.getElementAt(x, y);
		if (object != null) {
			int objectI = -1;
			int objectJ = -1;
			// Checks if the selected object is a card in play
			for (int i = 0; i < cards.size(); i++) {
				for (int j = 0; j < cards.get(i).size(); j++) {
					if (cards.get(i).get(j).equals(object)) {
						objectI = i; // stores the card's info
						objectJ = j;
					}
				}
			}
			if (objectI != -1 && objectJ != -1) {
				card myCard = board.get(objectI).get(objectJ);
				if (myCard.isSelected()) { // Toggle selected/not
					humanClicked.remove(myCard);
					myCard.setSelected(false);
					object.setColor(Color.BLACK);
				} else {
					humanClicked.add(myCard);
					myCard.setSelected(true);
					object.setColor(Color.RED);
				}
				if (humanClicked.size() >= 3) {
					if (checkSet(humanClicked)) { // Your set is valid!
						userCount++; // Increase count
						humanFound = true; // Mark this true for run
						replaceCards();
						updateGUI1();
					}
					humanClicked.clear(); // clear clicked
					for (int i = 0; i < board.size(); i++) { // reset all
						for (int j = 0; j < board.get(i).size(); j++) {
							board.get(i).get(j).setSelected(false);
							cards.get(i).get(j).setColor(Color.BLACK);
						}
					}
				}
			}
		}
	}

	/*
	 * This is my attempt at an updating mechanism - run it once or twice and
	 * see how it fails. I don't know why it doesn't work, but that has yet
	 * to persuade it to do anything right.
	 */
	private void updateGUI1() {
		int maxSize = 5; // Maximum column size
//		for (int i = 0; i < board.size(); i++) {
//			maxSize = Math.max(maxSize, board.get(i).size());
//		}
		double cardWidth = canvas.getWidth() * 9 / 10 * 4 / 5 / maxSize;
		// These calculations make the cards fit in the frame. Very ornate.
		double cardHeight = canvas.getHeight() * 4 / 15 * 9 / 10;
		if (cardWidth > cardHeight * 2 / 3) {
			cardWidth = cardHeight * 2 / 3;
		} else {
			cardHeight = cardWidth * 3 / 2;
		}
		for (int i = 0; i < board.size(); i++) {
			for (int j = 0; j < board.get(i).size(); j++) {
				
				if (j < cardMemory.get(i).size()) { //If cardMemory 
					//is big enough, avoiding an out of bounds exception
					cardGraphic(i, j, cardWidth, cardHeight);
					if (!cardMemory.get(i).get(j).equals(cards.get(i).get(j))) {
						//If the card at this location changed since
						//last time, remove and replace it.
						canvas.remove(cardMemory.get(i).get(j));
					}
				} else { //This means I need to add a new card
					cardGraphic(i, j, cardWidth, cardHeight);
				}
			}
			// Deletes cards if the cardMemory board was larger than the current board
			while (cardMemory.get(i).size() > board.get(i).size()) {
				canvas.remove(cardMemory.get(i).remove(cardMemory.get(i).size() - 1));
			}
		}
		//I then go through and update the cardMemory to match the board
		for (int i = 0; i < board.size(); i++) {
			cardMemory.get(i).clear();
			for (int j = 0; j < board.get(i).size(); j++) {
				cardMemory.get(i).add(cards.get(i).get(j));
			}
		}
		//This just updates the score display
		compScore.setText("" + compCount);
		userScore.setText("" + userCount);
	}

	
	/*
	 * This is where the graphics element is going to go, once everything is all
	 * done and accounted for.
	 */
	private void cardGraphic(int i, int j, double cardWidth, double cardHeight) {
		double x = canvas.getWidth() * 1 / 20 + cardWidth * 5 / 4 * j;
		double y = canvas.getHeight() * 1 / 20 + cardHeight * 5 / 4 * i;
		GRoundRect card = new GRoundRect(x, y, cardWidth, cardHeight);
		card.setFilled(true);
		card.setFillColor(Color.WHITE);
		GCompound compound = new GCompound();
		cards.get(i).add(j, compound);
		canvas.add(compound);
		compound.add(card);
		card myCard = board.get(i).get(j);
		if (myCard.isSelected()) {
			compound.setColor(Color.RED);
		}
		x += cardWidth / 6;
		y += cardHeight / 19; //MAGIC... makes symbols EXACTLY centered
		if (myCard.getCount() == 2) {
			y += (cardHeight / 10);
		} else if (myCard.getCount() == 1) {
			y += (cardHeight * 1.75) / 10;
		} else {
			y += (cardHeight * 3.5) / 10;
		}
		
		for (int k = 0; k < myCard.getCount() + 1; k++) {
			GImage image = new GImage("C:/Users/Max/Documents/eclipse/workspace/set/res/" + myCard.toString() + ".jpg", x, y);
			image.setSize(cardWidth * 2 / 3, cardHeight * 1 / 5);
			compound.add(image);
			y += cardHeight / (2 + myCard.getCount());
		}

	}

	/*
	 * If the size of the board is 12, then this replaces the used cards with
	 * randomly chosen cards from the deck (if cards remain). If the size is not
	 * 12 or the deck is empty, then the cards are just removed from the board.
	 */
	private void replaceCards() {
		RandomGenerator rando = new RandomGenerator();
		int size = 0;
		for (int i = 0; i < board.size(); i++) {
			size += board.get(i).size();
		}
		if (size == 12 && !deck.isEmpty()) {
			for (int i = 0; i < board.size(); i++) {
				for (int j = 0; j < board.get(i).size(); j++) {
					if (!computerFound && board.get(i).get(j).isSelected()) {
						//humanClicked.remove(board.get(i).get(j)); Not necessary
						board.get(i).set(j,
								deck.remove(rando.nextInt(0, deck.size() - 1)));
					}			
					if (!humanFound && board.get(i).get(j).isUsed()) {
						board.get(i).set(j,
								deck.remove(rando.nextInt(0, deck.size() - 1)));
					}
				}
			}
		} else {
			for (int i = 0; i < board.size(); i++) {
				for (int j = board.get(i).size() - 1; j >= 0; j--) {

					if (!computerFound && board.get(i).get(j).isSelected()) {
						//humanClicked.remove(board.get(i).get(j)); Not necessary 
						board.get(i).remove(j);
					}	
					if (!humanFound && board.get(i).get(j).isUsed()) {
						board.get(i).remove(j);
					}
				}
			}
			// redistributes the board back to its original 3 x 4
			if (size > 12) {
				for (int i = 0; i < board.size(); i++) {
					while (board.get(i).size() < 4) {
						board.get(i).add(board.get((i + 1) % 3).remove(board.get((i + 1) % 3).size() - 1));
					}
				}
			}
		}
	}

	private void addCards() {
		if (deck.isEmpty())
			return;
		RandomGenerator rando = new RandomGenerator();
		Vector<card> toAdd = new Vector<card>();
		for (int i = 0; i < 3; i++) {
			toAdd.add(deck.remove(rando.nextInt(0, deck.size() - 1)));
		}
		int i = 0;
		int max = 0;
		while (!toAdd.isEmpty()) {
			i++;
			if (i == 3) {
				i = 0;
				max++;
			}
			if (board.get(i).size() < max) {
				board.get(i).add(board.get(i).size() - 1, toAdd.remove(0));
			}
		}
	}

	private boolean checkSet(Vector<card> visited) {
		card cardA = visited.get(0);
		card cardB = visited.get(1);
		card cardC = visited.get(2);
		boolean colorsMatch = (cardA.getColor() == cardB.getColor() && cardB
				.getColor() == cardC.getColor())
				|| (cardA.getColor() != cardB.getColor()
						&& cardB.getColor() != cardC.getColor() && cardA
						.getColor() != cardC.getColor());
		boolean shapesMatch = (cardA.getShape() == cardB.getShape() && cardB
				.getShape() == cardC.getShape())
				|| (cardA.getShape() != cardB.getShape()
						&& cardB.getShape() != cardC.getShape() && cardA
						.getShape() != cardC.getShape());
		boolean shadesMatch = (cardA.getShade() == cardB.getShade() && cardB
				.getShade() == cardC.getShade())
				|| (cardA.getShade() != cardB.getShade()
						&& cardB.getShade() != cardC.getShade() && cardA
						.getShade() != cardC.getShade());
		boolean countsMatch = (cardA.getCount() == cardB.getCount() && cardB
				.getCount() == cardC.getCount())
				|| (cardA.getCount() != cardB.getCount()
						&& cardB.getCount() != cardC.getCount() && cardA
						.getCount() != cardC.getCount());
		return (colorsMatch && shapesMatch && shadesMatch && countsMatch);
	}

	// Implements recursive backtracking to find sets
	// Returns true when a set is found, leaves the cards used marked as used on
	// the board
	private boolean findSet() {
		Vector<card> visited = new Vector<card>();
		return findSetHelper(visited);
		// if no set found, add three cards and repeat
	}

	private boolean findSetHelper(Vector<card> visited) {
		if (visited.size() == 3) {
			return checkSet(visited);
		}
		for (int i = 0; i < board.size(); i++) {
			for (int j = 0; j < board.get(i).size(); j++) {
				if (!board.get(i).get(j).isUsed()) {
					board.get(i).get(j).setUsed(true);
					visited.add(board.get(i).get(j));
					boolean result = findSetHelper(visited);
					if (result) { // Returns while cards are still marked as
									// used.
						return true;
					}
					visited.remove(board.get(i).get(j));
					board.get(i).get(j).setUsed(false);
				}
			}
		}
		return false;
	}
	
	public static final int APPLICATION_WIDTH = 900;
	public static final int APPLICAITON_HEIGHT = 800;

}

//import java.awt.*;
//import java.awt.event.*;
////import java.io.*;
//import java.util.*;
//import javax.swing.*;
////import javax.swing.event.*;
//import acm.graphics.*;
////import acm.gui.*;
//import acm.program.*;
//import acm.util.RandomGenerator;
//
//public class play extends Program {
//	// private GCompound graphics;
//	private JSlider delay; // Slider to control the delay
//	private ArrayList<ArrayList<GCompound>> cards; // This grid stores the
//													// graphics for the cards
//	private ArrayList<ArrayList<String>> cardLabels; // This is maybe necessary
//														// to update the
//														// graphics?
//	private ArrayList<ArrayList<GCompound>> cardMemory;
//	private boolean humanFound; // This says whether or not a human has found a
//								// valid set
//	private boolean computerFound; // to prevent human selected cards from being deleted when the 
//	  							   // computer tries to remove a set
//	private JLabel userScore; // Label of the user's score
//	private JLabel compScore; // Label of the computer's score
//	private GCanvas canvas; // The canvas that contains the card images, in the
//							// center
//	private JFrame frame; // Not sure if I need this, but this would pop out
//							// with messages if necessary.
//	private Vector<card> humanClicked; // Vector of clicked cards
//	private int userCount; // Int of the user's score
//	private int compCount; // Int of the computer's score
//	private Vector<card> deck; // Starts at 81 cards, decreases
//	private ArrayList<ArrayList<card>> board; // Board is 3 rows tall, starts 4
//												// col. but col. counts are
//												// flexible.
//
//	public void init() {
//		initializeDeck();
//		cardLabels = new ArrayList<ArrayList<String>>();
//		cards = new ArrayList<ArrayList<GCompound>>();
//		cardMemory = new ArrayList<ArrayList<GCompound>>();
//		humanClicked = new Vector<card>();
//		board = new ArrayList<ArrayList<card>>();
//		humanFound = false;
//		computerFound = false; 
//		userCount = 0;
//		compCount = 0;
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (Exception e) {
//			// empty
//		}
//		add(new JLabel("Set by Max, Cards by Alena"), NORTH);
//		canvas = new GCanvas();
//		add(canvas, CENTER);
//		userScore = new JLabel("" + userCount);
//		compScore = new JLabel("" + compCount);
//		add(new JLabel("User score:"), WEST);
//		add(userScore, WEST);
//		add(new JLabel("Computer score:"), WEST);
//		add(compScore, WEST);
//		makeSlider();
//		frame = new JFrame("");
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setResizable(false);
//	}
//
//	private void initializeDeck() {
//		deck = new Vector<card>();
//		Vector<Character> shades = new Vector<Character>();
//		shades.add('s');
//		shades.add('m');
//		shades.add('e');
//		Vector<Character> shapes = new Vector<Character>();
//		shapes.add('d');
//		shapes.add('w');
//		shapes.add('o');
//		Vector<Character> colors = new Vector<Character>();
//		colors.add('p');
//		colors.add('g');
//		colors.add('r');
//		for (int i = 0; i < 3; i++) { // For three numbers of cards
//			for (int j = 0; j < 3; j++) { // For three shades
//				for (int k = 0; k < 3; k++) { // For three shapes
//					for (int l = 0; l < 3; l++) { // For three colors
//						deck.add(new card(i, shades.elementAt(j), shapes
//								.elementAt(k), colors.elementAt(l)));
//					} // Add the element to the deck. It now has all 81 cards.
//				}
//			}
//		}
//	}
//
//	private void makeSlider() {
//		add(new JLabel("Computer play time (seconds): "), WEST);
//		delay = new JSlider(0, 120);
//		delay.setValue(2);
//		delay.setMajorTickSpacing(60);
//		delay.setMinorTickSpacing(5);
//		delay.setPaintLabels(true);
//		delay.setPaintTicks(true);
//		delay.setSnapToTicks(false);
//		add(delay, WEST);
//	}
//
//	public void run() {
//		setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT);
//		RandomGenerator rando = new RandomGenerator();
//		for (int i = 0; i < 3; i++) { // Makes each data structure the right #
//										// of rows
//			board.add(new ArrayList<card>());
//			cards.add(new ArrayList<GCompound>());
//			cardLabels.add(new ArrayList<String>());
//			cardMemory.add(new ArrayList<GCompound>());
//		}
//		for (int i = 0; i < 12; i++) { // Make board size 12 cards
//			card temp = deck.remove(rando.nextInt(0, deck.size() - 1));
//			board.get(i % 3).add(temp);
//			// cardLabels.get(i % 3).add("");
//		}
//		pause(3000); // Wait for application to load
//		updateGUI1(); // Sets up initial cards
//		canvas.addMouseListener(this);
//		//Problem: Must add new cards immediately
//		while (!deck.isEmpty()) { // Run until run out of cards
//			boolean result = findSet(); // Recursively finds sets
//			if (!result) { // If no set found, you need more cards.
//				addCards();
//				updateGUI1();
//			} else { // Otherwise,
////				println(delay.getValue());
//				pause(delay.getValue() * 1000);
//				// Gives the human a chance to find a set.
//				if (humanFound) {
//					humanFound = false;
//					resetBoard();
//					result = false;
//				}
//				if (result) {
//					computerFound = true;
//					compCount++;
//					replaceCards(); // Replaces taken cards
//					resetBoard(); // Resets the "used" values in board
//				}
//				computerFound = false;
//				updateGUI1();
//			}
//		}
//		while (findSet()) { // While a valid set is present
//			boolean result = true;
//			pause(delay.getValue() * 1000);
//			if (humanFound) {
//				humanFound = false;
//				result = false;
//			}
//			if (result) {
//				compCount++;
//				replaceCards();
//			}
//			updateGUI1();
//			resetBoard();
//		}
//	}
//
//	// Resets all used values to false
//	public void resetBoard() {
//		for (int i = 0; i < board.size(); i++) {
//			for (int j = 0; j < board.get(i).size(); j++) {
//				board.get(i).get(j).setUsed(false);
//			}
//		}
//	}
//
//	public void mouseClicked(MouseEvent event) {
//		double x = event.getX();
//		double y = event.getY();
//		GObject object = canvas.getElementAt(x, y);
//		if (object != null) {
//			int objectI = -1;
//			int objectJ = -1;
//			// Checks if the selected object is a card in play
//			for (int i = 0; i < cards.size(); i++) {
//				for (int j = 0; j < cards.get(i).size(); j++) {
//					if (cards.get(i).get(j).equals(object)) {
//						objectI = i; // stores the card's info
//						objectJ = j;
//					}
//				}
//			}
//			if (objectI != -1 && objectJ != -1) {
//				card myCard = board.get(objectI).get(objectJ);
//				if (myCard.isSelected()) { // Toggle selected/not
//					humanClicked.remove(myCard);
//					myCard.setSelected(false);
//					object.setColor(Color.BLACK);
//				} else {
//					humanClicked.add(myCard);
//					myCard.setSelected(true);
//					object.setColor(Color.RED);
//				}
//				if (humanClicked.size() >= 3) {
//					pause(DELAY_TIME);
//					if (checkSet(humanClicked)) { // Your set is valid!
//						userCount++; // Increase count
//						humanFound = true; // Mark this true for run
//						replaceCards();
//						updateGUI1();
//					}
//					humanClicked.clear(); // clear clicked
//					for (int i = 0; i < board.size(); i++) { // reset all
//						for (int j = 0; j < board.get(i).size(); j++) {
//							board.get(i).get(j).setSelected(false);
//							cards.get(i).get(j).setColor(Color.BLACK);
//						}
//					}
//				}
//			}
//		}
//	}
//
//	/*
//	 * This is my attempt at an updating mechanism - run it once or twice and
//	 * see how it fails. I don't know why it doesn't work, but that has yet
//	 * to persuade it to do anything right.
//	 */
//	private void updateGUI1() {
//		int maxSize = 5; // Maximum column size
//		 // cept not really... can go up to 7
//		for (int i = 0; i < board.size(); i++) {
//			maxSize = Math.max(maxSize, board.get(i).size());
//		}
//		double cardWidth = canvas.getWidth() * 9 / 10 * 4 / 5 / maxSize;
//		// These calculations make the cards fit in the frame. Very ornate.
//		double cardHeight = canvas.getHeight() * 4 / 15 * 9 / 10;
//		if (cardWidth > cardHeight * 2 / 3) {
//			cardWidth = cardHeight * 2 / 3;
//		} else {
//			cardHeight = cardWidth * 3 / 2;
//		}
//		for (int i = 0; i < board.size(); i++) {
//			for (int j = 0; j < board.get(i).size(); j++) {
//				
//				if (j < cardMemory.get(i).size()) { //If cardMemory 
//					//is big enough, avoiding an out of bounds exception
//					cardGraphic(i, j, cardWidth, cardHeight);
//					if (!cardMemory.get(i).get(j).equals(cards.get(i).get(j))) {
//						//If the card at this location changed since
//						//last time, remove and replace it.
//						canvas.remove(cardMemory.get(i).get(j));
//					}
//				} else { //This means I need to add a new card
//					cardGraphic(i, j, cardWidth, cardHeight);
//				}
//			}
//			// Deletes cards if the cardMemory board was larger than the current board
//			while (cardMemory.get(i).size() > board.get(i).size()) {
//				canvas.remove(cardMemory.get(i).remove(cardMemory.get(i).size() - 1));
//			}
//		}
//		//I then go through and update the cardMemory to match the board
//		for (int i = 0; i < board.size(); i++) {
//			cardMemory.get(i).clear();
//			for (int j = 0; j < board.get(i).size(); j++) {
//				cardMemory.get(i).add(cards.get(i).get(j));
//			}
//		}
//		//This just updates the score display
//		compScore.setText("" + compCount);
//		userScore.setText("" + userCount);
//	}
//
//	/*
//	 * This is my graphics updating thing that actually works,
//	 * by clearing the screen every call and repopulating the board.
//	 * I don't like this method because it is slow and looks bad.
//	 */
//	private void updateGUI() {
//		int maxSize = 5; // Maximum column size
////		for (int i = 0; i < board.size(); i++) {
////			maxSize = Math.max(maxSize, board.get(i).size());
////		}
//		double cardWidth = canvas.getWidth() * 9 / 10 * 4 / 5 / maxSize;
//		// These calculations make the cards fit in the frame. Very ornate.
//		double cardHeight = canvas.getHeight() * 4 / 15 * 9 / 10;
//		if (cardWidth > cardHeight * 2 / 3) {
//			cardWidth = cardHeight * 2 / 3;
//		} else {
//			cardHeight = cardWidth * 3 / 2;
//		}
//		// Clears the board 
//		for (int i = 0; i < cards.size(); i++) {
//			for (int j = 0; j < cards.get(i).size(); j++) {
//				canvas.remove(cards.get(i).get(j));
//			}
//			cards.get(i).clear();
//		}
//		// Populates every single card.
//		for (int i = 0; i < board.size(); i++) {
//			for (int j = 0; j < board.get(i).size(); j++) {
//				cardGraphic(i, j, cardWidth, cardHeight);
//			}
//		}
//
//		compScore.setText("" + compCount);
//		userScore.setText("" + userCount);
//	}
//
//	/*
//	 * This is where the graphics element is going to go, once everything is all
//	 * done and accounted for.
//	 */
//	private void cardGraphic(int i, int j, double cardWidth, double cardHeight) {
//		double x = canvas.getWidth() * 1 / 20 + cardWidth * 5 / 4 * j;
//		double y = canvas.getHeight() * 1 / 20 + cardHeight * 5 / 4 * i;
//		GRoundRect card = new GRoundRect(x, y, cardWidth, cardHeight);
//		card.setFilled(true);
//		card.setFillColor(Color.WHITE);
//		GCompound compound = new GCompound();
//		cards.get(i).add(j, compound);
//		canvas.add(compound);
//		compound.add(card);
//		card myCard = board.get(i).get(j);
//		if (myCard.isSelected()) {
//			compound.setColor(Color.RED);
//		}
//		x += cardWidth / 6;
//		y += cardHeight / 19; //MAGIC... makes symbols EXACTLY centered
//		if (myCard.getCount() == 2) {
//			y += (cardHeight / 10);
//		} else if (myCard.getCount() == 1) {
//			y += (cardHeight * 1.75) / 10;
//		} else {
//			y += (cardHeight * 3.5) / 10;
//		}
//		
//		for (int k = 0; k < myCard.getCount() + 1; k++) {
//			GImage image = new GImage("C:/Users/Max/Documents/eclipse/workspace/set/res/" + myCard.toString() + ".jpg", x, y);
//			image.setSize(cardWidth * 2 / 3, cardHeight * 1 / 5);
//			compound.add(image);
//			y += cardHeight / (2 + myCard.getCount());
//		}
//
//	}
//
//	/*
//	 * If the size of the board is 12, then this replaces the used cards with
//	 * randomly chosen cards from the deck (if cards remain). If the size is not
//	 * 12 or the deck is empty, then the cards are just removed from the board.
//	 */
//	private void replaceCards() {
//		RandomGenerator rando = new RandomGenerator();
//		int size = 0;
//		for (int i = 0; i < board.size(); i++) {
//			size += board.get(i).size();
//		}
////		if (!humanFound){
////			for (int i = 0; i < cards.size(); i ++){
////				for (int j = 0; j < cards.get(i).size(); j ++){
////					if (board.get(i).get(j).isSelected()){
////						cards.get(i).get(j).setColor(Color.BLUE);
////					}
////				}
////			}
////			pause(DELAY_TIME);
////		}
//		if (size == 12 && !deck.isEmpty()) {
//			for (int i = 0; i < board.size(); i++) {
//				for (int j = 0; j < board.get(i).size(); j++) {
//					//Not sure about this - shouldn't there be no '!'?
//					if (!computerFound && board.get(i).get(j).isSelected()) {
//						//humanClicked.remove(board.get(i).get(j)); Not necessary
//						board.get(i).set(j,
//								deck.remove(rando.nextInt(0, deck.size() - 1)));
//					}			
//					if (!humanFound && board.get(i).get(j).isUsed()) {
//						board.get(i).set(j,
//								deck.remove(rando.nextInt(0, deck.size() - 1)));
//					}
//				}
//			}
//		} else {
//			for (int i = 0; i < board.size(); i++) {
//				for (int j = board.get(i).size() - 1; j >= 0; j--) {
//					if (!computerFound && board.get(i).get(j).isSelected()) {
//						//humanClicked.remove(board.get(i).get(j)); Not necessary 
//						board.get(i).remove(j);
//					}	
//					if (!humanFound && board.get(i).get(j).isUsed()) {
//						board.get(i).remove(j);
//					}
//				}
//			}
//			// redistributes the board back to its original 3 x 4
//			if (size > 12) {
//				for (int i = 0; i < board.size(); i++) {
//					while (board.get(i).size() < 4) {
//						board.get(i).add(board.get((i + 1) % 3).remove(board.get((i + 1) % 3).size() - 1));
//					}
//				}
//			}
//		}
//	}
//
//	private void addCards() {
//		if (deck.isEmpty())
//			return;
//		RandomGenerator rando = new RandomGenerator();
//		Vector<card> toAdd = new Vector<card>();
//		for (int i = 0; i < 3; i++) {
//			toAdd.add(deck.remove(rando.nextInt(0, deck.size() - 1)));
//		}
//		int i = 0;
//		int max = 0;
//		while (!toAdd.isEmpty()) {
//			i++;
//			if (i == 3) {
//				i = 0;
//				max++;
//			}
//			if (board.get(i).size() < max) {
//				board.get(i).add(board.get(i).size() - 1, toAdd.remove(0));
//			}
//		}
//	}
//
//	private boolean checkSet(Vector<card> visited) {
//		card cardA = visited.get(0);
//		card cardB = visited.get(1);
//		card cardC = visited.get(2);
//		boolean colorsMatch = (cardA.getColor() == cardB.getColor() && cardB
//				.getColor() == cardC.getColor())
//				|| (cardA.getColor() != cardB.getColor()
//						&& cardB.getColor() != cardC.getColor() && cardA
//						.getColor() != cardC.getColor());
//		boolean shapesMatch = (cardA.getShape() == cardB.getShape() && cardB
//				.getShape() == cardC.getShape())
//				|| (cardA.getShape() != cardB.getShape()
//						&& cardB.getShape() != cardC.getShape() && cardA
//						.getShape() != cardC.getShape());
//		boolean shadesMatch = (cardA.getShade() == cardB.getShade() && cardB
//				.getShade() == cardC.getShade())
//				|| (cardA.getShade() != cardB.getShade()
//						&& cardB.getShade() != cardC.getShade() && cardA
//						.getShade() != cardC.getShade());
//		boolean countsMatch = (cardA.getCount() == cardB.getCount() && cardB
//				.getCount() == cardC.getCount())
//				|| (cardA.getCount() != cardB.getCount()
//						&& cardB.getCount() != cardC.getCount() && cardA
//						.getCount() != cardC.getCount());
//		return (colorsMatch && shapesMatch && shadesMatch && countsMatch);
//	}
//
//	// Implements recursive backtracking to find sets
//	// Returns true when a set is found, leaves the cards used marked as used on
//	// the board
//	private boolean findSet() {
//		Vector<card> visited = new Vector<card>();
//		return findSetHelper(visited);
//		// if no set found, add three cards and repeat
//	}
//
//	private boolean findSetHelper(Vector<card> visited) {
//		if (visited.size() == 3) {
//			return checkSet(visited);
//		}
//		for (int i = 0; i < board.size(); i++) {
//			for (int j = 0; j < board.get(i).size(); j++) {
//				if (!board.get(i).get(j).isUsed()) {
//					board.get(i).get(j).setUsed(true);
//					visited.add(board.get(i).get(j));
//					boolean result = findSetHelper(visited);
//					if (result) { // Returns while cards are still marked as
//									// used.
//						return true;
//					}
//					visited.remove(board.get(i).get(j));
//					board.get(i).get(j).setUsed(false);
//				}
//			}
//		}
//		return false;
//	}
//	
//	public static final int DELAY_TIME = 1000;
//	public static final int APPLICATION_WIDTH = 900;
//	public static final int APPLICATION_HEIGHT = 800;
//
//}
