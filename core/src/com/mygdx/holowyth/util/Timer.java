package com.mygdx.holowyth.util;

public class Timer {

	boolean started = false;	
	long timeOfLastFrame;
	long timeSinceLastFrame;
	long curFrame;
	public long frameNumber = 0;
	long intervalTime; // in milliseconds
	
	public Timer(){
		
	}
	
	/**
	 * Can call this redundantly, no problem
	 * @param interval time, in milliseconds
	 */
	public void start(long interval){
		this.intervalTime = interval;
		if(!started){
			timeOfLastFrame = System.nanoTime();
			started = true;
		}
	}
	
	public boolean taskReady(){
		frameNumber +=1;
		timeSinceLastFrame = System.nanoTime() - timeOfLastFrame;
		if(timeSinceLastFrame > intervalTime * 1000000){
			timeOfLastFrame = System.nanoTime();
			return true;
		}
		return false;
	}
	
}
