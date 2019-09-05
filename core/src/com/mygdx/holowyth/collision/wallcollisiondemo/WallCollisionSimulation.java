package com.mygdx.holowyth.collision.wallcollisiondemo;

import java.util.ArrayList;
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
	private final List<OrientedPoly> orientedPolys = new ArrayList<OrientedPoly>();

	private final Segment objectMotion = new Segment(200, 200, 300, 200);
	private float bodyRadius = 20;

	/**
	 * @param src
	 *            This class does not modify the given polygons
	 */
	public void setObstaclePolygons(List<Polygon> src) {
		polygons.clear();
		polygons.addAll(src);
		orientedPolys.clear();
		orientedPolys.addAll(calculateOrientedPolygons(polygons));
	}

	/**
	 * Calculates oriented polygons based on the current polygons
	 * 
	 * @param polygons
	 */
	private List<OrientedPoly> calculateOrientedPolygons(Polygons polys) {
		var orientedPolys = new ArrayList<OrientedPoly>();
		for (Polygon poly : polys) {
			orientedPolys.add(new OrientedPoly(poly));
		}
		return orientedPolys;
	}

	public Polygons getObstaclePolygons() {
		return polygons;
	}

	public List<OrientedPoly> getObstaclePolygonsOriented() {
		return orientedPolys;
	}

	public Segment getMotionSegment() {
		return new Segment(objectMotion);
	}

	public void setMotionStart(float x, float y) {
		objectMotion.x1 = x;
		objectMotion.y1 = y;
	}

	public void setMotionEnd(float x, float y) {
		objectMotion.x2 = x;
		objectMotion.y2 = y;
	}

	public float getBodyRadius() {
		return bodyRadius;
	}

}
