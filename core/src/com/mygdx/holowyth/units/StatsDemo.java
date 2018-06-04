package com.mygdx.holowyth.units;

import com.mygdx.holowyth.units.Item.EquipType;
import com.mygdx.holowyth.units.Item.ItemType;

public class StatsDemo {

	static Unit unit;
	static Item myItem;

	public static void main(String[] args){
		unit = new Unit();
		loadDummyUnitStats(unit);
		loadDummyEquipment(unit);
		unit.recalculateStats();
		unit.prepareUnit();
		
	}

	public static void loadDummyUnitStats(Unit unit) {

		unit.baseStr = 7;
		unit.baseAgi = 5;
		unit.baseFort = 6;
		unit.basePercep = 6;

		unit.baseMaxHp = 100;
		unit.baseMaxSp = 50;

		unit.baseMoveSpeed = 2.3f;
		
		unit.level = 3;
	}

	public static void loadDummyEquipment(Unit unit) {
		Item sword = new Item("Red Sword");
		
		sword.damage = 8;
		
		sword.atkBonus = 2;
		sword.defBonus = 2;
		sword.accBonus = 4;
		
		sword.type = ItemType.EQUIP;
		sword.equipType = EquipType.WEAPON;
		
		Item ring = new Item("Stone Ring");
		ring.fortBonus = 2;
		ring.percepBonus = 1;
		
		unit.getEquip().mainHand = sword;
		unit.getEquip().accessory1 = ring;
	}
	

}
