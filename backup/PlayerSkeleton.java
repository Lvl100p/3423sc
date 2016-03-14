
public class PlayerSkeleton {

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
		
		return 0;
	}
	
	/**
	 * @param s
	 * @param col
	 * @return corresponding column height of the wall
	 */
	public int getColHeight(State s, int col) {
	    return s.getTop()[col];
	}
	
	/**
	 * @param s
	 * @param col
	 * @return absolute difference between adjacent column heights
	 */
	public int getAdjColHeight(State s, int col) {
	    return s.getTop()[col] - s.getTop()[col + 1];
	}
	
	/**
	 * @param s
	 * @return maximum column height
	 */
	public int getMaxColHeight(State s) {
	    int max = 0;
	    for (int i = 0; i < s.COLS; i++) {
	        if (getColHeight(s, i) > max) {
	            max = getColHeight(s, i);
	        }
	    }
	    
	    return max;
	}
	
	public static void main(String[] args) {
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
	
}
