package com.mygdx.holowyth.knockback;

import com.mygdx.holowyth.util.dataobjects.Point;

public class CircleObject implements ObjectInfo {
	public final int id;
	private CircleCB colBody;

	public CircleObject(float x, float y, float radius) {
		colBody = new CircleCB(x, y, radius);
		this.id = getNextId();
	}

	public CircleCB getColBody() {
		return colBody;
	}

	public float getX() {
		return colBody.pos.x;
	}

	public float getY() {
		return colBody.pos.y;
	}

	public float getVx() {
		return colBody.vx;
	}

	public float getVy() {
		return colBody.vy;
	}

	public Point getPos() {
		return colBody.pos;
	}

	public void setPosition(float x, float y) {
		colBody.pos.x = x;
		colBody.pos.y = y;
	}

	public void setVelocity(float vx, float vy) {
		colBody.setVelocity(vx, vy);
	}

	private static int nextId = 1;

	private static int getNextId() {
		return nextId++;
	}

}
