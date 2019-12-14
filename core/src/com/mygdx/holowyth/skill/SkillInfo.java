package com.mygdx.holowyth.skill;

import com.mygdx.holowyth.skill.ActiveSkill.Status;
import com.mygdx.holowyth.skill.ActiveSkill.Targeting;

public interface SkillInfo {
	public boolean areEffectsSet();

	public Status getStatus();

	public Targeting getTargeting();
	
	public CastingInfo getCasting();
}
