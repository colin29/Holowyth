package com.mygdx.holowyth.collision.wallcollisiondemo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.collision.Collidable;
import com.mygdx.holowyth.collision.CollisionInfo;
import com.mygdx.holowyth.collision.ObstacleSeg;
import com.mygdx.holowyth.collision.collisiondemo.CircleCB;
import com.mygdx.holowyth.collision.collisiondemo.CircleCBImpl;
import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.polygon.Polygons;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;

/**
 * Detects and calculates a collision between a moving object and a set of polygon obstacles
 * 
 * @author Colin Ta
 *
 */
public class WallCollisionSimulation {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Polygons polygons = new Polygons();
	private final List<OrientedPoly> orientedPolys = new ArrayList<OrientedPoly>();

	private final Segment objectMotion = new Segment(200, 200, 300, 200);
	private float bodyRadius = 20;

	public WallCollisionSimulation() {
	}

	/**
	 * @param src
	 *            This class does not modify the given polygons
	 */
	public void setObstaclePolygons(List<Polygon> src) {
		polygons.clear();
		polygons.addAll(src);
		orientedPolys.clear();
		orientedPolys.addAll(calculateOrientedPolygons(polygons));
		recalculateObjectCollision();
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

	public List<OrientedSeg> getObstacleSegs() {
		var segs = new ArrayList<OrientedSeg>();
		for (var poly : orientedPolys) {
			segs.addAll(poly.segments);
		}
		return segs;
	}

	public List<OrientedSeg> getObstacleExpandedSegs() {
		var segs = new ArrayList<OrientedSeg>();
		for (var poly : orientedPolys) {
			for (var seg : poly.segments) {
				segs.add(seg.getOutwardlyDisplacedSegment(bodyRadius));
			}
		}
		return segs;
	}

	void recalculateObjectCollision() {
		getLineSegCollisionInfos();
	}

	private List<CollisionInfo> getLineSegCollisionInfos() {
		var infos = new ArrayList<CollisionInfo>();

		var segs = getObstacleExpandedSegs();

		for (var seg : segs) {
			Point intersection = lineSegsIntersect(objectMotion, seg);
			if (intersection != null) {

				// Calculate additional details and record collision info
				float pValue, surfaceNormalAngle;
				Segment startToIntersect = new Segment(objectMotion.startPoint(), intersection);
				pValue = startToIntersect.getLength() / objectMotion.getLength();

				Vector2 segVec = seg.toVector();
				segVec.rotate(seg.isClockwise ? 90 : -90);
				surfaceNormalAngle = segVec.angle();

				CircleCB objectBody = new CircleCBImpl(objectMotion.x1, objectMotion.y2, bodyRadius);
				objectBody.setVelocity(objectMotion.dx(), objectMotion.dy());

				Collidable obstacleSeg = new ObstacleSeg(seg);

				var info = new CollisionInfo(objectBody, obstacleSeg, pValue, surfaceNormalAngle);
				infos.add(info);
			}
		}

		logger.debug("Line segs found: {} collisions", infos.size());
		return infos;
	}

	private static Point lineSegsIntersect(Segment s1, Segment s2) {

		float a1, a2, b1, b2, c1, c2;

		a1 = s1.y2 - s1.y1;
		b1 = s1.x1 - s1.x2;
		c1 = a1 * s1.x1 + b1 * s1.y1;

		a2 = s2.y2 - s2.y1;
		b2 = s2.x1 - s2.x2;
		c2 = a2 * s2.x1 + b2 * s2.y1;

		float det = a1 * b2 - a2 * b1;
		float x, y;
		if (det == 0) {
		} else {
			x = (b2 * c1 - b1 * c2) / det;
			y = (a1 * c2 - a2 * c1) / det;

			// Check if the point is on both line segments
			float EPS = 0.001f; // tolerance (From brief testing I saw roundings errors of 0.000031 for x = 400, 30
								// times less than this. However rounding errors are proportional to size of x, thus
								// the large safety factor)

			// System.out.printf("%f, %f, %f %n", Math.min(s2.y1, s2.y2) - EPS, y, Math.max(s2.y1, s2.y2) + EPS);

			if (Math.min(s1.x1, s1.x2) - EPS <= x && x <= Math.max(s1.x1, s1.x2) + EPS
					&& Math.min(s1.y1, s1.y2) - EPS <= y && y <= Math.max(s1.y1, s1.y2 + EPS)
					&& Math.min(s2.x1, s2.x2) - EPS <= x && x <= Math.max(s2.x1, s2.x2) + EPS
					&& Math.min(s2.y1, s2.y2) - EPS <= y && y <= Math.max(s2.y1, s2.y2) + EPS) {
				return new Point(x, y);
			}
			return null;
		}
		return null;
	}

	public Segment getMotionSegment() {
		return new Segment(objectMotion);
	}

	public void setMotionStart(float x, float y) {
		objectMotion.x1 = x;
		objectMotion.y1 = y;
		recalculateObjectCollision();
	}

	public void setMotionEnd(float x, float y) {
		objectMotion.x2 = x;
		objectMotion.y2 = y;
		recalculateObjectCollision();
	}

	public float getBodyRadius() {
		return bodyRadius;
	}

}
