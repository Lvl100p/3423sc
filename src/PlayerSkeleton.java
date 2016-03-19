import static java.lang.Math.pow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class PlayerSkeleton {
	private static final int VECTOR_SIZE = 21;
	private static final String FILENAME = "weights.txt";
	
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

	public static final Double Lambda = 0.6;
	
	public PlayerSkeleton() throws IOException {	
		weightVector = new double[VECTOR_SIZE]; //All values initialized to 0
		readVectorFromFile(weightVector, FILENAME);
	}
	
	private void writeVectorToFile(double[] weightVector, String fileName) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(new File(fileName)); //Tries to create the file if it does not exist
		
		for (int i = 0; i < VECTOR_SIZE; i++) {
			pw.println(weightVector[i]);
		}
		
		pw.flush();
		pw.close();
	}
	
	private void readVectorFromFile(double[] weightVector, String fileName) throws IOException {
		File f = new File(fileName);
		
		if (!f.exists()) {
			writeVectorToFile(weightVector, fileName); //Writes initial weight vector of all zeroes to file
		} else {
			BufferedReader br = new BufferedReader(new FileReader(f));
			
			for (int i = 0; i < VECTOR_SIZE; i++) {
				weightVector[i] = Double.parseDouble(br.readLine());
			}
			
			br.close();
		}
	}
	
	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		
		// strategy: given a set of all possible legal moves, choose the one that maximizes the sum of the 
		// reward and utility functions
		
		// reward: number of rows cleared
		// utility: linear weighted sum of features
		
		// State class has public instance method getRowsCleared which probably can be used to get the value 
		// of the reward function for a corresponding state
		
		// Utility functions: column height information can probably be accessed via public instance method
		// 					  getTop of State class
		//
		//                    possible way to find holes: for each column, an empty square in a column is a 
		//                    hole if it is below the top of the column and directly to its left or right
		//                    are non empty squares? not sure about exact definition of hole
		//					
		//					  for now the weight vector can be hardcoded
		//					  eventually we can implement the weight vector learning function as described
		//					  in page 15 of ref2.pdf
		
		int optimalMove = 0;
		double maxEvaluation = -Double.MAX_VALUE;

		for (int i = 0; i < legalMoves.length; i++) {

			double evaluation = simulate(s, i, weightVector);

			if (evaluation > maxEvaluation) {
				maxEvaluation = evaluation;
				optimalMove = i;
			}
		}

		return optimalMove;
	}
	
	private double simulate(State state, int move, double[] vector) {
		int[][] simulatedField = copyField(state.getField());
		int[] simulatedTop = Arrays.copyOf(state.getTop(), state.getTop().length);
		double reward = makeMove(move, simulatedField, simulatedTop, state.getNextPiece());
		double utility = calculateUtility(simulatedField, simulatedTop, vector);

		return reward + utility;
	}
	
	private double calculateUtility(int[][] field, int[] top, double[] vector) {
		int utility = 0;
		int[] features = new int[VECTOR_SIZE];
	
		for (int i = 0; i < State.COLS; i++) {
			features[i] = getColHeight(top, i);
		}
	
		for (int i = 0; i < State.COLS - 1; i++) {
			features[State.COLS + i] = getAdjColHeight(top, i);
		}
	
		features[19] = getMaxColHeight(top);
		features[20] = getNumOfHoles(field, top);
	
		for (int i = 0; i < VECTOR_SIZE; i++) {
			utility += vector[i] * features[i];
		}
	
		return utility;
	}
	

	// Modified from State.java
	// Return the number of rows cleared, -1 if the game is lost
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
			return -1;
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

	private int[][] copyField(int[][] originalField) {
		int[][] simulatedField = new int[State.ROWS][State.COLS + 1];

		for (int i = 0; i < State.ROWS; i++) {
			for (int j = 0; j < State.COLS; j++) {
				simulatedField[i][j] = originalField[i][j];
			}
		}

		return simulatedField;
	}
	
	/**
	 * @param top
	 * @param col
	 * @return corresponding column height of the wall
	 */
	public int getColHeight(int[] top, int col) {
	    return top[col];
	}
	
	/**
	 * @param top
	 * @param col
	 * @return absolute difference between adjacent column heights
	 */
	public int getAdjColHeight(int[] top, int col) {
	    return Math.abs(top[col] - top[col + 1]);
	}
	
	/**
	 * @param top
	 * @return maximum column height
	 */
	public int getMaxColHeight(int[] top) {
	    int max = 0;
	    for (int i = 0; i < State.COLS; i++) {
	        if (getColHeight(top, i) > max) {
	            max = getColHeight(top, i);
	        }
	    }
	    
	    return max;
	}
	
	/**
	 * @param field
	 * @param top
	 * @return number of holes
	 */
	public int getNumOfHoles(int[][] field, int[] top) {
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
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
	
	
	//From Matthew
	private double temporalDiff(State state, State nextState){
		
		return 0.0;
	}

	//From Matthew
	private double sumtemporalDiff(int s, int k){
		
		Double LambdaConstant = pow(Lambda, s-k);
		
		return 0.0;
	}
	
}
