import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.Random;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.util.Random;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.Arrays;
import java.util.ArrayList;

/*
The algorithm that I have partly used is called Knuth's algorithm (aka five-guess algorithm)
but I did not fully implement the full algorithm.

1. Create an array of all possible codes (0000 to 5555) because there are 6 colours and 4 numbers
2. Let the initial guess be 1122 (color equivalent)
3. Get response of black and white peg value
4. Go through the array of all possible codes
5. Compare the response of first guess to the white and peg value of each of the possible codes
6. If both values match, set that array index value to null
7. Go through the array again and remove null values from the array
8. Let the next guess be the index of array size divided by 2
9. Repeat again from Step 3 until pattern (if found) is stored in the array and game is won 

The maximum guesses that it can take to find the pattern is 13
It is possible that the pattern is not found, in which case you are able to restart the game
Average number of guesses for 50 guesses is 11.76 (excluding when solution was not found)

Source to PDF: http://www.dcc.fc.up.pt/~sssousa/RM09101.pdf
Source to Wiki:https://en.wikipedia.org/wiki/Mastermind_(board_game)#Five-guess_algorithm
*/

public class Mastermind extends JFrame  implements ActionListener {
        
	int width, height, numColors, numGuesses;
	JButton[][] colouredPegs, whites, blacks;       
	JButton[] computerGuess;
	static int[] hiddenGuess;
	JButton guess = new JButton("Next Computer guess");
	JPanel colouredPanel = new JPanel();
	JPanel whitesPanel = new JPanel();
	JPanel blacksPanel = new JPanel();
	JPanel computerGuessPanel = new JPanel();
	Random rand;
	final int totalNumbers = 4;
	final int totalPossibilities = 1296;
	int delete = 0;
	int[][] allCodes = new int[totalPossibilities][totalNumbers];

	/*
	check whether the elements in the original
	generated array equals to one in the guess
	return the number of elements that are in
	the correct position
	*/
	static int blacks (int[] one, int[] two) {
		int val = 0;

		for (int i = 0; i < one.length; i++) {
		  if (one[i] == two[i]) {
		  	val++;
		}
	}
	        return val;
	}
	
    /*
    following method compares the arrays of the guess
    and the pattern to see whether there is an element
    in the guess that is of correct colour, but is in
    the wrong position.
    */
	static int whites (int[] one, int[] two) {
		boolean found;
		int val = 0;
		int[] oneA = new int[one.length]; // create a new array with length of first array
		int[] twoA = new int[one.length]; // create a new array with length of first array
		
		for (int i = 0; i < one.length; i++) {
			oneA[i] = one[i]; // initialize the same elements in array into new array
			twoA[i] = two[i]; // initialize the same elements in array into new array
		}

		for (int i = 0; i < one.length; i++) {
		if (oneA[i] == twoA[i]) {
			oneA[i] = 0 - i - 10;
			twoA[i] = 0 - i - 20;
		}
	}
		
		for (int i = 0; i < one.length; i++) { 
		  found = false;
		  for (int j = 0; j < one.length && !found; j++) { 
		   if (i != j && oneA[i] == twoA[j]) { // check whether colour is the same, but in the wrong order
		   	val++; // increase the value of whites
		   	oneA[i] = 0 - i - 10;
		   	twoA[j] = 0 - j - 20;
		   	found = true;
		   }
		  }
		}
		return val; // return the number of white tiles
	}

	static Color choose(int i) {
        // return the colour based on i value
		  if (i == 0) return Color.red;
		  if (i == 1) return Color.green;
		  if (i == 2) return Color.blue;
		  if (i == 3) return Color.orange;
		  if (i == 4) return Color.pink; 
		  if (i == 5) return Color.cyan;
		  else return Color.yellow; // return yellow if none of the above
		}
		
	public Mastermind(int h, int w, int c) {
		width = w; // set width
		height = h; // set height
		numColors = c; // set number of colours
		numGuesses = 0; // initialise number of guesses to 0

        /*
        try and catch block to make sure that the program works on
        operating systems other than Windows
        */
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
		System.out.println("The look and feel classes are not present on this system.");	
		} catch (ClassNotFoundException e) {
		System.out.println("The class was not found.");
		} catch (InstantiationException e) {
			System.out.println("The class object cannot be instantiated.");
		} catch (IllegalAccessException e) {
			System.out.println("Access denied.");
		}

