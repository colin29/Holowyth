package com.mygdx.holowyth.pathfinding;

import java.util.ArrayList;

import com.mygdx.holowyth.util.data.Point;

/**
 * typedef for ArrayList<Pair<Integer, Integer>>
 *
 */
public class Path extends ArrayList<Point> {

	
	public Path deepCopy(){
		Path newPath = new Path();
		for (Point p : this) {
			newPath.add(new Point(p));
		}
		return newPath;
	}
	
	private static final long serialVersionUID = 1L;

}
