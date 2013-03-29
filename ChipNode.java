package Board;
public class ChipNode
{
	static final int UNVISIT = 0;//status of node from bfs
	static final int VISITED = 1;
	static final int VISITING = 2;
	private int x;//xy coordinate
	private int y;
	private int color;
	private ChipNode[] edges = new ChipNode[9];//keep track of its edges (chips that are paired with this node)
	private int visited;
	private int num;//number of edges
	/*
	 constructore for the chipnode. sets up its x, y coordiantes as well as its color. 
	 its edges are empty when the nod is frst creted
	 */
	public ChipNode(int x, int y, int color)
	{
		this.x = x;
		this.y = y;
		this.color = color;
		this.visited = 0;
	}
	/*
	 getter and setter for the private fields.
	 */
	public void setVisit(int v){
		visited = v; 
	}
	public int getVisit(){
		return visited;
	}
	
	public ChipNode[] getEdges(){
		return edges;
	}
	public int getColor()
	{
		return color;
	}
	public int getX()
	{
		return x;
	}
	public int getY(){
		return y;
	}
	public int getNum(){
		return num;
	}
	public ChipNode getNode(int i){
		return edges[i];
	}
	//add an edge to the current chipnode. takes in another chipnode and adds it to its edge list
	public void addEdge(ChipNode c)
	{
		edges[num] = c;
		num++;
	}
	//removes specific node from the edge list. takes in the node that is to be removed
	//it goes through the edge list, finds it, removes it from the list, and shift everything on the list over by one
	//does not change the counter for number of node since it will be used by the step move, so every time this method is called, it will be
	//followed by addEdge method
	public void removeEdge(ChipNode c){
		int count = 0;
		while(edges[count] != null)
		{
			if(edges[count].x == c.x && edges[count].y == c.y && edges[count].color == c.color){
				for(int i = count+1; i < num; i++){
					edges[i-1] = edges[i];
				}
				edges[num] = null;
				num--;
			}
			count++;
		}
	}
	//takes in a chipnode and check if it is the same node as the current node
	public boolean equals(ChipNode c){
		return(x == c.x && y == c.y && color == c.color);
	}
	//checks if the current node is in the goal area for the given color
	public boolean goal1(){
		if(color == Board.BLACK)
			return y == 0;
		else if(color == Board.WHITE)
			return x == 0;;
		return false;
	}
	public boolean goal2(){
		if(color == Board.BLACK)
			return y == 7;
		else if(color == Board.WHITE)
			return x == 7;
		return false;
	}
	//toString for testing
	public String toString()
	{
		String str = "x: " + x + "y:" + y + "color: "+ color + "\nedges:";
		for(int i = 0; i < num; i++)
		{
			str += "(" + edges[i].x + " ," + edges[i].y + ") ";
		}
		return str;
		
	}
}