		hiddenGuess = new int[width];
		rand = new Random(); 
		colouredPegs = new JButton[height][width];
		whites = new JButton[height][width];
		blacks = new JButton[height][width];
		computerGuess = new JButton[width];
		colouredPanel.setLayout(new GridLayout(height, width));
		blacksPanel.setLayout(new GridLayout(height, width));
		whitesPanel.setLayout(new GridLayout(height, width));
		computerGuessPanel.setLayout(new GridLayout(1, width));
		colouredPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		whitesPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		blacksPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		// k = 0, width = 4
		// for loop is to obtain four random colours as a guess
		System.out.print("Generated pattern to guess: ");
        for (int k = 0; k < width; k++) {
		  computerGuess[k] = new JButton(); // create a new button for each colour
		  computerGuess[k].setVisible(true); // do not make the guess visible
 		  computerGuessPanel.add(computerGuess[k]); // add the button to the panel
		  hiddenGuess[k] = rand.nextInt(numColors);// select a random colour
		  computerGuess[k].setBackground(choose(hiddenGuess[k])); // set background of each button to that colour
		System.out.print(hiddenGuess[k]);
        }
        System.out.println("\n");
		
		for (int i = 0; i < height; i++) {
		for (int j = 0; j < width; j++) {
		   allCodes[i][j] = 0;
		   colouredPegs[i][j] = new JButton(); // create a new button
		   colouredPegs[i][j].setBackground(choose(allCodes[i][j])); // set the background to selected colour
		   
		   whites[i][j] = new JButton(); // create a new button
		   whites[i][j].setVisible(false); // do not make it visible
		   whites[i][j].setBackground(Color.white); // set the background to white
		   blacks[i][j] = new JButton(); // create a new button
		   blacks[i][j].setVisible(false); // do not make it visible
		   blacks[i][j].setBackground(Color.black); // set the background to black
		   
		   colouredPanel.add(colouredPegs[i][j]); // add the colours to the panel
		   whitesPanel.add(whites[i][j]); // add the whites to the whites panel
		   blacksPanel.add(blacks[i][j]); // add the blacks to the black panel
		   if (i > 0) {
		   colouredPegs[i][j].setVisible(false); 
		 }	
		}
		 }
		
		setLayout(new BorderLayout());
		add(blacksPanel, "West"); // move the panel of blacks to left
		add(colouredPanel, "Center"); // move your guess panel to center
		add(whitesPanel, "East"); // move whites panel to right
		JPanel guessPanel = new JPanel(); // create a new panel
		guessPanel.setLayout(new FlowLayout()); // set the layout of the new panel
		guessPanel.add(guess); // add the button guess to the panel
		add(guessPanel,"South"); // place the guess button at the bottom of the program
		JPanel topPanel = new JPanel(); // create a new panel
		topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // set the border
		topPanel.setLayout(new GridLayout(1, 3)); // set the layout to grid
		topPanel.add(new JLabel("Blacks", JLabel.CENTER)); // add a new label to the panel
		topPanel.add(computerGuessPanel); // add the button where computer generated random colours as a guess
		topPanel.add(new JLabel("Whites", JLabel.CENTER)); // add a new label to the panel
		add(topPanel, "North"); // put the panel at the top of the program
		setDefaultCloseOperation(3);
		setTitle("Mastermind"); // set the title of the program
		setMinimumSize(new Dimension(width * 50, height * 50)); // set the minimum size of the program
		pack(); 
		setVisible(true); // make everything visible
		guess.addActionListener(this);


        /*
        following generates an array that contains
        all of the possible patterns (0000 to 5555)
        which will later be used to guess the pattern
        */
		int[] split = new int[totalNumbers];
		for(int i = 0; i < split.length; i++){
			split[i] = 0;
		}
		
