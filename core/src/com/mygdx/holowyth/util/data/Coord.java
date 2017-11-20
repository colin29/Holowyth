package com.mygdx.holowyth.util.data;

/**
 * Data class holding two ints
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
