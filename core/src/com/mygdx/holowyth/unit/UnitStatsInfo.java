package com.mygdx.holowyth.unit;

import com.mygdx.holowyth.unit.UnitStats.Stance;
import com.mygdx.holowyth.unit.UnitStats.StunState;

public interface UnitStatsInfo {
	public float getHp();
	public int getSp();

	public int getMaxHp();
	public int getMaxSp();

	public int getBaseMaxHp();
	public int getBaseMaxSp();
	
	public float getHpRatio();
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

	public Stance getStance();

	public StunState getStunState();

	public int getStunDurationRemainng();

	public UnitStatsInfo getLookingAt();

	public UnitStatsInfo getOccupiedBy();

	public String getName();
}
