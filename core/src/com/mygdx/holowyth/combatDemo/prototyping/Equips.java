package com.mygdx.holowyth.combatDemo.prototyping;

import com.mygdx.holowyth.unit.Item;
import com.mygdx.holowyth.unit.Item.EquipType;

/**
 * Static utility class for mocking up units
 * 
 * @author Colin Ta
 *
 */
public class Equips {

	public final static Item longSword = new Item("Long Sword", EquipType.WEAPON) {
		{
			markAsTemplate();
			bonus.damage = 5;
			bonus.atk = 4;
			bonus.def = 1;
			bonus.force = 2;
			bonus.stab = 1;
		}
	};

	public final static Item staff = new Item("Staff", EquipType.WEAPON) {
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