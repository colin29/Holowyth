package com.mygdx.holowyth.skill.skill;

import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.unit.Unit;

/**
 * A skill that requires no additional targeting (besides the base which provides the casting unit)
 * 
 * @author Colin Ta
 *
 */
public abstract class NoneSkill extends ActiveSkill {
	
	/**
	 * Whether the skill requires the unit be attacking
	 */
	public boolean isMeleeSkill;
	
	protected NoneSkill() {
		super(Targeting.NONE);
	}

	/**
	 * @return true if targeting was successful
	 */
	public abstract boolean pluginTargeting(Unit caster);
}
