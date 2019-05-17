package com.mygdx.holowyth.knockback;

import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * Circle collision body
 * 
 * @author Colin Ta
 *
 */
public class CircleCB {
	private float radius;
	private final Point position;

	public CircleCB(float x, float y, float radius) {
		this.position = new Point(x, y);
		this.radius = radius;
	}

	public float getRadius() {
		return radius;
	}

	public Point getPos() {
		return position;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

}
