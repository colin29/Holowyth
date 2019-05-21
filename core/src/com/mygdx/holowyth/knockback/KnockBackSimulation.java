package com.mygdx.holowyth.knockback;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.knockback.collision.Circle;
import com.mygdx.holowyth.knockback.collision.CollisionInfo;
import com.mygdx.holowyth.util.DataUtil;
import com.mygdx.holowyth.util.dataobjects.Segment;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;
import com.mygdx.holowyth.util.tools.debugstore.DebugValues;

public class KnockBackSimulation {

	Logger logger = LoggerFactory.getLogger(KnockBackSimulation.class);

	List<CircleObject> circleObjects = new ArrayList<CircleObject>();

	public final float COLLISION_BODY_RADIUS = 40;

	private IntersectDebugInfo intersectDebugInfo = new IntersectDebugInfo();

	public KnockBackSimulation(DebugStore debugStore) {
		DebugValues debugValues = debugStore.registerComponent("Knockback Sim");
		addDebugEntries(debugValues);
	}

	private void addDebugEntries(DebugValues debugValues) {
		debugValues.add("initial", () -> DataUtil.getRoundedString(intersectDebugInfo.initial));
		debugValues.add("delta", () -> DataUtil.getRoundedString(intersectDebugInfo.delta));
		debugValues.add("deltaNormalized", () -> DataUtil.getRoundedString(intersectDebugInfo.deltaNormalized));
		debugValues.space();

		debugValues.add("initialToCircleCenter",
				() -> DataUtil.getRoundedString(intersectDebugInfo.initialToCircleCenter));
		debugValues.add("initialToClosestPoint",
				() -> DataUtil.getRoundedString(intersectDebugInfo.initialToClosestPoint));
		debugValues.space();

		debugValues.add("closestPoint", () -> DataUtil.getRoundedString(intersectDebugInfo.closestPoint));
		debugValues.add("closestDistToCenter", () -> intersectDebugInfo.closestDistToCenter);
		debugValues.space();

		debugValues.add("intersectPoint", () -> DataUtil.getRoundedString(intersectDebugInfo.intersectPoint));
		debugValues.add("pOfIntersectPoint", () -> DataUtil.getRoundedString(intersectDebugInfo.pOfIntersectPoint));
		debugValues.space();

		debugValues.add("angleOfCircleAtIntersect", () -> intersectDebugInfo.angleOfCircleAtIntersectDegrees);
	}

	public void tick() {
		logger.debug("ticked");

		for (CircleObject o : circleObjects) {
			if (o.getColBody().radius != COLLISION_BODY_RADIUS) {
				logger.warn("Multiple unit radiuses not supported [id={}]", o.id);
			}
		}

		Map<CircleCB, CircleObject> bodyToObject = new HashMap<CircleCB, CircleObject>();
		for (CircleObject o : circleObjects) {
			bodyToObject.put(o.getColBody(), o);
		}

		// Collision resolution is run once for every unit. It is possible that a later unit's velocity was modified
		// earlier this tick.
		for (CircleObject thisObject : circleObjects) {

			final float x, y, vx, vy;

			x = thisObject.getX();
			y = thisObject.getY();
			vx = thisObject.getVx();
			vy = thisObject.getVy();

			List<CircleCB> allOtherBodies = new ArrayList<CircleCB>();
			for (CircleObject o : circleObjects) {
				if (o != thisObject)
					allOtherBodies.add(o.getColBody());
			}

			Segment motion = new Segment(x, y, x + vx, y + vy);

			List<CircleCB> collisions = getCollisionsAlongLineSegment(motion.x1, motion.y1, motion.x2, motion.y2,
					thisObject.getColBody().radius, allOtherBodies);

			for (CircleCB colidee : collisions) {
				logger.debug("Collision between units id [{} {}]", thisObject.id, bodyToObject.get(colidee).id);
			}

			if (collisions.isEmpty()) {
				// Move object normally
				thisObject.setPosition(x + vx, y + vy);
				continue;
			} else {

				// Determine the first collision (we only deal with the first collision)
				CollisionInfo collision = getFirstCollision(motion, thisObject.getColBody(), collisions);

				// 2. Resolve that collision, adjusting velocity.

			}

		}
	}

	public static ArrayList<CircleCB> getCollisionsAlongLineSegment(float ix, float iy, float dx, float dy,
			float unitRadius,
			List<CircleCB> cbs) {
		ArrayList<CircleCB> collisions = new ArrayList<CircleCB>();
		for (CircleCB cb : cbs) {
			if (Line2D.ptSegDistSq(ix, iy, dx, dy, cb.pos.x, cb.pos.y) < (cb.radius + unitRadius)
					* (cb.radius + unitRadius)) {
				collisions.add(cb);
			}
		}
		return collisions;
	}

	/**
	 * 
	 * @param segment
	 *            The motion for curBody this tick
	 * @param curBody
	 * @param collisions
	 *            The colBodies being collided into, each of these must actually be intersecting with curBody. Should
	 *            contain at least one colliding body
	 * 
	 * @return Information about the collision and the colBody in question.
	 */
	private CollisionInfo getFirstCollision(Segment segment, CircleCB curBody, List<CircleCB> collisions) {

		List<CollisionInfo> colInfos = new ArrayList<CollisionInfo>();
		for (CircleCB other : collisions) {
			CollisionInfo info = getCollisionInfo(segment, curBody, other);
			if (info != null) {
				colInfos.add(info);
			}
		}

		if (colInfos.isEmpty()) {
			logger.warn("All collisions provided were invalid, so no info could be returned");
			return null;
		}

		// Compare the p value of all collisions, return the one with the least
		Comparator<CollisionInfo> ascendingPOrder = (CollisionInfo c1, CollisionInfo c2) -> {
			if (c1.pOfCollisionPoint <= c2.pOfCollisionPoint) {
				return -1;
			} else {
				return 1;
			}
		};
		PriorityQueue<CollisionInfo> q = new PriorityQueue<CollisionInfo>(ascendingPOrder);

		for (CollisionInfo info : colInfos) {
			q.add(info);
		}

		CollisionInfo firstCollision = q.peek();
		return firstCollision;
	}

	/**
	 * @param segment
	 *            The motion for curBody this tick
	 * 
	 * @param other
	 *            A colBody which is colliding with curBody. other must actually be intersecting with curBody
	 * 
	 * @return Information about the collision. Can return null, but only due to improper input
	 */
	private CollisionInfo getCollisionInfo(Segment segment, CircleCB curBody, CircleCB other) {

		final float x1, y1, x2, y2;

		// We can represent two circles colliding with that of a line segment intersecting with an expanded circle.
		// The intersectPoint will be different of course, but the angle of collision will be the same
		Circle keyCircle = new Circle(other.pos.x, other.pos.y, curBody.radius + other.radius);

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
		 * Describes how far intersectPoint is along the motion line segment. Within [0,1] means it lies on the motion
		 * segment
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

		// set debugInformation
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

		return new CollisionInfo(other, pOfIntersectPoint, angleOfCircleAtIntersect);
	}

	public CircleObject addCircleObject(float x, float y) {
		CircleObject o = new CircleObject(x, y, COLLISION_BODY_RADIUS);
		circleObjects.add(o);
		return o;
	}

	public void addCircleObject(float x, float y, float vx, float vy) {
		circleObjects.add(new CircleObject(x, y, COLLISION_BODY_RADIUS));
	}

	public List<CircleObject> getCircleObjects() {
		return circleObjects;
	}

}
