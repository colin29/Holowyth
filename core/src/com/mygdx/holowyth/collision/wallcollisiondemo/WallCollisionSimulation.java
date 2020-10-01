package com.mygdx.holowyth.collision.wallcollisiondemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.collision.CircleCBInfo;
import com.mygdx.holowyth.collision.Collidable;
import com.mygdx.holowyth.collision.CollisionDetection;
import com.mygdx.holowyth.collision.CollisionInfo;
import com.mygdx.holowyth.collision.ObstaclePoint;
import com.mygdx.holowyth.collision.ObstacleSeg;
import com.mygdx.holowyth.collision.collisiondemo.CircleCB;
import com.mygdx.holowyth.collision.collisiondemo.CircleCBImpl;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;
import com.mygdx.holowyth.world.map.obstacle.OrientedPoly;
import com.mygdx.holowyth.world.map.obstacle.OrientedSeg;
import com.mygdx.holowyth.world.map.obstacle.Polygon;
import com.mygdx.holowyth.world.map.simplemap.Polygons;

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
	private final List<CircleCBInfo> obstaclePoints = new ArrayList<CircleCBInfo>();

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
		orientedPolys.addAll(OrientedPoly.calculateOrientedPolygons(polygons));
		updateObstaclePoints();
		recalculateObjectCollision();
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

	private Segment postCollisionMotion = null;

	void recalculateObjectCollision() {

		CircleCB objectBody = new CircleCBImpl(objectMotion.x1, objectMotion.y1, bodyRadius);
		objectBody.setVelocity(objectMotion.dx(), objectMotion.dy());

		var infos = new ArrayList<CollisionInfo>();

		infos.addAll(getLineSegCollisionInfos(objectBody));
		infos.addAll(getLinePointCollisionInfos(objectBody));

		CollisionInfo info;
		if (infos.isEmpty()) {
			postCollisionMotion = null;
		} else {
			info = getFirstCollision(infos);
			Vector2 vFinal = calculateVelocityAfterCollision(info);
			postCollisionMotion = new Segment(objectMotion.startPoint(), objectMotion.startPoint());
			postCollisionMotion.x2 += vFinal.x;
			postCollisionMotion.y2 += vFinal.y;

			postCollisionMotion.displace(objectMotion.toVector().scl(info.pOfCollisionPoint));
		}

	}

	private List<CollisionInfo> getLinePointCollisionInfos(CircleCBInfo objectBody) {

		var collidingPoints = CollisionDetection.getCircleBodyCollisionsAlongLineSegment(objectMotion, bodyRadius, obstaclePoints);

		var infos = new ArrayList<CollisionInfo>();

		for (var point : collidingPoints) {
			var info = CollisionDetection.getCircleCircleCollisionInfo(objectMotion, objectBody, point, null);
			if (info != null) {
				infos.add(info);
			}
		}
		return infos;
	}

	/**
	 * Updates obstacle points based on oriented polygons
	 */
	private void updateObstaclePoints() {
		obstaclePoints.clear();
		for (var poly : orientedPolys) {
			for (var seg : poly.segments) {
				obstaclePoints.add(new ObstaclePoint(seg.x1, seg.y1));
			}
		}
	}

	public List<CircleCBInfo> getObstaclePoints() {
		return Collections.unmodifiableList(obstaclePoints);
	}

	private List<CollisionInfo> getLineSegCollisionInfos(CircleCBInfo objectBody) {
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
				surfaceNormalAngle = segVec.angleRad() < 0 ? (float) (segVec.angleRad() + 2 * Math.PI) : segVec.angleRad();

				final float RADS_TO_DEGREES = (float) (180 / Math.PI);
				logger.debug("surface normal angle in degrees: {}", surfaceNormalAngle * RADS_TO_DEGREES);

				Collidable obstacleSeg = new ObstacleSeg(seg);

				var info = new CollisionInfo(objectBody, obstacleSeg, pValue, surfaceNormalAngle);
				infos.add(info);
			}
		}

		return infos;
	}

	private CollisionInfo getFirstCollision(List<CollisionInfo> infos) {
		// Compare the p value of all collisions, return the one with smallest p
		Comparator<CollisionInfo> ascendingPOrder = (CollisionInfo c1, CollisionInfo c2) -> {
			return (c1.pOfCollisionPoint <= c2.pOfCollisionPoint) ? -1 : 1;
		};
		PriorityQueue<CollisionInfo> q = new PriorityQueue<CollisionInfo>(ascendingPOrder);

		q.addAll(infos);

		CollisionInfo firstCollision = q.peek();
		return firstCollision;
	}

	/**
	 * Note: body 2 is always a wall, which has infinite mass
	 */
	private Vector2 calculateVelocityAfterCollision(CollisionInfo collision) {
		// The collision info takes
		// the opposite's vx/vy is either their normal movement speed, or their knockback
		// at the end, if the target wasn't in knockback state, automatically turn it into knockback state (stub
		// functionality)

		// 1. Determine unit's velocities in terms of the normalized collision normal vector. Set aside the
		// perpendicular components. We now have 2 1-d velocity vectors

		final CircleCBInfo thisBody = collision.cur;

		if (!(collision.other instanceof ObstacleSeg || collision.other instanceof ObstaclePoint)) {
			throw new RuntimeException("Unsupported Collidable type: " + collision.other.getClass().getSimpleName());
		}

		Vector2 normalNorm = new Vector2((float) Math.cos(collision.collisionSurfaceNormalAngle),
				(float) Math.sin(collision.collisionSurfaceNormalAngle));
		Vector2 v1 = new Vector2(thisBody.getVx(), thisBody.getVy());
		Vector2 v1Norm = new Vector2(v1).nor();

		Vector2 v2 = new Vector2(0, 0);
		Vector2 v2Norm = new Vector2(v2).nor();

		float v1ColAxis = v1.len() * v1Norm.dot(normalNorm);
		float v2ColAxis = v2.len() * v2Norm.dot(normalNorm);

		float elasticity = 0.25f;

		// 2. Transform the problem into the zero momentum frame

		final float MASS_BODY = 3; // system supports mass but game doesn't use it yet
		final float VERY_HIGH_MASS = 9999;

		final float m1 = MASS_BODY;
		final float m2 = VERY_HIGH_MASS;

		float M1ColAxis = v1ColAxis * m1;
		float M2ColAxis = v2ColAxis * m2;

		float MSystemColAxis = M1ColAxis + M2ColAxis;
		float VSystemColAxis = MSystemColAxis / (m1 + m2);

		float v1ZeroFrame = v1ColAxis - VSystemColAxis;
		float v2ZeroFrame = v2ColAxis - VSystemColAxis;

		// 3. Solve the problem with the zero momentum frame
		// 1. Use derived formula to compute v1'

		float v1FinalZeroFrame = (float) Math
				.sqrt(elasticity
						* (m1 * (v1ZeroFrame * v1ZeroFrame) + m2 * (v2ZeroFrame * v2ZeroFrame))
						/ (m1 * (1 + m1 / m2)));

		// 2. Plugin to momentum equation to get v2' (was unneeded)

		// 3. Subtract from initial velocities to get the change in velocity along the collision normal vector

		float dv1ColAxis = v1FinalZeroFrame - v1ZeroFrame;

		// 4. Convert change in velocity into standard coordinates and modify both body's velocities by that much,
		// respectively.

		Vector2 dv1 = new Vector2(normalNorm).scl(dv1ColAxis);

		return dv1.add(v1); // return the final velocity of body 1

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

	public Segment getPostCollisionMotion() {
		if (postCollisionMotion == null)
			return null;
		return new Segment(postCollisionMotion);
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
