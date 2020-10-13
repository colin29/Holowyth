package com.mygdx.holowyth.gamedata.skillsandeffects;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.util.DataUtil;

/**
 * Common collections of skill.
 * 
 * @author Colin Ta
 *
 */
@NonNullByDefault
@SuppressWarnings("null")
public class Skills {

	public static final List<ActiveSkill> warriorSkills = Collections
			.unmodifiableList(Arrays.asList(new WarriorSkills.RageBlow(), new WarriorSkills.Bash(),
					new WarriorSkills.Taunt(), new WarriorSkills.DeafeningCry()));
	
	public static final List<ActiveSkill> swordsmanSkills = Collections
			.unmodifiableList(Arrays.asList(new SwordsmanSkills.TripleStrike()));

	public static final List<ActiveSkill> mageSkills = Collections
			.unmodifiableList(Arrays.asList(new MageSkills.Fireball(), 
					new MageSkills.MagicMissile(),
					new MageSkills.ArcaneBolt(), 
					new MageSkills.WindBlades(), 
					new MageSkills.Hydroblast(),
					new MageSkills.Thunderclap(), 
					new MageSkills.BlindingFlash()));

	public static final List<ActiveSkill> rangerSkills = Collections
			.unmodifiableList(Arrays.asList(new RangerSkills.CrossSlash(), new RangerSkills.Archery()));
	public static final List<ActiveSkill> priestSkills = Collections
			.unmodifiableList(Arrays.asList(new PriestSkills.Heal(), new PriestSkills.StaffStrike()));
	
	public static final List<ActiveSkill> darkKnightSkills = Collections
			.unmodifiableList(Arrays.asList(new DarkKnightSkills.BladeInTheDark(), 
					new DarkKnightSkills.ThrowSand()));
	

	private static final String headerText = "Skill name; SP cost; Cast time (sec); Cooldown (sec); Global CD; Description";

	public static void main(String[] args) { // For output into external google sheets for easy viewing
		printSemiColonSeperatedTableOfSkills(warriorSkills, "Warrior Skills");
		printSemiColonSeperatedTableOfSkills(mageSkills, "Mage Skills");
		printSemiColonSeperatedTableOfSkills(rangerSkills, "Ranger Skills");
		printSemiColonSeperatedTableOfSkills(priestSkills, "Priest Skills");
	}

	private static void printSemiColonSeperatedTableOfSkills(List<ActiveSkill> skills, String tableName) {
		System.out.println(tableName);
		System.out.println(headerText);
		for (ActiveSkill s : skills) {
			System.out.println(getSkillInfoAsString(s));
		}
		System.out.println();
	}

	private static String getSkillInfoAsString(ActiveSkill s) {
		return String.format("%s; %d; %s; %s; %s; %s", s.name, s.spCost, s.getDescription(),
				DataUtil.round(s.casting.castTime / 60), DataUtil.round(s.cooldown / 60),
				DataUtil.round(s.globalCooldown / 60));
	}

}
