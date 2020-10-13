package com.mygdx.holowyth.gamedata.units;

import com.mygdx.holowyth.unit.UnitStatValues;

public class MonsterStats {

	public static final UnitStatValues baseHuman = new UnitStatValues() {
		{
			maxHp = 100;
			maxSp = 100;
			setGeneralDamage(2);

			atk = 3;
			def = 4;
			force = 3;
			stab = 3;
		}
	};

	// At the moment, monsters solely use their base stats -- they don't use equipment

	public static final UnitStatValues goblinScavenger = new UnitStatValues() {
		{
			maxHp = 80;
			maxSp = 50;
			setGeneralDamage(2);
			
			atk = 3;
			def = 4;
			force = 3;
			stab = 3;
		}
	};

}
