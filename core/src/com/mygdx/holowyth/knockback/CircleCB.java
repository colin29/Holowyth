package com.mygdx.holowyth.knockback;

import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * Circle Collision body <br>
 *
 * Simple data class
 *
 * @author Colin Ta
 *
 */
public class CircleCB {
	public float radius;
	public final Point pos;
	public float vx, vy;

	public CircleCB(float x, float y, float radius) {
		this.pos = new Point(x, y);
		this.radius = radius;
		if (radius <= 0) {
			throw new HoloIllegalArgumentsException("Radius must be positive, given: " + radius);
		}
	}

	void setVelocity(float vx, float vy) {
		this.vx = vx;
		this.vy = vy;
	}
}
