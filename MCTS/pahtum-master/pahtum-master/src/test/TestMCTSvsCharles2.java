package test;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;

import util.Tuple;

import ai.charles2.Charles_2;
import ai.minimax.MiniMax;
import ai.montecarlo.MonteCarlo;

import core.Board;
import core.Player;
import core.Rules;

/**
 * This class enables testing Monte-Carlo with Heuristic against Charles_2 AI. 
 * The test is constituted of 50 sets, each set is played as a match of 2 games 
 * where after one players switch sides.
 * @author kg687
 *
 */
public class TestMCTSvsCharles2 {

	/**
	 * Run test case. During the test, after each game is finished it updates 
	 * the results to the external file in order to keep track about results 
	 * even when the execution of the program would be interrupted. Before 
	 * running this application make sure there is no previous record of matches 
	 * (this application will append new finding, and therefore they might 
	 * become difficult to distinguish from previous ones). 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		//Counters which keep track of number of wins/draws that occurred.
		int montecaroloWinCount = 0, charlesWinCount = 0, drawCount = 0;
		
		//Board that is used in games.
		Board board = null;
		
		//Board that keeps copy of initial position, used to quickly reset the 
		//board before new game take place.
		Board initialPosition = null;
		
		//Array of all boards that are used in the test case.
		Board[] boardCollection = null;
		
		//Index of player that is entitled to make a move.
		int currentIndex = 0;
		
		//Number of all moves that was made during the game.
		int numberOfMove = 0;
		
		//Players participating in the test case.
		Player[] players =  {
			new Player("Charles_2", "charles2", "w", 0),
			new Player("MCTS", "MCTS", "b", 50)
		};
		
		//Number of total moves. It is used to check whether the game is in 
		//terminate state or not (the game finishes when there is no empty 
		//fields in the board).
		int totalNumberOfMoves = 46;
		
		//Condition variable which checks whether MiniMax is taking charge over
		//MC in terms of making moves. It occurs when the board is nearly full.  
		boolean capable = true;
		
		//Load board.
		try {
			FileInputStream fis = new FileInputStream("50_boards_3.sav");
			ObjectInputStream ois = new ObjectInputStream(fis);
			boardCollection = (Board[]) ois.readObject();
		} catch(Exception e) {
			System.err.println("Error" + e.getMessage());
		}
		
		//Check whether number of boards is OK. If not terminate program.
		if(boardCollection.length != 50) {
			System.err.println("Error, boardCollection has " + 
					boardCollection.length + " board.");
			System.exit(0);
		}
		
		//Boards are OK. Proceed to testing.
		for(int testIndex = 1; testIndex <= 100; ++testIndex) {
			//Reset settings.
			currentIndex = 0;
			numberOfMove = 0;
			capable = true;

			//Swap players.
			Player tmp = players[0];
			players[0] = players[1];
			players[1] = tmp;

			//Reset the board to an initial state. When index is odd generate a 
			//new random board.
			if(testIndex % 2 == 1) {
				//Load a new board.
				board = boardCollection[(Integer) testIndex/2];
				initialPosition = board.duplicate();
			} else {
				//Reset the board.
				board = initialPosition.duplicate();
			}

			//Run a single game.
			while(numberOfMove < totalNumberOfMoves) {
				if(players[currentIndex].getType().equals("charles2")) {
					//Charles_2 AI makes random move.
					Charles_2 charles2 = new Charles_2(
							players[currentIndex].getColor(), board);
					Tuple<Integer, Integer> move = charles2.getMove();
					board.makeMove(move, players[currentIndex].getColor());

					//Increment number of currently made moves.
					++numberOfMove;

					//Adjust index of current player.
					currentIndex = (currentIndex + 1) % 2;
				} else if(players[currentIndex].getType().equals("MCTS")) {
					Tuple<Integer, Integer> move;
					if(capable) {
						//Monte-Carlo AI to play.
						MonteCarlo mc = new MonteCarlo(
								board.duplicate(), 
								players[currentIndex].getColor(), 
								numberOfMove, 
								totalNumberOfMoves);
						try {
							move = mc.uct(players[currentIndex].
									getSimulationNumber());
						} catch(Exception e) {
							capable = false;
							MiniMax mm = new MiniMax(players[currentIndex].
									getColor().equals("w") ? "b" : "w", board);
							move = mm.getMove();
						}
					} else {
						MiniMax mm = new MiniMax(players[currentIndex].
								getColor().equals("w") ? "b" : "w", board);
						move = mm.getMove();
					}

					board.makeMove(move, players[currentIndex].getColor());

					//Increment number of currently made moves.
					++numberOfMove;

					//Adjust index of current player.
					currentIndex = (currentIndex + 1) % 2;
				}
			} //end of single game.
			
			String gameOutcome = Rules.calculateScore(board);
			BufferedWriter output = new BufferedWriter(
					new FileWriter("results_MCTSvsCharles.txt", true));
			output.append("Match #" + testIndex);
			output.newLine();
			output.append("Player 1: " + players[0].getName() + 
					" Player 2: " + players[1].getName());
			output.newLine();
			
			//Append the result to the text file and update counters..
			if(gameOutcome.equals("0")) {
				//The game was a draw.
				++drawCount;
				//Append information to the file.
				output.append("Result: draw");
				output.newLine();
				output.close();
			} else {
				//One side won the game.
				if(gameOutcome.equals(players[0].getColor())) {
					//Add note about the winner to the file.
					output.append("Result: " + players[0].getName() + " won");
					//Increment appropriate counter.
					if(players[0].getName().equals("Charles_2")) {
						++charlesWinCount;
					} else {
						++montecaroloWinCount;
					}
				} else {
					//Add note about the winner to the file.
					output.append("Result: " + players[1].getName() + " won");
					//Increment appropriate counter.
					if(players[1].getName().equals("Charles_2")) {
						++charlesWinCount;
					} else {
						++montecaroloWinCount;
					}
				}
				output.newLine();
				output.close();
			}			
		} //End of the test case.
		
		//Append total outcome of the test case to the file.
		BufferedWriter output1 = new BufferedWriter(
				new FileWriter("results_MCTSvsCharles.txt", true));
		output1.append("========================================");
		output1.newLine();
		output1.append("*Summary*");
		output1.newLine();
		output1.append("Draw occured: " + drawCount);
		output1.newLine();
		output1.append("Monte-Carlo (UCT) victories: " + montecaroloWinCount);
		output1.newLine();
		output1.append("Charles_2 AI victories: " + charlesWinCount);
		output1.newLine();
		output1.append("========================================");
		output1.close();

	} //End of main method.
}
