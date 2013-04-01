/* SimpleBoard.java */
package Board;
import player.*;
import java.lang.Math.*;
/**
 *  Simple class that implements an 8x8 game board with three possible values
 *  for each cell:  0, 1 or 2.
 *
 *  DO NOT CHANGE ANY PROTOTYPES IN THIS FILE.
 **/

public class Board {
  private final static int DIMENSION = 8;
  public static final int BLACK = 0;
  public static final int WHITE = 1;
  public static final int EMPTY = 2;
  public static final int CHIPS = 10;
  private static final int STRAIGHT = 0;
  private static final int DIAGONOL = 1; 
  private static final int UNPAIR = 0;
  private static final int REPAIR = 1;
  private static final int BIG_NUMBER = 1000000;
  private int[][] grid;
  private int[] chip = new int[2];//keeps track of number of chips used
  private int[][] dist = new int[10][10];//keeps track of distance from given nodes. used by eval
  private int me;//determing color of chip that im using
  private int you;
  private ChipNode[][] chips = new ChipNode[2][10];//keeps track of nodes for black and white chips

  

  /*
    Construct a new board in which all cells are zero.
    assign chip color to player
   */
  
  public Board(int color) {
    grid = new int[DIMENSION][DIMENSION];
    me = color;
    you = Math.abs(color-1);
    for(int i = 0; i < DIMENSION; i++)
		for(int j = 0; j < DIMENSION; j++)
		{
			grid[i][j] = EMPTY;
		}
  }

  /**
   *  Set the cell (x, y) in the board to the given value mod 3.
   *  @param value to which the element should be set (normally 0, 1, or 2).
   *  @param x is the x-index.
   *  @param y is the y-index.
   *  @exception ArrayIndexOutOfBoundsException is thrown if an invalid index
   *  is given.
   **/

  public void setElementAt(int x, int y, int value) {
    grid[x][y] = value % 3;
    if (grid[x][y] < 0) {
      grid[x][y] = grid[x][y] + 3;
    }
  }
  public int getChipCount(int color){
	  return chip[color];
  }
  /**
   *  Get the valued stored in cell (x, y).
   *  @param x is the x-index.
   *  @param y is the y-index.
   *  @return the stored value (between 0 and 2).
   *  @exception ArrayIndexOutOfBoundsException is thrown if an invalid index
   *  is given.
   */

  public int elementAt(int x, int y) {
    return grid[x][y];
  }

  /**
   *  Returns true if "this" SimpleBoard and "board" have identical values in
   *    every cell.
   *  @param board is the second SimpleBoard.
   *  @return true if the boards are equal, false otherwise.
   */

  public boolean equals(Object board) {
    // Replace the following line with your solution.  Be sure to return false
    //   (rather than throwing a ClassCastException) if "board" is not
    //   a SimpleBoard.
	if(board.getClass() != this.getClass())
		return false;
	for(int i = 0; i < DIMENSION; i++)
		for(int j = 0; j < DIMENSION; j++)
			if(((Board)board).elementAt(i,j) != grid[i][j])
			    return false;
      return true;
  }

  /**
   *  Returns a hash code for this SimpleBoard.
   *  @return a number between Integer.MIN_VALUE and Integer.MAX_VALUE.
   */

