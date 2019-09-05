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

	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public static float calcDistance(Point p1, Point p2) {
		float dx = p2.x - p1.x;
		float dy = p2.y - p1.y;
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	public static float calcDistanceSqr(Point p1, Point p2) {
		float dx = p2.x - p1.x;
		float dy = p2.y - p1.y;
		return dx * dx + dy * dy;
	}

	public static float getAngleInDegrees(Point p1, Point p2) {
		float angle = (float) Math.toDegrees(Math.atan2(p2.y - p1.y, p2.x - p1.x));

		if (angle < 0) {
			angle += 360;
		}

		return angle;
	}

	/**
	 * @return a new point with the added values
	 */
	public Point add(float x, float y) {
		return new Point(this.x + x, this.y + y);
	}

	public Vector2 toVector() {
		return new Vector2(x, y);
	}
}
