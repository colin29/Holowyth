package com.mygdx.holowyth.collision.wallcollisiondemo;

import java.util.List;

import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.polygon.Polygons;
import com.mygdx.holowyth.util.dataobjects.Segment;

/**
 * Detects and calculates a collision between a moving object and a set of polygon obstacles
 * 
 * @author Colin Ta
 *
 */
public class WallCollisionSimulation {

	private final Polygons polygons = new Polygons();

	private final Segment objectMotion = new Segment(200, 200, 300, 200);
	private float objectRadius = 20;

	/**
	 * @param src
	 *            WallCollisionSimulation does not modify the given polygons
	 * 
	 */
	public void setObstaclePolygons(List<Polygon> src) {
		polygons.clear();
		polygons.addAll(src);
	}

	public Polygons getObstaclePolygons() {
		return polygons;
	}

}
