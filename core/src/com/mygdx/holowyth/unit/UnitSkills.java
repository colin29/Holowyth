package com.mygdx.holowyth.unit;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.skill.ActiveSkill.Status;
import com.mygdx.holowyth.util.exceptions.HoloException;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

public class UnitSkills {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public final Unit self;

	private float curGlobalSkillsCooldown;

	// Parameters
	public static final int NUM_SKILL_SLOTS = 10;

	/** Skills the unit knows */
	private Set<Skill> skills = new LinkedHashSet<Skill>();
	/**
	 * Note: slot 0 cannot be used
	 */
	private ActiveSkill[] slot = new ActiveSkill[11];

	/**
	 * The skill the character is actively casting or channelling, else null. The Skill class will reset
	 * this when the active portion has finished.
	 */
	ActiveSkill activeSkill;


	public UnitSkills(Unit unit) {
		self = unit;
	}

	public void clearMapLifetimeData() {
		activeSkill = null;
	}

	void tick() {
		if (activeSkill != null)
			activeSkill.tick();

		tickSkillCooldowns();
	}

	/**
	 * Soft interrupts are caused by damage and reel. Only some skills are affected; most melee skills
	 * are not.
	 */
	public void interruptSoft() {
		if (isCasting() || isChannelling()) {
			activeSkill.interrupt(false);
		}
	}

	public void interruptRangedSkills() {
		if (isCasting() || isChannelling()) {
			if (activeSkill != null && activeSkill.isRangedSkill()) {
				activeSkill.interrupt(false);
			}
		}
	}

	/**
	 * Hard interrupts are caused by stun / knockback
	 */
	public void interruptHard() {
		if (isCasting() || isChannelling()) {
			activeSkill.interrupt(true);
		}
	}

	/**
	 * @param slotNumber between 1 and 10.
	 * 
	 * @return A cloned instance of the skill, or null if there was no skill in that slot
	 */
	public ActiveSkill copySkillInSlot(int slotNumber) {
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
	 * @return the skill slots, where you can access the original skill instances. Slots go from indexes
	 *         1-10, 0 is unused.
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
		curGlobalSkillsCooldown = Math.max(0, curGlobalSkillsCooldown - 1);
	}

	/**
	 * Doesn't add a skill to the skill bar
	 * 
	 * @return
	 */
	public boolean addSkill(Skill s) {
		boolean added = skills.add(s);
		self.stats.recalculateStats();
		return added;
	}

	public void addSkills(List<? extends Skill> skills) {
		for (Skill s : skills) {
			addSkill(s);
		}
	}

	public boolean removeSkill(Skill s) {
		return skills.remove(s);
	}

	public void slotSkills(ActiveSkill... skills) {
		slotSkills(Arrays.asList(skills));
	}

	/**
	 * Clears the unit skill slots, then tries to slot as many skills in as possible
	 * 
	 * @param activeSkills
	 * @return
	 */
	public void slotSkills(List<ActiveSkill> skills) {
		for (int i = 0; i < slot.length; i++) {
			slot[i] = null;
		}
		for (int i = 0; i < skills.size(); i++) {
			if (i + 1 > NUM_SKILL_SLOTS) {
				return;
			}
			try {
				slot[i + 1] = (ActiveSkill) skills.get(i).clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
	}

	public Set<Skill> getSkills() {
		return Collections.unmodifiableSet(skills);
	}

	ActiveSkill getActiveSkill() {
		return activeSkill;
	}

	void setActiveSkill(ActiveSkill activeSkill) {
		this.activeSkill = activeSkill;
	}

	public boolean isCasting() {
		return this.activeSkill != null && activeSkill.getStatus() == Status.CASTING;
	}

	public boolean isChannelling() {
		return this.activeSkill != null && activeSkill.getStatus() == Status.CHANNELING;
	}

	public boolean isSkillsOnCooldown() {
		return (curGlobalSkillsCooldown > 0);
	}

	public float getCurGlobalCooldown() {
		return curGlobalSkillsCooldown;
	}

	public void setCurGlobalCooldown(float curGlobalCooldown) {
		this.curGlobalSkillsCooldown = curGlobalCooldown;
	}

}
