# 3423sc

This document contains any information that could be potentially useful to the team. 
Feel free to update this document when necessary.

This is a basic Tetris simulation.

### Files
***
* State.java: tetris simulation
* TFrame.java: frame that draws the board
* TLabel.java: drawing library
* PlayerSkeleton.java: setup for implementing a player
	
State.java:
This is the tetris simulation.  It keeps track of the state and allows you to 
make moves.  The board state is stored in field (a double array of integers) and
is accessed by getField().  Zeros denote an empty square.  Other values denote
the turn on which that square was placed.  NextPiece (accessed by getNextPiece)
contains the ID (0-6) of the piece you are about to play.

* ID 0: xx  
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;xx


* ID 1: xxxx

* ID 2: &nbsp;&nbsp;&nbsp;x  
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;xxx

* ID 3: x  
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;xxx

* ID 4: &nbsp;x  
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;xxx

* ID 5: &nbsp;&nbsp;xx  
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;xx

* ID 6: xx  
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;xx

Moves are defined by two numbers: the SLOT, the leftmost column of the piece and
the ORIENT, the orientation of the piece.  legalMoves() gives an n x 2 int array
containing the n legal moves.  A move can be made by specifying the two
parameters as either 2 ints, an int array of length 2, or a single int
specifying the row in the legalMoves array corresponding to the appropriate move.

It also keeps track of the number of lines cleared - accessed by getRowsCleared().

draw() draws the board.
drawNext() draws the next piece above the board
clearNext() clears the drawing of the next piece so it can be drawn in a different
	slot/orientation

TFrame.java:
This extends JFrame and is instantiated to draw a state.
It can save the current drawing to a .png file.
The main function allows you to play a game manually using the arrow keys.

TLabel.java:
This is a drawing library.

PlayerSkeleton.java:
An example of how to implement a player.
The main function plays a game automatically (with visualization).
