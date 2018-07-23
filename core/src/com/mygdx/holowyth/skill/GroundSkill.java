package com.mygdx.holowyth.skill;

import com.mygdx.holowyth.unit.Unit;

public abstract class GroundSkill extends Skill {

	protected GroundSkill() {
		super(Targeting.GROUND);
	}
	public abstract void pluginTargeting(Unit caster, float x, float y);

}
