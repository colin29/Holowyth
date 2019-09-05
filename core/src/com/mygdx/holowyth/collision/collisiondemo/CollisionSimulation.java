package com.mygdx.holowyth.collision.collisiondemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.collision.CircleCBInfo;
import com.mygdx.holowyth.collision.CollisionDetection;
import com.mygdx.holowyth.collision.CollisionDetection.IntersectDebugInfo;
import com.mygdx.holowyth.collision.CollisionInfo;
import com.mygdx.holowyth.util.DataUtil;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;
import com.mygdx.holowyth.util.exceptions.HoloOperationException;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;
import com.mygdx.holowyth.util.tools.debugstore.DebugValues;

public class CollisionSimulation {

	static Logger logger = LoggerFactory.getLogger(CollisionSimulation.class);

	List<CircleObject> circleObjects = new ArrayList<CircleObject>();

	// public final float COLLISION_BODY_RADIUS = 30;

	public final float USUAL_RADIUS = 25;

	private IntersectDebugInfo intersectDebugInfo = new IntersectDebugInfo();

	public CollisionSimulation(DebugStore debugStore) {
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

			List<CircleCBInfo> allOtherBodies = new ArrayList<CircleCBInfo>();
			for (CircleObject o : circleObjects) {
				if (o != thisObject)
					allOtherBodies.add(o.getColBody());
			}

			Segment motion = new Segment(x, y, x + vx, y + vy);

			List<CircleCBInfo> collisions = CollisionDetection.getObjectCollisionsAlongLineSegment(motion.x1, motion.y1, motion.x2,
					motion.y2,
					thisObject.getColBody().getRadius(), allOtherBodies);

			for (CircleCBInfo colidee : collisions) {
				logger.debug("Collision between units id [{} {}]", thisObject.id, bodyToObject.get(colidee).id);
			}

			if (collisions.isEmpty()) {
				// Move object normally
				thisObject.setPosition(x + vx, y + vy);
			} else {

				try {
					CollisionInfo collision = CollisionDetection.getFirstCollisionInfo(thisObject.getColBody(), collisions, null,
							this.intersectDebugInfo);
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

		Vector2 normalNorm = new Vector2((float) Math.cos(collision.collisionSurfaceNormalAngle),
				(float) Math.sin(collision.collisionSurfaceNormalAngle));
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
