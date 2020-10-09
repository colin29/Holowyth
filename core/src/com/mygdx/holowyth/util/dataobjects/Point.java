package com.mygdx.holowyth.util.dataobjects;

import com.badlogic.gdx.math.Vector2;

/**
 * Data struct holding x and y.
 * 
 * @author Colin Ta
 */
public class Point {
	public float x, y;

	public Point() {
	}

	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Point(Point p) {
		this.x = p.x;
		this.y = p.y;
	}

	public Point(Vector2 vec) {
		this.x = vec.x;
		this.y = vec.y;
	}

	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public static float dist(Point p1, Point p2) {
		float dx = p2.x - p1.x;
		float dy = p2.y - p1.y;
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	public static float distSqr(Point p1, Point p2) {
		float dx = p2.x - p1.x;
		float dy = p2.y - p1.y;
		return dx * dx + dy * dy;
	}

	/**
	 * Returns the angle from point 1 to point 2. If points are identical in value, should return 0.
	 * 
	 * @return a value in the range [0, 360).
	 */
	public static float getAngleInDegrees(Point p1, Point p2) {
		float angle = (float) Math.toDegrees(Math.atan2(p2.y - p1.y, p2.x - p1.x));

		if (angle < 0) {
			angle += 360;
		}

		return angle;
	}

	/**
	 */
	public Point add(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Vector2 toVector() {
		return new Vector2(x, y);
	}
	public String toString() {
		return String.format("(%f,%f)", x, y);
	}
}
