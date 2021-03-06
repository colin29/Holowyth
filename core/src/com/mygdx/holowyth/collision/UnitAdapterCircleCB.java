package com.mygdx.holowyth.collision;

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
		return unit.getX();
	}

	@Override
	public float getY() {
		return unit.getY();
	}

	// If a unit isn't being knocked back, it still is treating as a colliding body, with its normal velocity.
	@Override
	public float getVx() {
		return unit.getMotion().isBeingKnockedBack() ? unit.getMotion().getKnockbackVx() : unit.getMotion().getVx();

	}

	@Override
	public float getVy() {
		return unit.getMotion().isBeingKnockedBack() ? unit.getMotion().getKnockbackVy() : unit.getMotion().getVy();
	}

	@Override
	public float getRadius() {
		return unit.getRadius();
	}

}
