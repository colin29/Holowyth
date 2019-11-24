package com.mygdx.holowyth.skill.skilltypes;

import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.skill.Skill.Targeting;
import com.mygdx.holowyth.unit.Unit;

public abstract class UnitGroundSkill extends Skill {

	protected UnitGroundSkill() {
		super(Targeting.UNIT_GROUND);
	}

	public abstract void pluginTargeting(Unit caster, Unit target, float x, float y);

}
