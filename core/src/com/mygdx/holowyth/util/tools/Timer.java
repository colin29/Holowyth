package com.mygdx.holowyth.util.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Update must be called on the timer
 * @author Colin
 *
 */
public class Timer {

	private boolean started;
	private boolean isPaused;
	private float timeElapsedMili;

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	public void start() {
		if (!started) {
			started = true;
			timeElapsedMili = 0;
		}
	}

	public void update(float timeInSeconds) {
		if (!isPaused) {
			timeElapsedMili += timeInSeconds * 1000;
		}
	}

	/**
	 * Re-inits the timer starting now
	 */
	public void restart() {
		started = true;
		timeElapsedMili = 0;
		isPaused = false;
	}

	public float getTimeElapsedMili() {
		return timeElapsedMili;
	}

	public float getTimeElapsedSeconds() {
		return  timeElapsedMili / 1000.0f;
	}

	public void pause() {
		isPaused = true;
	}

	public void resume() {
		isPaused = false;
	}
}
