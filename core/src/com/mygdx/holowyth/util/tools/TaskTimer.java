package com.mygdx.holowyth.util.tools;

/**
 * Can use to manually run tasks at a certain interval
 * 
 * @author Colin Ta
 *
 */
public class TaskTimer {

	boolean started = false;
	
	long timeOfLastFrame;
	long timeSinceLastFrame;
	public long frameNumber = 0;
	long intervalTimeMili; // in milliseconds
	long extraTime; // in nanoseconds

	//  fields for returning absolute elapsed time
	long timeInitial;
	
	public TaskTimer() {
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


}
