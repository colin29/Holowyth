package com.mygdx.holowyth.skill;

import org.eclipse.jdt.annotation.NonNull;

import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.util.Holo;

/**
 * Component of Skill. Default version simply ticks down.
 * 
 * @author Colin Ta
 *
 */
public class Casting implements Cloneable, CastingInfo {

	/**
	 * By default, cast time of 0. Cast-time zero spells will finish casting on the same frame as started so interruptibility are irrelevant
	 * 
	 */
	public float castTime = 0;
	public float castTimeRemaining;

	private boolean completed = false;

	public boolean isInterruptedByDamageOrReel = true;

	private boolean displayedSkillName = false;
	
	public enum CastingType {
		NORMAL, MOBILE;
	}
	public @NonNull CastingType castingType = CastingType.NORMAL; 

	ActiveSkill parent;

	public Casting(ActiveSkill parent) {
		this.parent = parent;
	}

	public void begin(UnitOrderable caster) {

		if (Holo.debugFastCastEnabled) {
			castTime = castTime / 10;
		}
		castTimeRemaining = castTime;
		onBeginCast();
	}

	public void tick() {
		castTimeRemaining -= 1;

		UnitOrderable caster = parent.caster;
		if (castTimeRemaining <= 25 && !displayedSkillName) {
			if (caster.getSide() == Side.PLAYER || Holo.debugDisplayEnemyCastingProgress) {
				caster.getMapInstance().getGfx().makeSkillNameEffect(parent.name + "!", caster);
			}
		}

		if (castTimeRemaining <= 0) {
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
	@Override
	public float getProgress() {
		if (completed)
			return 1;
		if (castTime == 0)
			return 1;
		return (castTime - castTimeRemaining) / castTime;
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
			e.printStackTrace();
			return null;
		}

	}
	public void setCastTimeSec(float sec) {
		castTime = sec/Holo.GAME_FPS;
	}

}
