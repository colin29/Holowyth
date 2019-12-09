package com.mygdx.holowyth.util.dataobjects;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.polygon.Polygon;

public class OrientedPoly {

	public final List<OrientedSeg> segments = new ArrayList<OrientedSeg>();

	Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Creates an oriented polygon from a simple points-defined polygon
	 * 
	 * @param poly
	 */
	public OrientedPoly(Polygon polygon) {
		// firstly, define the polygon edges in the new format

		float x1, y1, x2, y2;
		for (int i = 0; i <= polygon.count - 2; i += 2) { // for each polygon edge
			x1 = polygon.floats[i];
			y1 = polygon.floats[i + 1];
			x2 = polygon.floats[(i + 2) % polygon.count];
			y2 = polygon.floats[(i + 3) % polygon.count];

			OrientedSeg seg = new OrientedSeg(x1, y1, x2, y2);
			segments.add(seg);
		}
		if (polygon.vertexCount() < 3) {
			throw new InvalidParameterException("Invalid polygon given: it has less than three vertexes");
		}
		if (segments.size() < 3) {
			throw new RuntimeException("Something went wrong, the polygon constructed has less than 3 edges");
		}

		// Now, figure out the orientation of the polygon using the shoe lace formula
		if (isClockwise()) {
			for (OrientedSeg seg : segments) {
				seg.isClockwise = true;
			}
		}

	}

	public boolean isClockwise() {
		float total = 0;
		for (OrientedSeg seg : segments) {
			total += (seg.x2 - seg.x1) * (seg.y1 + seg.y2);
		}
		// logger.debug("caclulated shoelace total is {}", total);
		return total > 0;
	}

	public Polygon toRawPolygon() {
		float[] points = new float[segments.size() * 2];

		for (int i = 0; i < segments.size(); i++) {
			points[2 * i] = segments.get(i).x1;
			points[2 * i + 1] = segments.get(i).y1;
		}
		return new Polygon(points);
	}

	// We know the points are defined in order
}
