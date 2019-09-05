package com.mygdx.holowyth.collision.collisiondemo;

import com.mygdx.holowyth.collision.CircleCBInfo;

/**
 * Circle Collision body <br>
 *
 * Simple data class
 *
 * @author Colin Ta
 *
 */
public interface CircleCB extends CircleCBInfo {

	void setPosition(float x, float y);

	void setVelocity(float vx, float vy);

}
