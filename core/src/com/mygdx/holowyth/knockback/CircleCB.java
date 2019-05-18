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

	private float vx, vy;

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

	public float getVx() {
		return vx;
	}

	public float getVy() {
		return vy;
	}

	public void setVx(float vx) {
		this.vx = vx;
	}

	public void setVy(float vy) {
		this.vy = vy;
	}

	public void setVelocity(float vx, float vy) {
		this.vx = vx;
		this.vy = vy;
	}
}
