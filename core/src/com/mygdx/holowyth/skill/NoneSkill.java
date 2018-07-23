package com.mygdx.holowyth.skill;

import com.mygdx.holowyth.unit.Unit;

/**
 * A skill that requires no additional targeting (besides the base which provides the casting unit)
 * @author Colin Ta
 *
 */
public abstract class NoneSkill extends Skill{
	protected NoneSkill() {
		super(Targeting.NONE);
	}

	public abstract void pluginTargeting(Unit caster);
}
