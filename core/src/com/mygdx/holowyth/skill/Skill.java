package com.mygdx.holowyth.skill;

import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;

public class Skill {
	public String name = "Skill name";
	public int atkBonus;
	public int defBonus;
	public int forceBonus;
	public int stabBonus;

	public int damBonus;
	
	/**
	 * Called if the parent unit attacks (doesn't matter success)
	 */
	public void onUnitAttack(Unit parent, UnitOrderable target) {
	}

}
