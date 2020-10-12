package com.mygdx.holowyth.unit.statuseffect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlowEffect {

	private float duration;
	private float slowAmount;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public SlowEffect(float duration, float slowAmount) {
		this.duration = duration;

		if (slowAmount > 1) {
			slowAmount = 1;
			logger.warn("Tried to add a >1 slowAmount slow effect. Value was clamped.");
		}
		if (slowAmount < 0) {
			slowAmount = 0;
			logger.warn("Tried to add a <0 slowAmount slow effect. Value was clamped.");
		}

		this.slowAmount = slowAmount;
	}

	public float getDuration() {
		return duration;
	}

	public void tickDuration() {
		duration -= 1;
	}

	public boolean isExpired() {
		return duration <= 0;
	}

	/**
	 * 
	 * @return The slow amount. Will be between 0 and 1, inclusive.
	 */
	public float getSlowAmount() {
		return slowAmount;
	}

}
