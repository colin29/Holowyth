package com.mygdx.holowyth.unit;

/**
 * Handles logic for whether a unit is stunned or not
 *
 */
public class UnitStun {

	private Unit self;

	private boolean isStunned;
	private float stunDurationRemaining; // in frames;

	public UnitStun(Unit self) {
		this.self = self;
	}

	/**
	 * Advance by one game frame
	 */
	void tick() {
		stunDurationRemaining -= 1;
		if (stunDurationRemaining <= 0) {
			isStunned = false;
			stunDurationRemaining = 0;
		}
	}

	boolean isStunned() {
		return isStunned;
	}

	void applyStun(float duration) {
		if (!isStunned) {
			beginStun(duration);
			return;
		} else {
			stunDurationRemaining += duration;
		}
	}

	private void beginStun(float duration) {
		stunDurationRemaining = 0;

		self.motion.stopCurrentMovement();
		self.clearOrder();
		self.stopAttacking();
		self.interruptCastingAndChannelling();

		isStunned = true;
		stunDurationRemaining += duration;
	}

	private void endStun() {
		// TODO: stub
	}

	float getStunDurationRemaining() {
		return isStunned ? stunDurationRemaining : 0;
	}
}
