package com.mygdx.holowyth.skill;

import com.mygdx.holowyth.unit.Unit;

public abstract class UnitSkill extends Skill {

	protected UnitSkill() {
		super(Targeting.UNIT);
	}

	public abstract void pluginTargeting(Unit caster, Unit target);

}
