package com.mygdx.holowyth.skill.skill;

import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.skill.ActiveSkill.Targeting;
import com.mygdx.holowyth.unit.Unit;

public abstract class UnitSkill extends ActiveSkill {

	protected UnitSkill() {
		super(Targeting.UNIT);
	}

	public abstract void pluginTargeting(Unit caster, Unit target);

}
