package com.mygdx.holowyth.knockback;

import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

public class CircleCBImpl implements CircleCB {
	private float radius;
	private float x, y;
	private float vx, vy;

	/**
	 * Usual concrete implementation of CircleCB as a POJO. The reason why CircleCB was turned into an interface was to
	 * allow implementations where the data (state) was stored elsewhere
	 */
	public CircleCBImpl(float x, float y, float radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		if (radius <= 0) {
			throw new HoloIllegalArgumentsException("Radius must be positive, given: " + radius);
		}
	}

	@Override
	public float getX() {
		return this.x;
	}

	@Override
	public float getY() {
		return this.y;
	}

	@Override
	public float getVx() {
		return vx;
	}

	@Override
	public float getVy() {
		return vy;
	}

	@Override
	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void setVelocity(float vx, float vy) {
		this.vx = vx;
		this.vy = vy;
	}

	@Override
	public float getRadius() {
		return this.radius;
	}

}
