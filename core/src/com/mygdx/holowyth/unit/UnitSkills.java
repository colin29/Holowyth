package com.mygdx.holowyth.unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.skill.Skills;
import com.mygdx.holowyth.skill.WarriorSkills;
import com.mygdx.holowyth.util.exceptions.HoloException;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

public class UnitSkills {

	public final Unit self;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public UnitSkills(Unit unit) {
		self = unit;
	}

	/**
	 * Slot 0 is unused atm, but can be used.
	 */
	Skill[] slot = new Skill[11];
	{
		slot[1] = new Skills.StunTestSkill();
		slot[2] = new Skills.MagicMissile();
		slot[3] = new WarriorSkills.RageBlowSkill();
		slot[4] = new Skills.NovaFlare();
		slot[5] = new Skills.ForcePush();
		slot[6] = new Skills.StaticShock();
	}

	/**
	 * @param slotNumber
	 *            between 0 and 10.
	 * 
	 * @return A cloned instance of the skill, or null if there was no skill in that slot
	 */
	public Skill getSkillInSlot(int slotNumber) {
		if (slotNumber < 0 || slotNumber > 10) {
			throw new HoloIllegalArgumentsException("slot number must be between 0 and 10");
		}

		if (slot[slotNumber] == null) {
			logger.debug("Tried to use skill slot [{}] but no skill was assigned", slotNumber);
			return null;
		}

		try {
			return (Skill) slot[slotNumber].clone();
		} catch (CloneNotSupportedException e) {
			throw new HoloException(e);
		}
	}

	/**
	 * 
	 * @return the skill slots, where you can access the original skill instances. Slots go from indexes 1-10, 0 is unused.
	 */
	public Skill[] getSkillSlots() {
		return slot;
	}

}
