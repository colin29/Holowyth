package com.mygdx.holowyth.unit.units;

import com.mygdx.holowyth.unit.UnitStatValues;

public class MonsterStats {

	public static final UnitStatValues baseHuman = new UnitStatValues() {
		{
			maxHp = 100;
			maxSp = 100;
			damage = 2;

			atk = 3;
			def = 4;
			force = 3;
			stab = 3;
		}
	};

	// At the moment, monsters solely use their base stats -- they don't use equipment

	public static final UnitStatValues goblin = new UnitStatValues() {
		{
			maxHp = 5;
			maxSp = 50;

			damage = 6;
			atk = 7;
			def = 5;
			force = 3;
			stab = 4;
		}
	};

}
