package com.mygdx.holowyth.knockback;

import com.mygdx.holowyth.util.dataobjects.Point;

public class CircleObject {
	private CircleCB colBody;

	public CircleObject(float x, float y, float radius) {
		colBody = new CircleCB(x, y, radius);
	}

	public CircleCB getColBody() {
		return colBody;
	}

	public float getX() {
		return colBody.getPos().x;
	}

	public float getY() {
		return colBody.getPos().y;
	}

	public float getVx() {
		return colBody.getVx();
	}

	public float getVy() {
		return colBody.getVy();
	}

	public void setVx(float vx) {
		colBody.setVx(vx);
	}

	public void setVy(float vy) {
		colBody.setVx(vy);
	}

	public Point getPos() {
		return colBody.getPos();
	}

	public void setPosition(float x, float y) {
		colBody.getPos().x = x;
		colBody.getPos().y = y;
	}

	public void setVelocity(float vx, float vy) {
		colBody.setVelocity(vx, vy);
	}

}
