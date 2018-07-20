package com.mygdx.holowyth.skill;

import com.mygdx.holowyth.skill.Skill.Status;
import com.mygdx.holowyth.unit.Unit;

/**
 * Component of Skill.
 * Default version simply ticks down.
 * @author Colin Ta
 *
 */
public class Casting {
	
	float castTime = 0; //by default, cast time of 0
	float castTimeRemaining;
	
	Skill parent;
	
	public Casting(Skill parent) {
		this.parent = parent;
	}
	
	public void begin(Unit caste) {
		System.out.println("Casting started");
		
		
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
	
	private boolean completed = false;
	public boolean isComplete() {
		return completed;
	}
	
	protected void onInterrupt() {
	}
	protected void onBeginCast() {
	}
	protected void onFinishCast() {
	}

}
