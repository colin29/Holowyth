package com.mygdx.holowyth.collision;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.collision.wallcollisiondemo.OrientedPoly;
import com.mygdx.holowyth.collision.wallcollisiondemo.OrientedSeg;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;
import com.mygdx.holowyth.util.exceptions.HoloOperationException;

/**
 * Can find the first collision of a circle, given its motion segment and a list of other circle bodies.
 * 
 * A class with static methods in order to simulate circle vs circle collisions
 * 
 * 
 * 
 * @author Colin Ta
 *
 */
public class CollisionDetection {

	static Logger logger = LoggerFactory.getLogger(CollisionDetection.class);

	public static ArrayList<CircleCBInfo> getCircleBodyCollisionsAlongLineSegment(CircleCBInfo body,
			List<CircleCBInfo> cbs) {
		Segment segment = new Segment(body.getX(), body.getY(), body.getX() + body.getVx(), body.getY() + body.getVy());
		return getCircleBodyCollisionsAlongLineSegment(segment, body.getRadius(), cbs);
	}

	/**
	 * Finds the circle bodies which this circle body collides with while moving along a given line segment. Note: For collision detection purposes,
	 * all other objects are considered stationary. The physics collisions however, considers the speed of both objects.
	 */
	public static ArrayList<CircleCBInfo> getCircleBodyCollisionsAlongLineSegment(Segment motion,
			float thisRadius,
			List<CircleCBInfo> cbs) {
		ArrayList<CircleCBInfo> collisions = new ArrayList<CircleCBInfo>();
		for (CircleCBInfo cb : cbs) {
			if (Line2D.ptSegDistSq(motion.x1, motion.y1, motion.x2, motion.y2, cb.getX(), cb.getY()) < (cb.getRadius() + thisRadius)
					* (cb.getRadius() + thisRadius)) {
				collisions.add(cb);
			}
		}
		return collisions;
	}

	/**
	 * Finds and calculates collision infos of a circle body vs wall point, while moving along the body's motion
	 */
	public static List<CollisionInfo> getCirclePointCollisionInfos(CircleCBInfo cb, List<CircleCBInfo> obstaclePoints) {

		ArrayList<CircleCBInfo> collidingPoints = getCircleBodyCollisionsAlongLineSegment(cb, obstaclePoints);

		var infos = new ArrayList<CollisionInfo>();

		Segment motion = new Segment(cb.getX(), cb.getY(), cb.getX() + cb.getVx(), cb.getY() + cb.getVy());
		for (var point : collidingPoints) {
			var info = getCircleCircleCollisionInfo(motion, cb, point, null);
			if (info != null) {
				infos.add(info);
			}
		}
		return infos;
	}

