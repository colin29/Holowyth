package com.mygdx.holowyth.unit.interfaces;

import com.mygdx.holowyth.skill.SkillInfo;
import com.mygdx.holowyth.unit.Unit.Order;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.UnitMotion;
import com.mygdx.holowyth.util.dataobjects.Point;

public interface UnitInfo {

	public float getRadius();

	public Point getPos();

	public float getX();

	public float getY();

	public Side getSide();

	public UnitStatsInfo getStats();

	public Order getCurrentOrder();

	public UnitInfo getTarget();

	public boolean isAttacking();

	public UnitInfo getAttacking();

	public SkillInfo getActiveSkill();

	public boolean isCastingOrChanneling();

	/**
	 * For a short time when a unit starts retreating they can't be given any other commands
	 */
	public boolean isRetreatCooldownActive();

	public UnitMotion getMotion();

	public boolean isAPlayerCharacter();
}
