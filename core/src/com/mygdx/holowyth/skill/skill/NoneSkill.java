package com.mygdx.holowyth.skill.skill;

import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.unit.Unit;

/**
 * A skill that requires no additional targeting (besides the base which provides the casting unit)
 * 
 * @author Colin Ta
 *
 */
public abstract class NoneSkill extends Skill {
	protected NoneSkill() {
		super(Targeting.NONE);
	}

	/**
	 * @return true if targeting was successful
	 */
	public abstract boolean pluginTargeting(Unit caster);
}