	/**
	 * Finds and calculates collision infos of circle body vs wall segments, while moving along the body's motion
	 */
	public static List<CollisionInfo> getCircleSegCollisionInfos(CircleCBInfo objectBody,
			float thisRadius,
			List<OrientedPoly> polys) {

		Segment objectMotion = new Segment(objectBody.getX(), objectBody.getY(), objectBody.getX() + objectBody.getVx(),
				objectBody.getY() + objectBody.getVy());

		var infos = new ArrayList<CollisionInfo>();

		var segs = getObstacleExpandedSegs(polys, thisRadius);

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

	/**
	 * Given multiple types of detected collisions, narrows down and calculates the result of the first collision along the line segment motion. There
	 * should be at least one collision of some type.
	 * 
	 * @param segment
	 *            The motion for curBody this tick
	 * @param curBody
	 * @param bodyCollisions
	 *            optional, The colBodies being collided into, each of these must actually be intersecting with curBody.
	 * @param obstacleCollisions
	 *            optional, pass null if you don't need to consider obstacle collisions
	 * @param intersectDebugInfo
	 *            optional, pass in to receive debug values
	 * 
	 * @return Information about the first collision along curBody's motion
	 */
	public static CollisionInfo getFirstCollisionInfo(CircleCBInfo curBody, List<CircleCBInfo> bodyCollisions, List<CollisionInfo> obstacleCollisions,
			IntersectDebugInfo intersectDebugInfo) {

		List<CollisionInfo> colInfos = new ArrayList<CollisionInfo>();

		if (bodyCollisions != null) {
			Segment segment = new Segment(curBody.getX(), curBody.getY(),
					curBody.getX() + curBody.getVx(),
					curBody.getY() + curBody.getVy());
			for (CircleCBInfo other : bodyCollisions) {
				try {
					CollisionInfo info = getCircleCircleCollisionInfo(segment, curBody, other, intersectDebugInfo);
					if (info != null) {
						colInfos.add(info);
					}
				} catch (HoloOperationException e) {
					logger.warn(e.getMessage());
					logger.warn(e.getFromMessage());
					logger.warn("Skipping adding this collision's info");
				}

			}
		}

		// Compare the p value of all collisions, return the one with smallest p
		Comparator<CollisionInfo> ascendingPOrder = (CollisionInfo c1, CollisionInfo c2) -> {
			return (c1.pOfCollisionPoint <= c2.pOfCollisionPoint) ? -1 : 1;
		};
		PriorityQueue<CollisionInfo> q = new PriorityQueue<CollisionInfo>(ascendingPOrder);

		q.addAll(colInfos);
		if (obstacleCollisions != null) {
			q.addAll(obstacleCollisions);
		}

		if (q.isEmpty()) {
			throw (new HoloOperationException("Error, no collisions actually provided(?)."));
		}

		CollisionInfo firstCollision = q.peek();
		return firstCollision;
	}

	private static List<OrientedSeg> getObstacleExpandedSegs(List<OrientedPoly> polys, float bodyRadius) {
		var segs = new ArrayList<OrientedSeg>();
		for (var poly : polys) {
			for (var seg : poly.segments) {
				segs.add(seg.getOutwardlyDisplacedSegment(bodyRadius));
			}
		}
		return segs;
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

	/**
	 * @param segment
	 *            The motion for curBody this tick
	 * 
	 * @param other
	 *            A colBody which is colliding with curBody. other must actually be intersecting with curBody
	 * @param intersectDebugInfo
	 *            optional
	 * 
	 * @return Information about the collision. Can return null, but only due to improper input.
	 */
	public static CollisionInfo getCircleCircleCollisionInfo(Segment segment, CircleCBInfo curBody, CircleCBInfo other,
			IntersectDebugInfo intersectDebugInfo) {

		if (curBody.getVx() == 0 && curBody.getVy() == 0) {
			throw new HoloOperationException(
					"Given curBody has velocity 0, cannot compute collision info. Based on arguments, it appears that curBody is already colliding with something, which is invalid state.");
		}

		final float x1, y1, x2, y2;

		// We can represent two circles colliding with that of a line segment intersecting with an expanded circle.
		// The intersectPoint will be different of course, but the angle of collision will be the same
		Circle keyCircle = new Circle(other.getX(), other.getY(), curBody.getRadius() + other.getRadius());

		Vector2 initial = new Vector2();
		Vector2 delta = new Vector2();
		Vector2 deltaNormalized = new Vector2();
		Vector2 initialToCircleCenter = new Vector2();
		Vector2 initialToClosestPoint = new Vector2();
		Vector2 closestPoint = new Vector2();
		float closestDistToCenter;
		Vector2 intersectPoint = new Vector2();

		final float RADS_TO_DEGREES = (float) (180 / Math.PI);

		/**
		 * Describes how far intersectPoint is along the motion line segment. Within [0,1] means it lies on the motion segment
		 */
		float pOfIntersectPoint;

		/**
		 * Is the angle in rads, from circle center to intersect point, 0 degrees is at (0,radius), spinning CCW
		 */
		float angleOfCircleAtIntersect; //

		x1 = segment.x1;
		y1 = segment.y1;
		x2 = segment.x2;
		y2 = segment.y2;

		initial.set(x1, y1);
		delta.set(x2 - x1, y2 - y1);
		deltaNormalized.set(delta).nor();

		initialToCircleCenter.setZero().add(keyCircle.getCenter()).sub(initial);

		float length = delta.dot(initialToCircleCenter) / delta.len();
		initialToClosestPoint.set(deltaNormalized).scl(length);

		closestPoint.set(initial).add(initialToClosestPoint);

		closestDistToCenter = (keyCircle.getCenter().sub(closestPoint)).len();

		// if distance is greater than center, there is no collision
		if (closestDistToCenter > keyCircle.getRadius()) {
			logger.warn("No collision -- line does not intersect circle");
			return null;
		}

		float radius = keyCircle.getRadius();
		float lengthOfHalfChord = (float) Math.sqrt((radius * radius - closestDistToCenter * closestDistToCenter));

		// There are two intersect points, but we only get the first one, by backtracking along the segment line
		intersectPoint.set(closestPoint).add(deltaNormalized.scl(-1 * lengthOfHalfChord));

		if (Math.abs(delta.x) > Math.abs(delta.y)) {
			pOfIntersectPoint = (intersectPoint.x - initial.x) / delta.x;
		} else {
			pOfIntersectPoint = (intersectPoint.y - initial.y) / delta.y;
		}

		if (pOfIntersectPoint < 0 || pOfIntersectPoint > 1) {
			logger.warn("No collision -- p is not within [0,1], collision is not within line segment");
			return null;
		}

		Vector2 circleCenterToIntersect = new Vector2(intersectPoint.x - keyCircle.x, intersectPoint.y - keyCircle.y);

		if (circleCenterToIntersect.y > 0) {
			angleOfCircleAtIntersect = (float) (Math.acos(circleCenterToIntersect.x / keyCircle.getRadius()));
		} else {
			angleOfCircleAtIntersect = (float) (2 * Math.PI
					- Math.acos(circleCenterToIntersect.x / keyCircle.getRadius()));
		}

		// Set debug information
		if (intersectDebugInfo != null) {
			intersectDebugInfo.initial.set(initial);
			intersectDebugInfo.delta.set(delta);
			intersectDebugInfo.deltaNormalized.set(deltaNormalized);

			intersectDebugInfo.initialToCircleCenter.set(initialToCircleCenter);
			intersectDebugInfo.initialToClosestPoint.set(initialToClosestPoint);

			intersectDebugInfo.closestPoint.set(closestPoint);
			intersectDebugInfo.closestDistToCenter = closestDistToCenter;

			intersectDebugInfo.intersectPoint.set(intersectPoint);

			intersectDebugInfo.pOfIntersectPoint = pOfIntersectPoint;

			intersectDebugInfo.angleOfCircleAtIntersectDegrees = angleOfCircleAtIntersect * RADS_TO_DEGREES;
		}

		return new CollisionInfo(curBody, other, pOfIntersectPoint, angleOfCircleAtIntersect);
	}

	public static class IntersectDebugInfo {
		public final Vector2 initial = new Vector2();
		public final Vector2 delta = new Vector2();
		public final Vector2 deltaNormalized = new Vector2();

		public final Vector2 initialToCircleCenter = new Vector2();
		public Vector2 initialToClosestPoint = new Vector2();

		public final Vector2 closestPoint = new Vector2();
		public float closestDistToCenter;

		public final Vector2 intersectPoint = new Vector2();

		/**
		 * Describes how far intersectPoint is along the motion line segment. Within [0,1] means it lies on the motion segment
		 */
		public float pOfIntersectPoint;

		/**
		 * Is the angle in rads, from circle center to intersect point, 0 degrees is at (0,radius), spinning CCW
		 */
		public float angleOfCircleAtIntersectDegrees;
	}

}
