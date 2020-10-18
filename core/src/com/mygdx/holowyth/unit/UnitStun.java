package com.mygdx.holowyth.unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.util.exceptions.HoloAssertException;

/**
 * Handles logic for whether a unit is stunned or not <br><br>
 * 
 * Has Map life-time.
 *
 */
class UnitStun {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	private Unit self;

	enum State {
		STUNNED, REELED, NORMAL;
	}

	private State state = State.NORMAL;

	private float stunDurationRemaining; // in frames;
	/**
	 * Amount of reel time accumulated, to be executed after stun expires
	 */
	private float deferredReelAmount;

	private float reelDurationRemaining;

	UnitStun(Unit self) {
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
		if (stunDurationRemaining <= 0 &&
				!self.getMotion().isBeingKnockedBack()) { // if unit is still being knockbacked, wait until knockback ends before ending stun
			endStun();
		}
	}

	private void tickReeled() {
		reelDurationRemaining -= 1;
		if (reelDurationRemaining <= 0) {
			endReeled();
		}
	}

	/**
	 * A duration of 0 will create a very brief stun, like an interrupt. <br>
	 * However, knockback is also a stun, so even if duration is 0, the unit will remain stunned until knockback ends
	 * 
	 * @param maxStunDuration
	 *            Cumulative stun contributions are prorated around this. If maxStunDuration < duration, it will be treated as maxStunDuration =
	 *            duration
	 */
	void applyStun(float duration, float maxStunDuration) {
		if (duration < 0) {
			logger.warn("tried to apply stun of negative duration");
			return;
		}
		if (maxStunDuration == 0) {
			return;
		}
		float stunTimeAddedDebug;

		self.combat.setTimeOutOfCombat(0);
		
		switch (state) {
		case NORMAL:
			beginStun(duration);
			stunTimeAddedDebug = duration;
			break;
		case REELED:
			deferredReelAmount = reelDurationRemaining;
			beginStun(duration);
			stunTimeAddedDebug = duration;
			break;
		case STUNNED:
			// in case of consecutive stuns, stun contributions are prorated around a maxDuration
			if (maxStunDuration < duration) {
				maxStunDuration = duration;
			}
			float prorateFactor = Math.max(0, 1 - stunDurationRemaining / maxStunDuration);
			stunDurationRemaining += duration * prorateFactor;
			stunTimeAddedDebug = duration * prorateFactor;
			break;
		default:
			throw new HoloAssertException("Unsupported state");
		}
		logger.debug("{} Stun time: {} (+{}) (max: {})", self.getName(), stunDurationRemaining, stunTimeAddedDebug, maxStunDuration);
	}

	void applyKnockbackStun(float duration, Vector2 dv, float maxKnockBackVel, float maxStunDuration) {
		applyStun(duration, maxStunDuration);

		if (self.getMotion().isBeingKnockedBack()) {
			// Prorate dv based on current velocity: the higher velocity, the smaller dv's effect is
			Vector2 curVel = self.getMotion().getKnockbackVelocity();
			float factor = Math.max(0, 1 - curVel.len() / maxKnockBackVel);
			Vector2 dVProrated = new Vector2(dv).scl(factor);
			self.getMotion().applyKnockBackVelocity(dVProrated.x, dVProrated.y);

			// logger.debug("Knockback info: Orig Vel {} maxKnockBackVel {} dv {} proratedDv {} finalVel {}", curVel.len(), maxKnockBackVel, dv.len(),
			// dVProrated.len(), self.motion.getKnockbackVelocity().len());
		} else {
			self.getMotion().applyKnockBackVelocity(dv.x, dv.y);
		}
		self.combat.setTimeOutOfCombat(0);

	}

	void applyKnockbackStunWithoutVelProrate(float duration, float maxStunDuration, Vector2 dv) {
		applyStun(duration, maxStunDuration);
		self.getMotion().applyKnockBackVelocity(dv.x, dv.y);
		self.combat.setTimeOutOfCombat(0);
	}

	boolean isUnitBeingKnockedBack() {
		return self.getMotion().isBeingKnockedBack();
	}

	/**
	 * 
	 */
	void applyReel(float duration, float maxDuration) {
		if (duration < 0) {
			logger.warn("tried to apply reel of negative duration");
			return;
		}
		
		self.combat.setTimeOutOfCombat(0);
		float reelTimeAdded;

		switch (state) {
		case NORMAL:
			beginReel(duration);
			logger.debug("Reel time: {} (+{})", reelDurationRemaining, reelDurationRemaining);
			break;
		case REELED:
			reelTimeAdded = getReelTime(duration, maxDuration, reelDurationRemaining);
			reelDurationRemaining += reelTimeAdded;
			logger.debug("Reel time: {} (+{}) (max: {})", reelDurationRemaining, reelTimeAdded, maxDuration);
			break;
		case STUNNED:
			reelTimeAdded = getReelTime(duration, maxDuration, deferredReelAmount);
			deferredReelAmount += reelTimeAdded;
			logger.debug("Reel time: {} (+{}) (max: {}) (Deferred)", deferredReelAmount, reelTimeAdded, maxDuration);
			break;
		default:
			throw new HoloAssertException("Unsupported state");
		}
	}

	private float getReelTime(float duration, float maxDuration, float existingReelTime) {
		float prorateFactor = Math.max(0, 1 - existingReelTime / maxDuration);
		return duration * prorateFactor;
	}

	boolean isStunned() {
		return state == State.STUNNED;
	}

	/**
	 * Note: a unit that is stun will return false, if you want either use isReelel() || isStunned()
	 */
	boolean isReeled() {
		return state == State.REELED;
	}

	float getStunDurationRemaining() {
		return isStunned() ? stunDurationRemaining : 0;
	}

	float getReelDurationRemaining() {
		return isReeled() ? reelDurationRemaining : 0;
	}

	private void beginStun(float duration) {
		self.orders.deffering.deferCurrentOrder();
		self.getMotion().stopCurrentMovement();
		self.clearOrder();
		self.combat.stopAttacking();
		self.interruptHard();

		state = State.STUNNED;
		stunDurationRemaining = duration;
	}

	private void beginReel(float duration) {
		// reeling doesn't interrupt attacking or motion, unlike stun
		self.interruptSoft();

		state = State.REELED;
		reelDurationRemaining = duration;

		self.combat.addAttackCooldownRemaining(self.getAttackCooldown());
	}

	/**
	 * Must be called whenever a stun ends.
	 */
	private void endStun() {
		stunDurationRemaining = 0;
		beginReel(Math.max(120, deferredReelAmount));
		deferredReelAmount = 0;
		logger.debug("{} stun ended", self.getName());
		self.orders.deffering.tryToResumeDeferredOrder(); // important to call this AFTER stun state de-set, ie. unit.isStunned() returns false.
	}

	private void endReeled() {
		state = State.NORMAL;
		reelDurationRemaining = 0;
	}

}
