package com.mygdx.holowyth.skill.skill;

import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.skill.Skill.Targeting;
import com.mygdx.holowyth.unit.Unit;

public abstract class UnitSkill extends Skill {

	protected UnitSkill() {
		super(Targeting.UNIT);
	}

	public abstract void pluginTargeting(Unit caster, Unit target);

}
