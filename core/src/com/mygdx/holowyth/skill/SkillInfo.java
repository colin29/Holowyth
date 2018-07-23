package com.mygdx.holowyth.skill;

import com.mygdx.holowyth.skill.Skill.Status;
import com.mygdx.holowyth.skill.Skill.Targeting;

public interface SkillInfo {
	public boolean areEffectsSet();

	public Status getStatus();

	public Targeting getTargeting();
}
