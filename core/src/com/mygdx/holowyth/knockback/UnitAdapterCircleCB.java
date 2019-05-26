package com.mygdx.holowyth.knockback;

import com.mygdx.holowyth.unit.Unit;

/**
 * Serves as a read only adapter for Collision classes that use CircleCB
 * 
 * Mass is actually not required, because a ColBody is only for colliding and finding the angle of collision
 * 
 * It isn't used for resolving the collision itself (mass).
 */
public class UnitAdapterCircleCB implements CircleCBInfo {

	Unit unit;

	public UnitAdapterCircleCB(Unit unit) {
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
		return unit.motion.getKnockBackVx();
	}

	@Override
	public float getVy() {
		return unit.motion.getKnockBackVy();
	}

	@Override
	public float getRadius() {
		return unit.getRadius();
	}

}
