package com.mygdx.holowyth.pathfinding;

import java.awt.geom.Line2D;
import java.util.ArrayList;

import com.mygdx.holowyth.polygon.Polygon;

/**
 * Contains helper functions related to pathfinding and geometry
 *
 */
public class HoloPF {

	public static boolean isEdgePathable(float x, float y, float x2, float y2, ArrayList<Polygon> polys) {
		boolean intersects = false;
		for (Polygon polygon : polys) {
			for (int i = 0; i <= polygon.count - 2; i += 2) { // for each polygon edge
				if (Line2D.linesIntersect(x, y, x2, y2, polygon.vertexes[i], polygon.vertexes[i + 1],
						polygon.vertexes[(i + 2) % polygon.count], polygon.vertexes[(i + 3) % polygon.count])) {
					intersects = true;
				}
			}
		}
		return !intersects;
	}

}
