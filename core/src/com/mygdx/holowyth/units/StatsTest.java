package com.mygdx.holowyth.units;

import com.mygdx.holowyth.units.Item.EquipType;
import com.mygdx.holowyth.units.Item.ItemType;
import com.mygdx.holowyth.units.Unit.UnitType;

/**
 * Tests some of the unit functionality, but is a simple program instead of a libgdx application.
 * @author Colin Ta
 *
 */
public class StatsTest {

	static Unit unit;
	static Item myItem;

	public static void main(String[] args){
		unit = new Unit("Arthur");
		
		loadDummyUnitStats(unit);
		loadDummyEquipment(unit);
		unit.recalculateStats();
		unit.prepareUnit();
		
		
		unit.printInfo();
		
		Unit unitB = new Unit("Bob");
		loadDummyUnitStats2(unitB);
		unitB.recalculateStats();
		unitB.printInfo();
		
		
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
		
		unit.unitType = UnitType.PLAYER;
	}
	
	public static void loadDummyUnitStats2(Unit unit) {

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

	public static void loadDummyEquipment(Unit unit) {
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
