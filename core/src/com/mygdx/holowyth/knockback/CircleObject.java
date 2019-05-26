package com.mygdx.holowyth.knockback;

import com.mygdx.holowyth.util.dataobjects.Point;

public class CircleObject implements ObjectInfo {
	public final int id;
	private CircleCB colBody;
	private float radius;

	public CircleObject(float x, float y, float radius) {
		colBody = new CircleCBImpl(x, y, radius);
		this.id = getNextId();
		this.radius = radius;
	}

	public CircleCB getColBody() {
		return colBody;
	}

	public float getX() {
		return colBody.getX();
	}

	public float getY() {
		return colBody.getY();
	}

	public float getVx() {
		return colBody.getVx();
	}

	public float getVy() {
		return colBody.getVy();
	}

	/**
	 * Returns a new copy
	 */
	public Point getPos() {
		return new Point(getX(), getY());
	}

	public void setPosition(float x, float y) {
		colBody.setPosition(x, y);
	}

	public void setVelocity(float vx, float vy) {
		colBody.setVelocity(vx, vy);
	}

	private static int nextId = 1;

	private static int getNextId() {
		return nextId++;
	}

	public float getRadius() {
		return radius;
	}

}
