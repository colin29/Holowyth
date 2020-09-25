package com.mygdx.holowyth.pathfinding;

import com.mygdx.holowyth.util.dataobjects.Point;

public class Vertex {
	public boolean N, S, W, E; // marks whether edge to the to the adjacent vertex is path-able or not.
	public boolean NW, NE, SW, SE;
	public boolean reachable; // according to the latest floodfill, whether this vertex is reachable or not

	public int ix, iy;
	
	public Vertex(int ix, int iy){
		this.ix = ix;
		this.iy = iy;
	}
	
	public void set(Vertex v) {
		this.N = v.N;
		this.S = v.S;
		this.W = v.W;
		this.E = v.E;

		this.NW = v.NW;
		this.NE = v.NE;
		this.SW = v.SW;
		this.SE = v.SE;
		
		this.reachable = v.reachable;
	}
	/**
	 * Mark this as unpathable
	 */
	public void block(){
		this.N = false;
		this.S = false;
		this.W = false;
		this.E = false;

		this.NW = false;
		this.NE = false;
		this.SW = false;
		this.SE = false;
		this.reachable = false;
	}
	public Point getAsPoint(int CELL_SIZE) {
		return new Point(ix * CELL_SIZE, iy * CELL_SIZE);
	}
}
