package com.mygdx.holowyth.skill.skill;

import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;

/**
 * Not used currently but is supported
 */
public abstract class UnitGroundSkill extends ActiveSkill {

	protected UnitGroundSkill() {
		super(Targeting.UNIT_GROUND);
	}

	public abstract boolean pluginTargeting(UnitOrderable caster, UnitOrderable target, float x, float y);

}
