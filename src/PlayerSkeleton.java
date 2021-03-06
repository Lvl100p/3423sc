import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PlayerSkeleton {
	
    //================================================================================
    // Constants
    //================================================================================
	
	private static final int VECTOR_SIZE = 21;
	private static final String FILENAME_VECTOR = "weights.txt";
	private static final String FILENAME_SCORE = "score.txt";
	
    //================================================================================
    // Fields
    //================================================================================
	
	private static int[][] pWidth = {
			{2},
			{1,4},
			{2,3,2,3},
			{2,3,2,3},
			{2,3,2,3},
			{3,2},
			{3,2}
	};
	
	private static int[][] pHeight = {
			{2},
			{4,1},
			{3,2,3,2},
			{3,2,3,2},
			{3,2,3,2},
			{2,3},
			{2,3}
	};
	
	private static int[][][] pBottom = {
		{{0,0}},
		{{0},{0,0,0,0}},
		{{0,0},{0,1,1},{2,0},{0,0,0}},
		{{0,0},{0,0,0},{0,2},{1,1,0}},
		{{0,1},{1,0,1},{1,0},{0,0,0}},
		{{0,0,1},{1,0}},
		{{1,0,0},{0,1}}
	};
	
	private static int[][][] pTop = {
		{{2,2}},
		{{4},{1,1,1,1}},
		{{3,1},{2,2,2},{3,3},{1,1,2}},
		{{1,3},{2,1,1},{3,3},{2,2,2}},
		{{3,2},{2,2,2},{2,3},{1,2,1}},
		{{1,2,2},{3,2}},
		{{2,2,1},{2,3}}
	};
	
	private double[] weightVector;
	private double[] adjustments; //Contains the values of adjustments to be made to each weight 
	private double maxAvgScore; //The maximum average score carried over from previous sessions
	private ExecutorService service = Executors.newCachedThreadPool(); //To manage asynchronous and concurrent threads.

    //================================================================================
    // Constructor
    //================================================================================
	
	public PlayerSkeleton() throws IOException {	
		weightVector = new double[VECTOR_SIZE]; //All values initialized to 0
		adjustments = new double[VECTOR_SIZE];
		Arrays.fill(adjustments, -0.001); //Can be fine-tuned
		readVectorFromFile(FILENAME_VECTOR);
		readScoreFromFile(FILENAME_SCORE);
	}
	
    //================================================================================
    // File IO methods
    //================================================================================
	
	private void writeVectorToFile(String fileName) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(new File(fileName)); //Tries to create the file if it does not exist
		
		for (int i = 0; i < VECTOR_SIZE; i++) {
			pw.println(weightVector[i]);
		}
		
		pw.flush();
		pw.close();
	}
	
	private void writeScoreToFile(double score, String fileName) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(new File(fileName)); //Tries to create the file if it does not exist
		pw.print(score);
		pw.flush();
		pw.close();
	}
	
	private void readVectorFromFile(String fileName) throws IOException {
		File f = new File(fileName);
		
		if (!f.exists()) {
			writeVectorToFile(fileName); //Writes initial weight vector of all zeroes to file
		} else {
			BufferedReader br = new BufferedReader(new FileReader(f));
			
			for (int i = 0; i < VECTOR_SIZE; i++) {
				weightVector[i] = Double.parseDouble(br.readLine());
			}
			
			br.close();
		}
	}
	
	private void readScoreFromFile(String fileName) throws IOException {
		File f = new File(fileName);
		
		if (!f.exists()) {
			writeScoreToFile(0, FILENAME_SCORE);
		} else {
			BufferedReader br = new BufferedReader(new FileReader(f));
			maxAvgScore = Double.parseDouble(br.readLine());
			
			br.close();
		}
	}
	
    //================================================================================
    // Miscellaneous
    //================================================================================
	
	//Improves the current weight vector via an iterative learning method. Uncomment the lines if you want to improve 
	//the vector.
	private void improveVector(int numAdjustments, int numGamesToPlay) {
		int currWeightIndex = 0; //Index of current weight to be adjusted
		
		for (int i = 0; i < numAdjustments; i++) {
			//weightVector[currWeightIndex] += adjustments[currWeightIndex];
			double currAvgScore = playGames(numGamesToPlay);

			if (currAvgScore > maxAvgScore) {
				maxAvgScore = currAvgScore;
			} else { 
				//weightVector[currWeightIndex] -= adjustments[currWeightIndex]; //Undo adjustments
			}
		
			currWeightIndex = (currWeightIndex + 1) % (VECTOR_SIZE);
		}
		
		service.shutdown();
	}
	
	//Plays the specified number of games, and returns the average score. Here, games are played in separate threads 
	//to facilitate parallelization.
	private double playGames(int numGamesToPlay) {
		ArrayList<Future<Integer>> scores = new ArrayList<>(numGamesToPlay);
		
		for (int i = 0; i < numGamesToPlay; i++) {
			Future<Integer> score = service.submit(new Callable<Integer>(){
				@Override
				public Integer call() throws Exception {
					return playGame();
				}
			});
			
			scores.add(score);
		}
		
		int sumOfScores = 0;
		
		for (int i = 0; i < numGamesToPlay; i++) {
			try {
				sumOfScores += scores.get(i).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		return (double) sumOfScores / numGamesToPlay;
	}
	
	//Plays a game and returns the score i.e number of rows cleared. Uncomment the lines if you want to see visual output.
	private int playGame() {
		State s = new State();
		//new TFrame(s);
		
		while(!s.hasLost()) {
			s.makeMove(pickMove(s, s.legalMoves()));
			//s.draw();
			//s.drawNext(0,0);
			
			/*try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		}
		
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
		
		return s.getRowsCleared();
	}
	
	//Out of all the moves that can be made, choose the one that yields the highest reward + utility.
	private int pickMove(State s, int[][] legalMoves) {
		int optimalMove = 0;
		double maxEvaluation = -Double.MAX_VALUE;

		for (int i = 0; i < legalMoves.length; i++) {
			double evaluation = simulate(s, i);

			if (evaluation > maxEvaluation) {
				maxEvaluation = evaluation;
				optimalMove = i;
			}
		}

		return optimalMove;
	}
	
	//Simulates the making of a move and returns the sum of the reward and utility as a result of making that move.
	private double simulate(State state, int move) {
		int[][] simulatedField = copyField(state.getField());
		int[] simulatedTop = Arrays.copyOf(state.getTop(), state.getTop().length);
		//This also modifies simulatedField and simulatedTop.
		double reward = makeMove(move, simulatedField, simulatedTop, state.getNextPiece());
		double utility = calculateUtility(simulatedField, simulatedTop);

		return reward + utility;
	}
	
	//Calculate the utility of a given state, using the linear weighted sum of feature functions.
	private double calculateUtility(int[][] field, int[] top) {
		double utility = 0;
		int[] features = new int[VECTOR_SIZE];
	
		for (int i = 0; i < State.COLS; i++) {
			features[i] = getColHeight(top, i);
		}
	
		for (int i = 0; i < State.COLS - 1; i++) {
			features[State.COLS + i] = getAdjHeightDiff(top, i);
		}
	
		features[19] = getMaxColHeight(top);
		features[20] = getNumOfHoles(field, top);
	
		for (int i = 0; i < VECTOR_SIZE; i++) {
			utility += weightVector[i] * features[i];
		}
	
		return utility;
	}

	// Modified from State.java
	// Return the number of rows cleared, -INF if the game is lost
	private int makeMove(int move, int[][] field, int[] top, int nextPiece) {
		int orient = State.legalMoves[nextPiece][move][State.ORIENT];
		int slot = State.legalMoves[nextPiece][move][State.SLOT];
		
		//height if the first column makes contact
		int height = top[slot]-pBottom[nextPiece][orient][0];
		//for each column beyond the first in the piece
		for(int c = 1; c < pWidth[nextPiece][orient];c++) {
			height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
		}
		
		//check if game ended
		if(height+pHeight[nextPiece][orient] >= State.ROWS) {
			return Integer.MIN_VALUE;
		}
		
		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {
			
			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				field[h][i+slot] = 1;
			}
		}
		
		//adjust top
		for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
			top[slot+c]=height+pTop[nextPiece][orient][c];
		}
		
		int rowsCleared = 0;
		
		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
			//check all columns in the row
			boolean full = true;
			for(int c = 0; c < State.COLS; c++) {
				if(field[r][c] == 0) {
					full = false;
					break;
				}
			}
			//if the row was full - remove it and slide above stuff down
			if(full) {
				rowsCleared++;
				//for each column
				for(int c = 0; c < State.COLS; c++) {
	
					//slide down all bricks
					for(int i = r; i < top[c]; i++) {
						field[i][c] = field[i+1][c];
					}
					//lower the top
					top[c]--;
					while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
				}
			}
		}
		
		return rowsCleared;
	}
	
	//Copy the field (state) into another array, so that moves can be made on the new array without modifying the original
	//array.
	private int[][] copyField(int[][] originalField) {
		int[][] simulatedField = new int[State.ROWS][State.COLS];

		for (int i = 0; i < State.ROWS; i++) {
			for (int j = 0; j < State.COLS; j++) {
				simulatedField[i][j] = originalField[i][j];
			}
		}

		return simulatedField;
	}
	
    //================================================================================
    // Feature functions
    //================================================================================
	
	public int getColHeight(int[] top, int col) {
	    return top[col];
	}
	
	//Returns the absolute difference between adjacent column heights.
	private int getAdjHeightDiff(int[] top, int col) {
	    return Math.abs(top[col] - top[col + 1]);
	}
	
	private int getMaxColHeight(int[] top) {
	    int max = 0;
	    for (int i = 0; i < State.COLS; i++) {
	        if (getColHeight(top, i) > max) {
	            max = getColHeight(top, i);
	        }
	    }
	    
	    return max;
	}
	
	private int getNumOfHoles(int[][] field, int[] top) {
		int holes = 0;

		for (int i = 0; i < State.COLS; i++) { 
			int j = 0; //Row number
			while (j < top[i]) {
				if (field[j][i] == 0) {
					holes++;
				}
				
				j++;
			}
		}
		
		return holes;
	}
	
	public static void main(String[] args) throws IOException {
		PlayerSkeleton p = new PlayerSkeleton();
		int numAdjustments = VECTOR_SIZE * 100; //The total number of adjustments to be made to the weight vector.  
		int numGamesToPlay = 30; //The number of games to play for each adjustment.
		p.improveVector(numAdjustments, numGamesToPlay);
		p.writeVectorToFile(FILENAME_VECTOR);
		p.writeScoreToFile(p.maxAvgScore, FILENAME_SCORE);
	}
}
