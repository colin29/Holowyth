package com.mygdx.holowyth.skill.skill;

import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.unit.Unit;

public abstract class UnitGroundSkill extends ActiveSkill {

	protected UnitGroundSkill() {
		super(Targeting.UNIT_GROUND);
	}

	public abstract void pluginTargeting(Unit caster, Unit target, float x, float y);

}
