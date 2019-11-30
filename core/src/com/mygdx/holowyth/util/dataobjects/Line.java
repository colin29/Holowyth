package com.mygdx.holowyth.util.dataobjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * A point + (non-zero) vector representation of a line
 *
 */
public class Line {

	private static Logger logger = LoggerFactory.getLogger(Line.class);

	public final Point p;
	private final Vector2 v;

	public Line(float x, float y, float dx, float dy) {
		p = new Point(x, y);
		v = new Vector2();
		setV(dx, dy);
	}

	public void setV(float vx, float vy) {
		v.x = vx;
		v.y = vy;
		if (v.isZero()) {
			throw new HoloIllegalArgumentsException("Line direction vector must be non-zero");
		}
	}

	public float getVx() {
		return v.x;
	}

	public float getVy() {
		return v.y;
	}

	/**
	 * Returns new vector with the vector component of the line
	 */
	public Vector2 getV() {
		return new Vector2(v);
	}

	public float getAngle() {
		return v.angle();
	}

	/**
	 * Shifts p by the given vector
	 */
	public void displace(Vector2 vec) {
		p.x += vec.x;
		p.y += vec.y;
	}

	public boolean doesPointLieOnTheLeft(Point point) {
		float angleToPoint = Point.getAngleInDegrees(p, point);
		float diff = normalizeAngle(v.angle() - angleToPoint);
		return diff >= 180;
	}

	private static float normalizeAngle(float angle) {
		return (angle %= 360) > 0 ? angle : (angle + 360);
	}
}
