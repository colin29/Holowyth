package com.mygdx.holowyth.unit;

import com.mygdx.holowyth.unit.Item.EquipType;
import com.mygdx.holowyth.unit.Item.ItemType;
import com.mygdx.holowyth.unit.UnitStats.UnitType;

/**
 * Static utility class for mocking up units
 * @author Colin Ta
 *
 */
public class PresetUnits {
	public static void loadUnitStats(UnitStats unit) {
		unit.baseStr = 7;
		unit.baseAgi = 5;
		unit.baseFort = 6;
		unit.basePercep = 6;
	
		unit.baseMaxHp = 100;
		unit.baseMaxSp = 50;
	
		unit.baseMoveSpeed = 2.3f;
	
		unit.level = 3;
	
		unit.unitType = UnitType.PLAYER;
	}

	public static void loadUnitStats2(UnitStats unit) {
	
		unit.baseStr = 5;
		unit.baseAgi = 5;
		unit.baseFort = 5;
		unit.basePercep = 5;
	
		unit.baseMaxHp = 100;
		unit.baseMaxSp = 50;
	
		unit.baseMoveSpeed = 2.3f;
	
		unit.level = 1;
	
		unit.unitType = UnitType.PLAYER;
	}

	public static void loadSomeEquipment(UnitStats unit) {
		Item sword = new Item("Red Sword");
	
		sword.damage = 8;
	
		sword.atkBonus = 2;
		sword.defBonus = 2;
		sword.accBonus = 4;
		sword.armorNegationBonus = 0.1f;
	
		sword.itemType = ItemType.EQUIPMENT;
		sword.equipType = EquipType.WEAPON;
	
		Item ring = new Item("Stone Ring");
		ring.itemType = ItemType.EQUIPMENT;
		ring.equipType = EquipType.ACCESSORY;
		ring.fortBonus = 2;
		ring.percepBonus = 1;
	
		unit.getEquip().mainHand = sword;
		unit.getEquip().accessory1 = ring;
	}
	
	public static void loadArmor(UnitStats unit) {
		Item armor = new Item("Steel Plate", EquipType.ARMOR);
		
		armor.armorBonus = 6;
		armor.dmgReductionBonus = 0.35f;
		
		unit.getEquip().torso = armor;
	}
}
