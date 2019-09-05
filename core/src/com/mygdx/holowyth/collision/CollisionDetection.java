package com.mygdx.holowyth.collision;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.util.dataobjects.Segment;
import com.mygdx.holowyth.util.exceptions.HoloOperationException;

/**
 * Can find the first collision of a circle, given its motion segment and a list of other circle bodies.
 * 
 * A class with static methods in order to simulate circle vs circle collisions
 * 
 * 
 * @author Colin Ta
 *
 */
public class CollisionDetection {

	static Logger logger = LoggerFactory.getLogger(CollisionDetection.class);

	/**
	 * Finds the circle bodies which this circle body collides with while moving along a given line segment. Note: For collision detection purposes,
	 * all other objects are considered stationary. The physics collisions however, considers the speed of both objects.
	 */
	public static ArrayList<CircleCBInfo> getObjectCollisionsAlongLineSegment(float ix, float iy, float dx, float dy,
			float thisRadius,
			List<CircleCBInfo> cbs) {
		ArrayList<CircleCBInfo> collisions = new ArrayList<CircleCBInfo>();
		for (CircleCBInfo cb : cbs) {
			if (Line2D.ptSegDistSq(ix, iy, dx, dy, cb.getX(), cb.getY()) < (cb.getRadius() + thisRadius)
					* (cb.getRadius() + thisRadius)) {
				collisions.add(cb);
			}
		}
		return collisions;
	}

	/**
	 * Given a list of colliding bodies, narrows down and calculates the result of the first collision along the line segment motion.
	 * 
	 * @param segment
	 *            The motion for curBody this tick
	 * @param curBody
	 * @param collisions
	 *            The colBodies being collided into, each of these must actually be intersecting with curBody. Should contain at least one colliding
	 *            body
	 * @param intersectDebugInfo
	 *            optional, pass in an object to receive debug values
	 * 
	 * @return Information about the first collision along curBody's motion
	 */
	public static CollisionInfo getFirstCollisionInfo(CircleCBInfo curBody, List<CircleCBInfo> collisions,
			IntersectDebugInfo intersectDebugInfo) {

		Segment segment = new Segment(curBody.getX(), curBody.getY(),
				curBody.getX() + curBody.getVx(),
				curBody.getY() + curBody.getVy());

		List<CollisionInfo> colInfos = new ArrayList<CollisionInfo>();
		for (CircleCBInfo other : collisions) {
			try {
				CollisionInfo info = getCollisionInfo(segment, curBody, other, intersectDebugInfo);
				if (info != null) {
					colInfos.add(info);
				}
			} catch (HoloOperationException e) {
				logger.warn(e.getMessage());
				logger.trace(e.getFromMessage());
				logger.warn("Skipping adding this collision's info");
			}

		}

		if (colInfos.isEmpty()) {
			throw (new HoloOperationException("All collisions provided were invalid, so no info could be returned"));
		}

		// Compare the p value of all collisions, return the one with smallest p
		Comparator<CollisionInfo> ascendingPOrder = (CollisionInfo c1, CollisionInfo c2) -> {
			if (c1.pOfCollisionPoint <= c2.pOfCollisionPoint) {
				return -1;
			} else {
				return 1;
			}
		};
		PriorityQueue<CollisionInfo> q = new PriorityQueue<CollisionInfo>(ascendingPOrder);

		q.addAll(colInfos);

		// TODO: Here we want to detect and add collisions with an obstacle line-segment

		CollisionInfo firstCollision = q.peek();
		return firstCollision;
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
	private static CollisionInfo getCollisionInfo(Segment segment, CircleCBInfo curBody, CircleCBInfo other,
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
