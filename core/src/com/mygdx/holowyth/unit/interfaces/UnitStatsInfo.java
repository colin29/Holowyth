package com.mygdx.holowyth.unit.interfaces;

import com.mygdx.holowyth.unit.UnitStatValues;

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

	int getDodge();

	int getStr();

	int getAgi();

	int getFort();

	int getPercep();

	String getName();

	float getArmorNegation();

	/**
	 * Takes on values between 0 and 1. For example, 0.2 is 20% damage reduction from armor
	 */
	float getPercentageArmor();

	int getArmorPiercing();

	int getArmor();


	UnitStatValues getSkillBonuses();

	UnitStatValues getEquipBonuses();

	UnitStatValues getBaseStats();

	float getDamage();


}
