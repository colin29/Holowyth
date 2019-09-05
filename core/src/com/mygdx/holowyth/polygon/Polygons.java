package com.mygdx.holowyth.polygon;

import java.util.ArrayList;
import java.util.List;

import com.mygdx.holowyth.collision.wallcollisiondemo.OrientedPoly;

/**
 * Convenience class to use instead of typing ArrayList<Polygon>
 *
 */
public class Polygons extends ArrayList<Polygon> {

	private static final long serialVersionUID = 1L;

	/**
	 * Calculates oriented polygons based given polygons
	 * 
	 * @param polygons
	 */
	public static List<OrientedPoly> calculateOrientedPolygons(ArrayList<Polygon> polys) {
		var orientedPolys = new ArrayList<OrientedPoly>();
		for (Polygon poly : polys) {
			orientedPolys.add(new OrientedPoly(poly));
		}
		return orientedPolys;
	}

}
