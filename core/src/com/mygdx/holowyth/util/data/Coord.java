package com.mygdx.holowyth.util.data;

/**
 * Data class holding two ints. Different from {@link Point} in that it's for int coordinates instead of an arbitrary point 
 */
public class Coord {
		public int x, y;
		public Coord(int x, int y){
			this.x = x;
			this.y = y;
		}

		public Coord (Coord p) {
			this.x = p.x;
			this.y = p.y;
		}
}
