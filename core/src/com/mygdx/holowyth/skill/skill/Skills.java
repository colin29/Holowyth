package com.mygdx.holowyth.skill.skill;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.skill.skillsandeffects.MageSkills;
import com.mygdx.holowyth.skill.skillsandeffects.WarriorSkills;

/**
 * Common collections of skill
 * 
 * @author Colin Ta
 *
 */
public class Skills {

	public static final List<ActiveSkill> warriorSkills = Collections.unmodifiableList(
			Arrays.asList(new WarriorSkills.RageBlow(),
					new WarriorSkills.Bash(),
					new WarriorSkills.Taunt(),
					new WarriorSkills.DeafeningCry()));

	public static final List<ActiveSkill> mageSkills = Collections.unmodifiableList(
			Arrays.asList(new MageSkills.Fireball(),
					new MageSkills.MagicMissile(),
					new MageSkills.ArcaneBolt(),
					new MageSkills.WindBlades(),
					new MageSkills.Hydroblast(),
					new MageSkills.Thunderclap(),
					new MageSkills.BlindingFlash()));

}