package com.mygdx.holowyth.skill.skill;

import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.skill.Skill.Targeting;
import com.mygdx.holowyth.unit.Unit;

public abstract class GroundSkill extends Skill {

	public float aimingHelperRadius;

	protected GroundSkill() {
		super(Targeting.GROUND);
	}

	public abstract void pluginTargeting(Unit caster, float x, float y);

}
