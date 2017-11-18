package com.mygdx.holowyth.pathfinding;

/**
 * 
 * A new Node which corresponds to an exploration of a Vertex (note a Vertex can be explored multiple times)
 *
 */
public class Node {	
	public int vertexID;
		
	public float costToGetHere;
	/**
	 * Underestimate of cost to reach goal, squared
	 */
	public float h;  
	public String coordinates;
	public float f; //Parameter used by A*
	
	public Node(int ID, float costToGetHere, float hSquared, int graphWidth){
		this.vertexID = ID;
		this.costToGetHere = costToGetHere;
		this.h = hSquared;		
		this.coordinates = ID%graphWidth + ", " + ID/graphWidth; 
		
		f = h+costToGetHere *costToGetHere;
	}
	
	
}
