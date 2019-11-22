package com.mygdx.holowyth.unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.util.exceptions.HoloAssertException;

/**
 * Handles logic for whether a unit is stunned or not
 *
 */
public class UnitStun {
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
		if (stunDurationRemaining <= 0 &&
				!self.motion.isBeingKnockedBack()) { // if unit is still being knockbacked, wait until knockback ends before ending stun
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
	 * 
	 */
	void applyStun(float duration) {
		if (duration < 0) {
			logger.warn("tried to apply stun of negative duration");
			return;
		}
		switch (state) {
		case NORMAL:
			beginStun(duration);
			break;
		case REELED:
			deferredReelAmount = reelDurationRemaining;
			beginStun(duration);
			break;
		case STUNNED:
			stunDurationRemaining += duration;
			break;
		default:
			throw new HoloAssertException("Unsupported state");
		}
	}

	/**
	 * Knockback is treated exactly as a stun stun, except that the unit will remain stunned until knockback motion ends
	 */
	void applyKnockbackStun(float duration, Vector2 dv) {
		applyStun(duration);
		self.motion.applyKnockBackVelocity(dv.x, dv.y);
	}

	boolean isUnitBeingKnockedBack() {
		return self.motion.isBeingKnockedBack();
	}

	/**
	 * 
	 */
	void applyReel(float duration) {
		if (duration < 0) {
			logger.warn("tried to apply reel of negative duration");
			return;
		}

		switch (state) {
		case NORMAL:
			beginReel(duration);
			break;
		case REELED:
			reelDurationRemaining += duration;
			break;
		case STUNNED:
			deferredReelAmount += duration;
			break;
		default:
			throw new HoloAssertException("Unsupported state");
		}
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
		self.motion.stopCurrentMovement();
		self.clearOrder();
		self.stopAttacking();
		self.interruptCastingAndChannelling();

		state = State.STUNNED;
		stunDurationRemaining = duration;
	}

	private void beginReel(float duration) {

		self.motion.stopCurrentMovement();
		self.clearOrder();
		// reeling doesn't interrupt attacking, unlike stun
		self.interruptCastingAndChannelling();

		state = State.REELED;
		reelDurationRemaining = duration;

		self.addAttackCooldownRemaining(self.getAttackCooldown());
	}

	private void endStun() {
		stunDurationRemaining = 0;
		beginReel(Math.max(120, deferredReelAmount));
		deferredReelAmount = 0;
	}

	private void endReeled() {
		state = State.NORMAL;
		reelDurationRemaining = 0;
	}

}
