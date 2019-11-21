package com.mygdx.holowyth.unit;

/**
 * Handles logic for whether a unit is stunned or not
 *
 */
public class UnitStun {

	private Unit self;

	private boolean isStunned;
	private float stunDurationRemaining; // in frames;

	private boolean isReeled;
	private float reelDurationRemaining;

	public UnitStun(Unit self) {
		this.self = self;
	}

	/**
	 * Advance by one game frame
	 */
	void tick() {
		if (isStunned()) {
			tickStun();
		} else if (isReeled()) {
			tickReeled();
		}

	}

	private void tickStun() {
		stunDurationRemaining -= 1;
		if (stunDurationRemaining <= 0) {
			endStun();
		}
	}

	private void tickReeled() {
		reelDurationRemaining -= 1;
		if (reelDurationRemaining <= 0) {
			endReeled();
		}
	}

	void applyStun(float duration) {
		if (!isStunned) {
			beginStun(duration);
		} else {
			stunDurationRemaining += duration;
		}
	}

	void applyReel(float duration) {
		if (!isReeled) {
			beginReel(duration);
		} else {
			reelDurationRemaining += duration;
		}
	}

	boolean isStunned() {
		return isStunned;
	}

	/**
	 * Note: a unit that is stun will return false, if you want either use isReelel() || isStunned()
	 */
	boolean isReeled() {
		return isReeled;
	}

	float getStunDurationRemaining() {
		return isStunned() ? stunDurationRemaining : 0;
	}

	float getReelDurationRemaining() {
		return isReeled() ? reelDurationRemaining : 0;
	}

	private void beginStun(float duration) {
		self.motion.stopCurrentMovement();
		self.clearOrder();
		self.stopAttacking();
		self.interruptCastingAndChannelling();

		isStunned = true;
		stunDurationRemaining = duration;
	}

	private void endStun() {
		isStunned = false;
		stunDurationRemaining = 0;
		beginReel(120);
	}

	private void beginReel(float duration) {
		self.motion.stopCurrentMovement();
		self.clearOrder();

		self.interruptCastingAndChannelling();

		isReeled = true;
		reelDurationRemaining = duration;

		self.addAttackCooldownRemaining(self.getAttackCooldown());
	}

	private void endReeled() {
		isReeled = false;
		reelDurationRemaining = 0;
	}

}
