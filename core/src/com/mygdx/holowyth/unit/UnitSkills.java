package com.mygdx.holowyth.unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.skill.Skills;
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
	Skill[] skills = new Skill[11];
	{
		skills[1] = new Skills.Explosion();
		skills[2] = new Skills.ExplosionLongCast();
		skills[3] = new Skills.StaticShock();
		skills[4] = new Skills.NovaFlare();
		skills[5] = new Skills.ForcePush();
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

		if (skills[slotNumber] == null) {
			logger.debug("Tried to use skill slot [{}] but no skill was assigned", slotNumber);
			return null;
		}

		try {
			return (Skill) skills[slotNumber].clone();
		} catch (CloneNotSupportedException e) {
			throw new HoloException(e);
		}
	}

}
