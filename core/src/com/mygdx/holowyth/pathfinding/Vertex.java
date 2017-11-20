package com.mygdx.holowyth.pathfinding;

public class Vertex {
	public boolean N, S, W, E; //marks whether edge to the to the adjacent vertex is path-able or not.
	public boolean NW, NE, SW, SE;
	public boolean reachable; // according to the latest floodfill, whether this vertex is reachable or not
}
