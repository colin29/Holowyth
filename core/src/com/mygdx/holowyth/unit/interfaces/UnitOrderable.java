package com.mygdx.holowyth.unit.interfaces;

import com.mygdx.holowyth.skill.Skill;

public interface UnitOrderable extends UnitInfo {

	void orderMove(float dx, float dy);

	/**
	 * @param unit
	 * @return Whether the command was valid and accepted
	 */
	boolean orderAttackUnit(UnitOrderable unit);

	boolean orderAttackUnit(UnitOrderable unit, boolean isHardOrder);

	boolean orderSwitchAttackUnit(UnitOrderable target, boolean isHardOrder);

	void orderAttackMove(float x, float y);

	void orderRetreat(float x, float y);

	/**
	 * A stop order stops a unit's motion and current order. You cannot use stop to cancel your own casting atm.
	 */
	void orderStop();

	void orderUseSkill(Skill skill);

}