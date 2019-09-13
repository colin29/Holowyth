package com.mygdx.holowyth.unit.interfaces;

public interface UnitStatsInfo {
	public float getHp();

	public float getSp();

	public int getMaxHp();

	public int getMaxSp();

	public int getBaseMaxHp();

	public int getBaseMaxSp();

	/**
	 * Gets ratio of current hp to maxHp
	 */
	public float getHpRatio();

	/**
	 * Gets ratio of current sp to maxSp
	 */
	public float getSpRatio();

	public float getBaseMoveSpeed();

	public int getExp();

	public int getExpGives();

	public int getAtk();

	public int getDef();

	public int getForce();

	public int getStab();

	public int getAcc();

	public int getDodge();

	public int getStr();

	public int getAgi();

	public int getFort();

	public int getPercep();

	public String getName();
}
