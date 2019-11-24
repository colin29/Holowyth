package com.mygdx.holowyth.unit.interfaces;

public interface UnitStatsInfo {
	float getHp();

	float getSp();

	int getMaxHp();

	int getMaxSp();

	int getBaseMaxHp();

	int getBaseMaxSp();

	/**
	 * Gets ratio of current hp to maxHp
	 */
	float getHpRatio();

	/**
	 * Gets ratio of current sp to maxSp
	 */
	float getSpRatio();

	float getBaseMoveSpeed();

	int getAtk();

	int getDef();

	int getForce();

	int getStab();

	int getAcc();

	int getDodge();

	int getStr();

	int getAgi();

	int getFort();

	int getPercep();

	String getName();

	/**
	 * Gets the statistical movespeed of the object, modified by slows. Note this is the speed of the unit for "normal" motion (as opposed to
	 * knockback motion).
	 */
	float getMoveSpeed();

	float getStunDurationRemaining();

	boolean isStunned();

	boolean isReeled();

	float getReeledDurationRemaining();

	float getArmorNegation();

	/**
	 * Takes on values between 0 and 1. For example, 0.2 is 20% damage reduction from armor
	 */
	float getPercentageArmor();

	int getArmorPiercing();

	int getArmor();
}
