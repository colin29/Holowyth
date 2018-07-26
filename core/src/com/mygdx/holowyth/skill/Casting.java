package com.mygdx.holowyth.skill;

import com.mygdx.holowyth.skill.Skill.Status;
import com.mygdx.holowyth.unit.Unit;

/**
 * Component of Skill.
 * Default version simply ticks down.
 * @author Colin Ta
 *
 */
public class Casting implements Cloneable, CastingInfo {
	
	float castTime = 0; //by default, cast time of 0
	float castTimeRemaining;
	
	private boolean completed = false;
	
	Skill parent;
	
	public Casting(Skill parent) {
		this.parent = parent;
	}
	
	public void begin(Unit caste) {
		
		this.castTimeRemaining = castTime;
		onBeginCast();
	}
	
	public void tick() {
		castTimeRemaining -=1;
		if(castTimeRemaining <= 0) {
			completed = true;
			onFinishCast();
		}
	}
	
	public boolean isComplete() {
		return completed;
	}
	// Get the progress of this casting
	/**
	 * 
	 * @return current progress, from 0 to 1
	 */
	public float getProgress() {
		if(completed)
			return 1;
		if(castTime == 0)
			return 1;
		return (castTime-castTimeRemaining) / castTime;
	}
	
	protected void onInterrupt() {
	}
	protected void onBeginCast() {
	}
	protected void onFinishCast() {
	}
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}

}
