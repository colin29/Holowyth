package com.mygdx.holowyth.collision.linecircledemo;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;

class Circle {
	private float radius;
	public float x, y;

	public Circle(float x, float y, float radius) {
		this.x = x;
		this.y = y;
		setRadius(radius);
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		if (radius < 0) {
			LoggerFactory.getLogger(this.getClass()).warn("Circle radius cannot be negative, set ignored");
		} else {
			this.radius = radius;
		}
	}

	/**
	 * Creates a vector with the center coordinates
	 */
	public Vector2 getCenter() {
		return new Vector2(x, y);
	}
}
