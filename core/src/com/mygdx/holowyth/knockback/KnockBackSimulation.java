package com.mygdx.holowyth.knockback;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.util.DataUtil;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;
import com.mygdx.holowyth.util.exceptions.HoloOperationException;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;
import com.mygdx.holowyth.util.tools.debugstore.DebugValues;

public class KnockBackSimulation {

	Logger logger = LoggerFactory.getLogger(KnockBackSimulation.class);

	List<CircleObject> circleObjects = new ArrayList<CircleObject>();

	// public final float COLLISION_BODY_RADIUS = 30;

	public final float USUAL_RADIUS = 25;

	private IntersectDebugInfo intersectDebugInfo = new IntersectDebugInfo();

	public KnockBackSimulation(DebugStore debugStore) {
		@SuppressWarnings("unused")
		DebugValues debugValues = debugStore.registerComponent("Knockback Sim");
		// addDebugEntries(debugValues);
	}

	@SuppressWarnings("unused")
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

		Map<CircleCB, CircleObject> bodyToObject = new HashMap<CircleCB, CircleObject>();
		for (CircleObject o : circleObjects) {
			bodyToObject.put(o.getColBody(), o);
		}

		detectAndResolveObjectObjectCollisions(Collections.unmodifiableMap(bodyToObject));
		detectAndResolveObjectMapBoundaryCollisions();
	}

	private void detectAndResolveObjectObjectCollisions(Map<CircleCB, CircleObject> bodyToObject) {

		// Collision resolution is run once for every unit. It is possible that a later unit's velocity (or possibly
		// position) was modified earlier this tick.
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

			List<CircleCB> collisions = getObjectCollisionsAlongLineSegment(motion.x1, motion.y1, motion.x2, motion.y2,
					thisObject.getColBody().getRadius(), allOtherBodies);

			for (CircleCBInfo colidee : collisions) {
				logger.debug("Collision between units id [{} {}]", thisObject.id, bodyToObject.get(colidee).id);
			}

			if (collisions.isEmpty()) {
				// Move object normally
				thisObject.setPosition(x + vx, y + vy);
				continue;
			} else {

				try {
					CollisionInfo collision = getFirstCollisionInfo(thisObject.getColBody(), collisions);
					resolveCollision(collision);
				} catch (HoloOperationException e) {
					logger.warn(e.getMessage());
					logger.trace(e.getFromMessage());
					logger.warn("Skipping resolving this object's collision");
					// Skip resolving this collision
				}
			}
		}
	}

	private static ArrayList<CircleCB> getObjectCollisionsAlongLineSegment(float ix, float iy, float dx, float dy,
			float thisRadius,
			List<CircleCB> cbs) {
		ArrayList<CircleCB> collisions = new ArrayList<CircleCB>();
		for (CircleCB cb : cbs) {
			if (Line2D.ptSegDistSq(ix, iy, dx, dy, cb.getX(), cb.getY()) < (cb.getRadius() + thisRadius)
					* (cb.getRadius() + thisRadius)) {
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
	 * @return Information about the first collision along curBody's motion
	 */
	private CollisionInfo getFirstCollisionInfo(CircleCB curBody, List<CircleCB> collisions) {

		Segment segment = new Segment(curBody.getX(), curBody.getY(),
				curBody.getX() + curBody.getVx(),
				curBody.getY() + curBody.getVy());

		List<CollisionInfo> colInfos = new ArrayList<CollisionInfo>();
		for (CircleCB other : collisions) {
			try {
				CollisionInfo info = getCollisionInfo(segment, curBody, other);
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
	 * @return Information about the collision. Can return null, but only due to improper input.
	 */
	private CollisionInfo getCollisionInfo(Segment segment, CircleCBInfo curBody, CircleCBInfo other) {

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

		// Set debug information
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

		return new CollisionInfo(curBody, other, pOfIntersectPoint, angleOfCircleAtIntersect);
	}

	/**
	 * Resolves collision, updating velocities (and possibly location).
	 * 
	 * @param collision
	 */
	private void resolveCollision(CollisionInfo collision) {

		// 1. Determine unit's velocities in terms of the normalized collision normal vector. Set aside the
		// perpendicular components. We now have 2 1-d velocity vectors

		CircleCB thisBody = (CircleCB) collision.cur;
		CircleCB other = (CircleCB) collision.other;

		Vector2 normalNorm = new Vector2((float) Math.cos(collision.collisionAngle),
				(float) Math.sin(collision.collisionAngle));
		Vector2 v1 = new Vector2(thisBody.getVx(), thisBody.getVy());
		Vector2 v1Norm = new Vector2(v1).nor();

		Vector2 v2 = new Vector2(other.getVx(), other.getVy());
		Vector2 v2Norm = new Vector2(v2).nor();

		float v1ColAxis = v1.len() * v1Norm.dot(normalNorm);
		float v2ColAxis = v2.len() * v2Norm.dot(normalNorm);

		// 2. Transform the problem into the zero momentum frame

		final float MASS_BODY = 3; // system supports mass but game doesn't use it yet

		final float m1 = MASS_BODY;
		final float m2 = MASS_BODY;

		float M1ColAxis = v1ColAxis * m1;
		float M2ColAxis = v2ColAxis * m2;

		float MSystemColAxis = M1ColAxis + M2ColAxis;
		float VSystemColAxis = MSystemColAxis / (m1 + m2);

		float v1ZeroFrame = v1ColAxis - VSystemColAxis;
		float v2ZeroFrame = v2ColAxis - VSystemColAxis;

		// 3. Solve the problem with the zero momentum frame
		// 1. Use derived formula to compute v1'

		float v1FinalZeroFrame = (float) Math
				.sqrt(elasticity * (m1 * (v1ZeroFrame * v1ZeroFrame) + m2 * (v2ZeroFrame * v2ZeroFrame))
						/ (m1 * (1 + m1 / m2)));

		// 2. Plugin to momentum equation to get v2'

		float v2FinalZeroFrame = -1 * v1FinalZeroFrame * m1 / m2;

		// 3. Subtract from initial velocities to get the change in velocity along the collision normal vector

		float dv1ColAxis = v1FinalZeroFrame - v1ZeroFrame;
		float dv2ColAxis = v2FinalZeroFrame - v2ZeroFrame;

		// 4. Convert change in velocity into standard coordinates and modify both body's velocities by that much,
		// respectively.

		Vector2 dv1 = new Vector2(normalNorm).scl(dv1ColAxis);
		Vector2 dv2 = new Vector2(normalNorm).scl(dv2ColAxis);

		thisBody.setVelocity(
				thisBody.getVx() + dv1.x,
				thisBody.getVy() + dv1.y);

		other.setVelocity(
				other.getVx() + dv2.x,
				other.getVy() + dv2.y);

		// thisBody.vx += dv1.x;
		// thisBody.vy += dv1.y;
		//
		// other.vx += dv2.x;
		// other.vy += dv2.y;

	}

	private void detectAndResolveObjectMapBoundaryCollisions() {
		float[] horizontalWalls = new float[2];
		float[] verticalWalls = new float[2];

		horizontalWalls[0] = 0;
		horizontalWalls[1] = getMapHeight(); // horizontal walls restrict y position
		verticalWalls[0] = 0;
		verticalWalls[1] = getMapWidth();

		for (CircleObject thisObject : circleObjects) {

			final float x, y, vx, vy;

			x = thisObject.getX();
			y = thisObject.getY();
			vx = thisObject.getVx();
			vy = thisObject.getVy();

			Segment motion = new Segment(x, y, x + vx, y + vy);

			boolean collidesWithHorizontalWall = false;
			boolean collidesWithVerticalWall = false;

			for (float horizontalWall : horizontalWalls) {
				if (isNumberInBounds(horizontalWall, motion.y1, motion.y2, thisObject.getRadius())) {
					collidesWithHorizontalWall = true;
				}
			}
			for (float verticalWall : verticalWalls) {
				if (isNumberInBounds(verticalWall, motion.x1, motion.x2, thisObject.getRadius())) {
					collidesWithVerticalWall = true;
				}
			}

			if (collidesWithHorizontalWall) {
				thisObject.setVelocity(vx, -1 * vy);
			}
			if (collidesWithVerticalWall) {
				thisObject.setVelocity(-1 * vx, vy);
			}

		}

	}

	private boolean isNumberInBounds(float value, float bound1, float bound2, float addedPadding) {
		float lowerBound = Math.min(bound1, bound2) - addedPadding;
		float upperBound = Math.max(bound1, bound2) + addedPadding;
		return (lowerBound <= value && value <= upperBound);
	}

	private float getMapHeight() {
		return Gdx.graphics.getHeight();
	}

	private float getMapWidth() {
		return Gdx.graphics.getWidth();
	}

	public CircleObject addCircleObject(float x, float y) {
		CircleObject o = new CircleObject(x, y, USUAL_RADIUS);
		circleObjects.add(o);
		return o;
	}

	public void addCircleObject(float x, float y, float vx, float vy) {
		CircleObject o = new CircleObject(x, y, USUAL_RADIUS);
		o.setVelocity(vx, vy);
		circleObjects.add(o);
	}

	public void clearAllCircles() {
		circleObjects.clear();
		logger.info("Simulation cleared");
	}

	private final float screenCenterX = Gdx.graphics.getWidth() / 2;
	private final float screenCenterY = Gdx.graphics.getHeight() / 2;

	private final Point screenCenter = new Point(screenCenterX, screenCenterY);

	private float elasticity = 1;

	void restartWithSingleCollision() {
		clearAllCircles();
		this.addCircleObject(screenCenterX, screenCenterY, 5, 0);
		this.addCircleObject(screenCenterX + 200, screenCenterY, 0, 0);
	}

	void restartWith3WayCollision() {

		float offset = 200;
		float speed = 3;

		clearAllCircles();

		for (int i = 0; i < 3; i++) {
			float angle = 360f * i / 3;
			circleObjects.add((makeCircleObjectWithOffset(screenCenter, offset, angle, speed, angle + 180)));
		}
	}

	void restartWith8WayCollision() {

		float offset = 200;
		float speed = 3;

		clearAllCircles();

		for (int i = 0; i < 8; i++) {
			float angle = 360f * i / 8;
			circleObjects.add((makeCircleObjectWithOffset(screenCenter, offset, angle, speed, angle + 180 + 10)));
		}
	}

	void restartWithManyObjects() {

		float speed = 3;

		float NUM_OBJECTS_TO_CREATE = 30;
		final int MAX_TRIES = 100;

		clearAllCircles();
		Point newPos = new Point();
		int objectsCreated = 0;

		for (int i = 0; i < MAX_TRIES && objectsCreated < NUM_OBJECTS_TO_CREATE; i++) {
			float variation = 5;
			float radius = (float) (USUAL_RADIUS + Math.random() * variation * 2 - variation);

			newPos.x = (float) Math.random() * (getMapWidth() - 2 * radius) + radius;
			newPos.y = (float) Math.random() * (getMapHeight() - 2 * radius) + radius;

			if (isAreaClear(newPos, radius)) {
				CircleObject o = new CircleObject(newPos.x, newPos.y, radius);
				float angle = (float) Math.random() * 360;
				setVelocity(o, speed, angle);
				circleObjects.add(o);
			}
		}
	}

	void restartWithIllegalUnitPlacement() {
		clearAllCircles();

		CircleObject o1 = makeCircleObjectWithOffset(screenCenter, 0, 0);
		CircleObject o2 = makeCircleObjectWithOffset(screenCenter, USUAL_RADIUS / 2, 0);
		circleObjects.add(o1);
		circleObjects.add(o2);

	}

	private boolean isAreaClear(Point point, float radius) {

		for (CircleObject o : circleObjects) {
			if (Point.calcDistance(point, o.getPos()) <= radius + o.getRadius()) {
				return false;
			}
		}
		return true;
	}

	private CircleObject makeCircleObjectWithOffset(Point point, float offset, float angleDegrees, float speed,
			float velAngleDegrees) {
		CircleObject c = makeCircleObjectWithOffset(point, offset, angleDegrees);
		setVelocity(c, speed, velAngleDegrees);
		return c;
	}

	private CircleObject makeCircleObjectWithOffset(Point point, float offset, float angleDegrees) {
		float angle = (float) (angleDegrees / 180 * Math.PI);
		CircleObject c = new CircleObject((float) (point.x + offset * Math.cos(angle)),
				(float) (point.y + offset * Math.sin(angle)), USUAL_RADIUS);
		return c;
	}

	private void setVelocity(CircleObject c, float speed, float angleDegrees) {
		float angle = (float) (angleDegrees / 180 * Math.PI);
		c.setVelocity((float) (speed * Math.cos(angle)), (float) (speed * Math.sin(angle)));
	}

	public List<CircleObject> getCircleObjects() {
		return circleObjects;
	}

	void setElasticity(float elasticity) {
		if (elasticity >= 0 && elasticity <= 1) {
			this.elasticity = elasticity;
		} else {
			logger.warn("Invalid value {} given. Elasticity must be between 0 and 1", elasticity);
		}

	}

}
