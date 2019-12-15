package com.mygdx.holowyth.combatDemo.prototyping;

import com.mygdx.holowyth.unit.item.Equip;
import com.mygdx.holowyth.unit.item.Equip.EquipType;

/**
 * Contains equipment templates which can be copied and given to units
 * 
 * @author Colin Ta
 *
 */
public class Equips {

	public final static Equip longSword = new Equip("Long Sword", EquipType.WEAPON) {
		{
			markAsTemplate();
			bonus.damage = 5;
			bonus.atk = 4;
			bonus.def = 1;
			bonus.force = 2;
			bonus.stab = 1;
		}
	};

	public final static Equip staff = new Equip("Staff", EquipType.WEAPON) {
		{
			markAsTemplate();
			bonus.damage = 2;
			bonus.atk = 2;
			bonus.def = 1;
			bonus.force = 3;
			bonus.stab = 2;
		}
	};

}