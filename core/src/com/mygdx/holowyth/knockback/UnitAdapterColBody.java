package com.mygdx.holowyth.knockback;

import com.mygdx.holowyth.unit.Unit;

/**
 * Serves as a read only adapter for Collision classes that use CircleCB
 * 
 * Mass is actually not required, because a ColBody is only for colliding and finding the angle of collision
 * 
 * It isn't used for resolving the collision itself (mass).
 */
public class UnitAdapterColBody implements CircleCBInfo {

	Unit unit;

	public UnitAdapterColBody(Unit unit) {
		this.unit = unit;
	}

	@Override
	public float getX() {
		return unit.getY();
	}

	@Override
	public float getY() {
		return unit.getY();
	}

	@Override
	public float getVx() {
		return 0; // unit.motion.getVx();
					// TODO
	}

	@Override
	public float getVy() {
		return 0; // unit.motion.getVy();
					// TODO
	}

	@Override
	public float getRadius() {
		// TODO Auto-generated method stub
		return 0;
	}

}
