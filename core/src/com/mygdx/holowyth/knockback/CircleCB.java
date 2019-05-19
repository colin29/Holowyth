package com.mygdx.holowyth.knockback;

import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * Circle collision body
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
	}

	void setVelocity(float vx, float vy) {
		this.vx = vx;
		this.vy = vy;
	}
}
