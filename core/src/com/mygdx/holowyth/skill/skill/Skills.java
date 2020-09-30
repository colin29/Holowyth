package com.mygdx.holowyth.skill.skill;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mygdx.holowyth.gamedata.skillsandeffects.MageSkills;
import com.mygdx.holowyth.gamedata.skillsandeffects.WarriorSkills;
import com.mygdx.holowyth.skill.ActiveSkill;

/**
 * Common collections of skill
 * 
 * @author Colin Ta
 *
 */
@SuppressWarnings("null")
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
