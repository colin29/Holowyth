package com.mygdx.holowyth.util.tools;

/**
 * Can use to manually run tasks at a certain interval
 * 
 * @author Colin Ta
 *
 */
public class Timer {

	boolean started = false;
	
	long timeOfLastFrame;
	long timeSinceLastFrame;
	public long frameNumber = 0;
	long intervalTimeMili; // in milliseconds
	long extraTime; // in nanoseconds

	//  fields for returning absolute elapsed time
	long timeInitial;
	
	public Timer() {

	}

	/**
	 * Can call this redundantly, no problem
	 * 
	 * @param interval
	 *            time, in milliseconds
	 */
	public void start(long interval) {
		this.intervalTimeMili = interval;
		if (!started) {
			timeOfLastFrame = System.nanoTime();
			started = true;
			timeInitial = System.nanoTime();
		}
	}
	public void restart() {
		timeOfLastFrame = System.nanoTime();
		started = true;
		timeInitial = System.nanoTime();
		frameNumber = 0;
		timeSinceLastFrame = 0;
	}

	public boolean taskReady() {
		frameNumber += 1;
		timeSinceLastFrame = System.nanoTime() - timeOfLastFrame;

		if (timeSinceLastFrame > intervalTimeMili * 1000000) {
			extraTime = Math.min(timeSinceLastFrame - intervalTimeMili * 1000000, intervalTimeMili * 1000000);
			timeOfLastFrame = System.nanoTime() - extraTime; // carry over remaining time, up to a max of interval
			return true;
		}
		return false;
	}
	/**
	 * In Miliseconds
	 * @return
	 */
	public int getTimeElapsed() {
		return (int) ((System.nanoTime() - timeInitial)/1000000);
	}
	public float getTimeElapsedSeconds() {
		return (float)( ((System.nanoTime() - timeInitial)/ 1000000000.0));
	}
	public static float getTimeElapsedSeconds(long initialTime) {
		return (float)( ((System.nanoTime() - initialTime)/ 1000000000.0));
	}

}
