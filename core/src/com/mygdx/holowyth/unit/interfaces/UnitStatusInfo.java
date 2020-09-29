package com.mygdx.holowyth.unit.interfaces;

public interface UnitStatusInfo {

	/**
	 * @returns movespeed / baseMoveSpeed. For example if a unit was slowed by 30%, it would return 0.7
	 */
	float getMoveSpeedRatio();

	float getMoveSpeed();

	UnitInfo getTauntAttackTarget();

	boolean isTaunted();

	boolean isBlinded();

	boolean isSlowedIgnoringBasicAttackSlow();

	boolean isSlowed();

}
