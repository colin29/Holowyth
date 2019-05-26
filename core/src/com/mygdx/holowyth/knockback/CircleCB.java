package com.mygdx.holowyth.knockback;

/**
 * Circle Collision body <br>
 *
 * Simple data class
 *
 * @author Colin Ta
 *
 */
public interface CircleCB {

	float getX();

	float getY();

	float getVx();

	float getVy();

	float getRadius();

	void setPosition(float x, float y);

	void setVelocity(float vx, float vy);

}
