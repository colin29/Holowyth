package com.mygdx.holowyth.skill;

import com.mygdx.holowyth.unit.Unit;

public abstract class UnitGroundSkill extends Skill {

	protected UnitGroundSkill() {
		super(Targeting.UNIT_GROUND);
	}

	public abstract void pluginTargeting(Unit caster, Unit target, float x, float y);

}
