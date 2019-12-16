package com.mygdx.holowyth.unit;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.skill.skillsandeffects.MageSkills;
import com.mygdx.holowyth.skill.skillsandeffects.Skills;
import com.mygdx.holowyth.skill.skillsandeffects.WarriorSkills;
import com.mygdx.holowyth.util.exceptions.HoloException;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

public class UnitSkills {

	public final Unit self;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public UnitSkills(Unit unit) {
		self = unit;
	}

	public static final int NUM_SKILL_SLOTS = 10;

	private Set<Skill> skills = new LinkedHashSet<Skill>();

	/**
	 * Slot 0 cannot be used
	 */
	ActiveSkill[] slot = new ActiveSkill[11];
	{

		// slot[1] = new WarriorSkills.RageBlow();
		// slot[2] = new RangerSkills.CrossSlash();

		slot[1] = new MageSkills.Fireball();
		slot[2] = new MageSkills.MagicMissile();
		slot[3] = new MageSkills.Thunderclap();
		slot[4] = new MageSkills.BlindingFlash();
		slot[5] = new MageSkills.Hydroblast();
		slot[6] = new Skills.StaticShock();
		slot[7] = new WarriorSkills.RageBlow();
		slot[8] = new MageSkills.ArcaneBolt();

	}

	/**
	 * @param slotNumber
	 *            between 1 and 10.
	 * 
	 * @return A cloned instance of the skill, or null if there was no skill in that slot
	 */
	public ActiveSkill getSkillInSlot(int slotNumber) {
		if (slotNumber < 1 || slotNumber > 10) {
			throw new HoloIllegalArgumentsException("slot number must be between 1 and 10");
		}

		if (slot[slotNumber] == null) {
			logger.debug("Tried to use skill slot [{}] but no skill was assigned", slotNumber);
			return null;
		}

		try {
			ActiveSkill skill = (ActiveSkill) slot[slotNumber].clone();
			skill.setParent(slot[slotNumber]);
			return skill;
		} catch (CloneNotSupportedException e) {
			throw new HoloException(e);
		}
	}

	/**
	 * 
	 * @return the skill slots, where you can access the original skill instances. Slots go from indexes 1-10, 0 is unused.
	 */
	public ActiveSkill[] getSkillSlots() {
		return slot;
	}

	public void tickSkillCooldowns() {
		for (int i = 1; i <= 10; i++) {
			var skill = slot[i];
			if (skill != null) {
				skill.tickCooldown();
			}
		}
	}

	public boolean addSkill(Skill s) {
		boolean added = skills.add(s);
		self.stats.recalculateStats();
		return added;
	}

	public boolean removeSkill(Skill s) {
		return skills.remove(s);
	}

	public Set<Skill> getSkills() {
		return Collections.unmodifiableSet(skills);
	}

}
