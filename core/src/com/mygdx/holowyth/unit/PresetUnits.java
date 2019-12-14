package com.mygdx.holowyth.unit;

import com.mygdx.holowyth.unit.Item.EquipType;

/**
 * Static utility class for mocking up units
 * 
 * @author Colin Ta
 *
 */
public class PresetUnits {

	public static void loadBasicWeapon(UnitStats unit) {
		Item sword = new Item("Wooden Sword", EquipType.WEAPON);

		sword.damage = 5;
		sword.atkBonus = 1;
		sword.accBonus = 3;

		unit.getEquip().mainHand = sword;
	}

	public static void loadSomeEquipment(UnitStats unit) {
		Item sword = new Item("Red Sword", EquipType.WEAPON);

		sword.damage = 8;

		sword.atkBonus = 2;
		sword.defBonus = 2;
		sword.accBonus = 4;
		sword.armorNegationBonus = 0.1f;

		Item ring = new Item("Stone Ring", EquipType.ACCESSORY);
		ring.fortBonus = 2;
		ring.percepBonus = 1;

		unit.getEquip().mainHand = sword;
		unit.getEquip().accessory1 = ring;
	}

	public static void loadArmor(UnitStats unit) {
		Item armor = new Item("Chain Mail", EquipType.ARMOR);

		armor.armorBonus = 3;
		armor.dmgReductionBonus = 0.20f;

		unit.getEquip().torso = armor;
	}
}
