package com.mygdx.holowyth.knockback.collision;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.knockback.CircleCB;
import com.mygdx.holowyth.util.DataUtil;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;
import com.mygdx.holowyth.util.tools.debugstore.DebugValues;

public class World {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private List<Circle> circles = new ArrayList<Circle>();
	private final Segment segment = new Segment(0, 0, 0, 0);
	private Circle keyCircle;

	private Vector2 initial = new Vector2();
	private Vector2 delta = new Vector2();
	private Vector2 deltaNormalized = new Vector2();

	private Vector2 initialToCircleCenter = new Vector2();
	private Vector2 initialToClosestPoint = new Vector2();

	private Vector2 closestPoint = new Vector2();
	private float closestDistToCenter;

	private Vector2 intersectPoint = new Vector2();

	final float RADS_TO_DEGREES = (float) (180 / Math.PI);

	/**
	 * Describes how far intersectPoint is along the motion line segment. Within [0,1] means it lies on the motion
	 * segment
	 */
	private float pOfIntersectPoint;

	/**
	 * Is the angle in rads, from circle center to intersect point, 0 degrees is at (0,radius), spinning CCW
	 */
	private float angleOfCircleAtIntersect; //

	public World(DebugStore debugStore) {
		DebugValues debugValues = debugStore.registerComponent("World");
		debugValues.add("Intersect point", () -> getRoundedString(intersectPoint));
		debugValues.add("Initial", () -> getRoundedString(new Vector2(segment.x1, segment.y1)));
		debugValues.add("Final", () -> getRoundedString(new Vector2(segment.x2, segment.y2)));
		debugValues.add("Delta", () -> getRoundedString(delta));
		debugValues.add("P of Intersect point", () -> DataUtil.getRoundedString(pOfIntersectPoint));
		debugValues.add("Angle at intersect (degrees)",
				() -> DataUtil.getRoundedString(angleOfCircleAtIntersect * RADS_TO_DEGREES));

	}

	List<Circle> getCircles() {
		return circles;
	}

	public String getRoundedString(Vector2 point) {
		return String.format("%s %s", DataUtil.getRoundedString(point.x), DataUtil.getRoundedString(point.y));
	}

	public void setKeyCircle(Circle circle) {
		if (circles.contains(circle)) {
			keyCircle = circle;
		} else {
			LoggerFactory.getLogger(this.getClass()).warn("KeyCircle must be a circle contained in World, set ignored");
		}
	}

	public Circle getKeyCircle() {
		return keyCircle;
	}

	/**
	 * Returns a fresh copy
	 */
	public Segment getSegment() {
		return new Segment(segment);
	}

	/**
	 * May also update other dependent World state
	 */
	public void setSegment(float x1, float y1, float x2, float y2) {
		segment.set(x1, y1, x2, y2);

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

		Vector2 circleCenterToIntersect = new Vector2(intersectPoint.x - keyCircle.x, intersectPoint.y - keyCircle.y);

		if (circleCenterToIntersect.y > 0) {
			angleOfCircleAtIntersect = (float) (Math.acos(circleCenterToIntersect.x / keyCircle.getRadius()));
		} else {
			angleOfCircleAtIntersect = (float) (2 * Math.PI
					- Math.acos(circleCenterToIntersect.x / keyCircle.getRadius()));
		}

	}

	/**
	 * Takes in a motion segment, the current collisionBody, and all the other collisionBodies
	 * 
	 * @return
	 */
	private CollisionInfo getFirstCollision(Segment segment, CircleCB curBody, List<CircleCB> others) {
		return null;
	}

	/**
	 * Takes in the current collisionBody
	 * 
	 * @return
	 */
	private CollisionInfo getCollision() {
		return null;
	}

	public Segment getInitialToCircleCenter() {
		return new Segment(initial.x, initial.y, keyCircle.x, keyCircle.y);
	}

	public Segment getInitialToClosestPoint() {
		return new Segment(initial.x, initial.y, closestPoint.x, closestPoint.y);
	}

	public Point getIntersectPoint() {
		return new Point(intersectPoint.x, intersectPoint.y);
	}

	public float getPofIntersectPoint() {
		return pOfIntersectPoint;
	}

}