  public int hashCode() {
    // Replace the following line with your solution.
	int code = Integer.MIN_VALUE;
	int pow = 3;
	for(int i = 0; i < DIMENSION; i++)
		for(int j = 0; j < DIMENSION; j++){
			code += pow*grid[i][j];
			pow *= 3;
		}
	  return code;
  }
  //print out the layout of board and the chips and its edges for testing
  public String toString(){
      String str = "";
      for(int i = 0; i < DIMENSION ; i++)
	  {		
		  for(int j = 0; j < DIMENSION ; j++)
		  {
		    str+= grid[j][i] + " ";
		  }
			str += "\n";
	  }
      for(int i = 0; i < 2; i++)
    	  for(int j = 0; j < chip[i]; j++)
    		  str += "\n"+ chips[i][j];
	  return str;
  }
  /*
   * takes in a move m, and the color of the chip that is being used
   * if it is an add move, place the new chip on the board, and update the edges for each existing chipNode
   * since adding a chip might disrupt previous pairs
   * if it is a step move, remove and update the old chip, then add and update the new chip
   */
  public void makeMove(Move m, int color)
  {
	  if(m.moveKind == Move.ADD){
		  ChipNode c = new ChipNode(m.x1, m.y1,color);
		  pairFix(c, UNPAIR);
		  addChip(c, chip[color]);
	  }
	  else if(m.moveKind == Move.STEP){
		  ChipNode oldC = findChip(m.x1,m.y1);
		  ChipNode c = new ChipNode(m.x2, m.y2,color);  
		  
		  pairFix(oldC, REPAIR);
		  System.out.println("after repair:\n\n" + this + "\n\n");
		  pairFix(c, UNPAIR);
		  int index = removeChips(oldC);
		  
		  addChip(c, index);
		  chip[color]--;
	  }
  }
  //takes in a chipnode and where to add on the chiplist(in case of step--will fill in the null spot created by remove)
  //update the grid, chip counts, and check with existing nodes if it can be paired up with them
  public void addChip(ChipNode c, int index){
	  grid[c.getX()][c.getY()] = c.getColor();
	  chips[c.getColor()][index] = c;
	  for(int i = 0; i < chip[c.getColor()]; i++){
		  makePair(c, chips[c.getColor()][i],false);
	  }
	  
	  chip[c.getColor()]++;
  }
  //remove the given chip c. update the borad, remove the edges of other chips, and then remove itself
  public int removeChips(ChipNode c){
	  int result = -1;
	  grid[c.getX()][c.getY()] = EMPTY;
	  for(int i = 0; i < chip[c.getColor()]; i++){
		  chips[c.getColor()][i].removeEdge(c);
		  if(chips[c.getColor()][i].equals(c)){
			  chips[c.getColor()][i] = null;
			  result = i;
		  }
	  }
	  return result;
  }
  //determine if c1 and c2 and form a connection based on type of move (diagonol or straight) by checking the slope of the two as well as 
  //checking if there is any node in between the two nodes.
  private boolean isPairHelp(ChipNode c1, ChipNode c2, int moveType)
	{
	  	if(c1.equals(c2))
	  		return false;
		int nodex1, nodey1;
		int x1 = c1.getX();
		int x2 = c2.getX();
		int y1 = c1.getY();
		int y2 = c2.getY();
		for(int i = 0; i< 2; i++)
			for(int j = 0; j < chip[i]; j++){
				nodex1 = chips[i][j].getX();
				nodey1 = chips[i][j].getY();
				if(nodex1 == x1 && nodey1 == y1){
					continue;
				}
				switch(moveType){
					case STRAIGHT:
						if(x1 == x2){
							if(nodex1 == x1 && nodey1 > Math.min(y1,y2) && nodey1 < Math.max(y1, y2))
								return false;
						}
						else if(y1 == y2){
							if(nodey1 == y1 && nodex1< Math.max(x1,x2) && nodex1 > Math.min(x1,x2))
								return false;
							}
							else{
								return false;
							}
						break;
					case DIAGONOL:
						if(y1 == y2 || x1 == x2)
							return false;
						if(Math.abs((double)(x2-x1)/(y2-y1)) == 1.0){
							if(((double)(x1-nodex1)/(y1-nodey1) == (double)(nodex1-x2)/(nodey1-y2))&&
									( nodex1< Math.max(x1,x2) && nodex1 > Math.min(x1,x2)))
								return false;
						}	
						else{
							return false;
						}
						break;
				}
			}

		
		return true;
	}
  //call ispair helper with 2 different move type. added another boolean force to see if we want to force a connection (use for repair and unpair)
  //add the edge to each other's edge list
  private void makePair(ChipNode c1, ChipNode c2,boolean force)
  {
	  	if(force){
	  		c1.addEdge(c2);
			c2.addEdge(c1);
	  	}
	  	else{
	  		if(isPairHelp(c1,c2,0) || isPairHelp(c1,c2,1))
  			{
  				c1.addEdge(c2);
  				c2.addEdge(c1);
  			}
	  	}
  }
  //remove the nodes from each other's edge list
  private void breakPair(ChipNode c1, ChipNode c2){
	  c1.removeEdge(c2);
	  c2.removeEdge(c1);
  }
  //get the specific chip by using its position
  private ChipNode findChip(int x, int y){
	  for(int i = 0; i < 2; i++)
		  for(int j = 0; j < chip[i]; j++){
			  if(chips[i][j].getX() == x &&  chips[i][j].getY() == y)
				  return chips[i][j];
		  }
	  return null;
  }
  //update the edge relationship caused by removing node c and opperation(add or remove). 
  //extend from the current node c and make pairs base their slope
  private void pairFix(ChipNode c, int opp)
  {
	  int rad=1;
	  int x = c.getX();
	  int y = c.getY();
	  ChipNode[][] pair = new ChipNode[4][2];
	  for(int i = 0; i < 4;i++)
		  for(int j = 0; j < 2; j++)
			  pair[i][j] = new ChipNode(0,0,5);
	  while(x+rad < DIMENSION){
		  if(grid[x+rad][y] != EMPTY){
			  pair[0][0] = findChip(x+rad, y);
			  break;
		  }
		  rad++;
	  }
	  rad = 1;
	  while(x-rad >= 0){
		  if(grid[x-rad][y] != EMPTY){
			  pair[0][1] = findChip(x-rad, y);
			  break;
		  }
	  rad++;
	  }
	  rad = 1;
	  while(y-rad >= 0){
		  if(grid[x][y-rad] != EMPTY){
			  pair[1][1] = findChip(x, y-rad);
			  break;
		  }
	  rad++;
	  }	  
	  rad = 1;
	  while(y+rad < DIMENSION){
		  if(grid[x][y+rad] != EMPTY){
			  pair[1][0] = findChip(x, y+rad);
			  break;
		  }
	  rad++;
	  }	  	  
	  rad = 1;
	  while(y-rad >= 0 && x-rad >= 0){
		  if(grid[x-rad][y-rad] != EMPTY){
			  pair[2][1] = findChip(x-rad, y-rad);
			  break;
		  }
	  rad++;
	  }	  
	  rad = 1;
	  while(y+rad < DIMENSION && x+rad < DIMENSION){
		  if(grid[x+rad][y+rad] != EMPTY){
			  pair[2][0] = findChip(x+rad, y+rad);
			  break;
		  }
	  rad++;
	  }	  
	  rad = 1;
	  while(y-rad >= 0 && x+rad < DIMENSION){
		  if(grid[x+rad][y-rad] != EMPTY){
			  pair[3][1] = findChip(x+rad, y-rad);
			  break;
		  }
	  rad++;
	  }	  
	  rad = 1;
	  while(y+rad < DIMENSION && x-rad >= 0){
		  if(grid[x-rad][y+rad] != EMPTY){
			  pair[3][0] = findChip(x-rad, y+rad);
			  break;
		  }
	  rad++;
	  }	  
	  for(int i = 0; i < 4; i++){
		  if(pair[i][0].getColor() == pair[i][1].getColor()){
			  if(opp == REPAIR){
				  makePair(pair[i][0], pair[i][1],true);
			  }
			  else if(opp == UNPAIR){
				  breakPair(pair[i][0], pair[i][1]);
			  }
		  }
	  }
  }
  //return the index of the given chip in its chipset. used for finding path between nodes (indexed based on position on the list)
  private int getIndex(ChipNode c, int color){
	  for(int i = 0; i < chip[color]; i++){
		  if(chips[color][i].equals(c))
			  return i;
	  }
	  return -1;
  }
  //evaluate the board using color(black and white)
  //loop through the edge and assign score based on the path distance. path is weighted by its distance
  private int evaluateHelp(int color)
  {
	  int score = 0;
	  for(int i = 0; i < chip[color]; i++){
		  dfs(chips[color][i], 0, 2, i, color, 0,0);
		  
	  }
	  for(int i = 0; i < chip[color]; i++)
		  for(int j = 0; j < i; j++){
			  score += dist[i][j] * dist[i][j];
		  }
	  if(color == you)
		  score = -score;
	  if(score <= -BIG_NUMBER)
	  	  score = -score *2;
	  return score;
	  
  }
  //calculate eval of white and black, and subtract from each other
  public int evaluate(){
	  return evaluateHelp(Board.BLACK)+ evaluateHElp(Board.BLACK);
  }
  //start-starting node, sum= current distrance from the starting node, and slope for the limitation on how nodes can connect
  //keep root and color to update distance matrix
  private void dfs(ChipNode start, int sum, int slope, int root, int color, int flag1, int flag2)
  {
	  if(flag1 == 0 && start.goal1())
		  flag1 = 1;
	  else if(flag1 != 0 && start.goal1())
		  flag1 = 2;
	  if(flag2 == 0 && start.goal2())
		  flag2 = 1;
	  else if(flag2 != 0 && start.goal2())
		  flag2 = 2;
	  
	  int index = getIndex(start, color);
	  
	  if (start.getVisit() == ChipNode.VISITED)
		  return;
	  if(sum >= 5 && flag1 == 1 && flag2 == 1){
		  dist[root][index] = BIG_NUMBER;
		  return;
	  }
	  start.setVisit(ChipNode.VISITED);
	  if( dist[root][index] < sum){
		  dist[root][index] = sum;
		  
	  }
	  for(int i = 0; i < start.getNum(); i++){
		  ChipNode n = start.getNode(i);
		  if(n == null)
			  break;
		  try{//in case of dividing by 0
			  if(slope == 2){
				  dfs(n, sum+1, (n.getY()-start.getY())/(n.getX()-start.getX()), root,color, flag1, flag2);
			  }
		  
			  else{
				  if((n.getY()-start.getY())/(n.getX()-start.getX()) != slope){
					  dfs(n, sum+1, (n.getY()-start.getY())/(n.getX()-start.getX()), root, color, flag1,flag2);
				  }
			  }
		  }catch(ArithmeticException e){
			  dfs(n, sum+1, 2, root, color, flag1, flag2);
		  }
	  }
	  start.setVisit(ChipNode.UNVISIT);

  }
  public String evalTest(){
	  String str = "";
	  for(int i = 0; i <10; i++){
		  for(int j = 0; j < 10; j++)
		  {
			  str += dist[i][j];
		  }
		  str += "\n";
	  }
	  return str;
  }
}
