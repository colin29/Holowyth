package com.mygdx.holowyth.knockback.collision;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
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

	public World(DebugStore debugStore) {
		DebugValues debugValues = debugStore.registerComponent("World");
		debugValues.add("Intersect point", () -> getRoundedString(intersectPoint));
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
		deltaNormalized = delta.nor();

		initialToCircleCenter.setZero().add(keyCircle.getCenter()).sub(initial);

		float length = delta.dot(initialToCircleCenter) / delta.len();
		initialToClosestPoint.set(deltaNormalized).scl(length);

		closestPoint.set(initial).add(initialToClosestPoint);

		closestDistToCenter = (keyCircle.getCenter().sub(closestPoint)).len();

		float radius = keyCircle.getRadius();
		float lengthOfHalfChord = (float) Math.sqrt((radius * radius - closestDistToCenter * closestDistToCenter));

		// There are two intersect points, but we only get the first one, by backtracking along the segment line
		intersectPoint.set(closestPoint).add(deltaNormalized.scl(-1 * lengthOfHalfChord));

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

}
