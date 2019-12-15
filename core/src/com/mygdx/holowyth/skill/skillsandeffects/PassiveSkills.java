package com.mygdx.holowyth.skill.skillsandeffects;

import com.mygdx.holowyth.skill.Skill;

public class PassiveSkills {

	public final static Skill basicCombatTraining = new Skill() {
		{
			name = "Basic Combat Training";
			atkBonus = 2;
			defBonus = 2;
			stabBonus = 2;

			damBonus = 1;
		}
	};
}
