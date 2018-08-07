package com.mygdx.holowyth.unit;

import com.mygdx.holowyth.skill.SkillInfo;
import com.mygdx.holowyth.unit.Unit.Order;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.util.data.Point;

public interface UnitInfo {

	public float getRadius();

	public Point getPos();

	public float getX();

	public float getY();

	public Side getSide();

	public UnitStatsInfo getStats();

	public Order getCurrentOrder();

	public UnitInfo getTarget();

	public UnitInfo getAttacking();

	public SkillInfo getActiveSkill();

	/**
	 * For a short time when a unit starts retreating they can't be given any other commands
	 */
	public boolean isRetreatCooldownActive();
}
