package com.mygdx.holowyth.unit.statuseffect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpeedIncreaseEffect {

	private float duration;
	private float speedIncrease;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public SpeedIncreaseEffect(float duration, float speedIncrease) {
		this.duration = duration;

		if (speedIncrease < 0) {
			speedIncrease = 0;
			logger.warn("Tried to add a <0 speed increase effect. Value was clamped.");
		}

		this.speedIncrease = speedIncrease;
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

	public float getSpeedIncrease() {
		return speedIncrease;
	}

}
