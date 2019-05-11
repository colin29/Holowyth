package com.mygdx.holowyth.test.demo.statsBranch;

import com.mygdx.holowyth.test.demo.statsBranch.UnitStatsSB.UnitType;
import com.mygdx.holowyth.unit.Item;
import com.mygdx.holowyth.unit.Item.EquipType;
import com.mygdx.holowyth.unit.Item.ItemType;

/**
 * Tests some of the unit functionality, but is a simple program instead of a libgdx application.
 * 
 * @author Colin Ta
 *
 */
public class StatsTest {

	static UnitStatsSB unit;
	static Item myItem;

	public static void main(String[] args) {
		unit = new UnitStatsSB("Arthur");

		loadDummyUnitStats(unit);
		loadDummyEquipment(unit);
		unit.recalculateStats();
		unit.prepareUnit();
		unit.printInfo();

		UnitStatsSB unitB = new UnitStatsSB("Bob");
		loadDummyUnitStats2(unitB);
		unitB.recalculateStats();
		unitB.printInfo();

	}

	public static void loadDummyUnitStats(UnitStatsSB unit) {

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

	public static void loadDummyUnitStats2(UnitStatsSB unit) {

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

	public static void loadDummyEquipment(UnitStatsSB unit) {
		Item sword = new Item("Red Sword");

		sword.damage = 8;
		sword.atkBonus = 2;
		sword.defBonus = 2;
		sword.accBonus = 4;

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

}