		for (int i = 0; i < totalPossibilities; i++) {
			for(int j = 0; j < totalNumbers; j++){
				allCodes[i][j] = split[j];
			}
			
			split[totalNumbers - 1] = split[totalNumbers - 1] + 1;
			
			for(int j = totalNumbers; j > 1; j--){
				if (split[j - 1] == 6) {
					split[j - 1] = 0;
					split[j - 2] = split[j - 2] + 1;
				}
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
    int whiteThings, blackThings, whiteThings2, blackThings2;

    if (allCodes.length < 1) { // make sure that the array does not go out of bounds
    	System.out.println("Unfortunately, No solution was found.");
    	lostWin("Would you like to play again?", "No solution was found!", JOptionPane.YES_NO_OPTION);
    } else {
    if (numGuesses == 0) { // if it's the first guess, guess [1, 1, 2, 2]
		whiteThings = whites(allCodes[266], hiddenGuess); // guess 1, 1, 2, 2 as first guess
		blackThings = blacks(allCodes[266], hiddenGuess); // guess 1, 1, 2, 2 as first guess
        System.out.println("Guessing: " + Arrays.toString(allCodes[266]));
	} else {
		whiteThings = whites(allCodes[allCodes.length/2], hiddenGuess); // guess length/2 index as next guess
		blackThings = blacks(allCodes[allCodes.length/2], hiddenGuess); // guess length/2 index as next guess
        System.out.println("Guessing: " + Arrays.toString(allCodes[allCodes.length/2])); // print to console
    }

		for (int i = 0; i < width; i++) {
			colouredPegs[numGuesses][i].setEnabled(false); // allow to select a colour for each button
		}
		

		// if the guess matches with the original pattern, you win the game
		if (blackThings == width) {  
			for (int i = 0; i < blackThings; i++) {
				blacks[numGuesses][i].setVisible(true); // show all 4 black buttons to show that pattern was found
			}
		for (int i = 0; i < width; i++) {
			computerGuess[i].setVisible(true); // show the computers pattern
		}

        System.out.println("Total number of guesses taken: " + numGuesses); // print total amount of computer guesses
		lostWin("Would you like to play again?", "You won!", JOptionPane.YES_NO_OPTION);
	} 

        /*
        loop through the whole code array and get number of white
        and black values for each element of the array
        */
		for (int i = 0; i < allCodes.length; i++) {
		whiteThings2 = whites(allCodes[i], hiddenGuess); // get the amount of 
		blackThings2 = blacks(allCodes[i], hiddenGuess);

        /*
        if any of the array element white and black values match with
        the original guess values, set that array index to null, I will
        later delete the null values from the array continuously
        to get to the final pattern
        */
		if (whiteThings == whiteThings2 && blackThings == blackThings2) {
			allCodes[i] = null;
			delete++;
		}
	}

    /*
    Following goes through the code array (uses a temporary array)
    and if there is a null value for an index in the array, it adds
    the elements into the temporary array.

    The final array will contain no null values, so the guessing algorithm
    can continue guessing with new values
    */
	int[][] tempArray = new int[totalPossibilities - delete][totalNumbers];
	int j = 0;

	for (int i = 0; i < allCodes.length; i++) {
		if (allCodes[i] != null) {
			for (int k = 0; k < totalNumbers; k++) {
				tempArray[j][k] = allCodes[i][k];
			}
			j++;
		}
	}
	allCodes = tempArray;

        // if user has not reached maximum of 10 guesses, this is active
		if (numGuesses < height-1) {
		  for (int i = 0; i < whiteThings; i++) 
		  	whites[numGuesses][i].setVisible(true); // show the amount of whites
		  for (int i = 0; i < blackThings; i++)
		  	blacks[numGuesses][i].setVisible(true); // show the amount of blacks
		  numGuesses++; // increase the number of guesses by one for each guess
		  for (int i = 0; i < width; i++) 
		  	colouredPegs[numGuesses][i].setVisible(true);
		} else {  // if user reached maximum of 10 guesses, this is active
		  	lostWin("Would you like to play again?", "You lost!", JOptionPane.YES_NO_OPTION);
		}
	}
	}

	public void lostWin(String text, String title, int optionType) {
		int n = JOptionPane.showConfirmDialog(this, text, title, optionType);
			if (n == JOptionPane.NO_OPTION) {
				System.exit(0);
			} else { 
				dispose(); // remove the frame
				new Mastermind(height, width, numColors); // start again
			}
	}		

	public static void main(String[] args) {
		new Mastermind(15, 4, 7);
	}
}