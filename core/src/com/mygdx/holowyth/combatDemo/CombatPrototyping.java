package com.mygdx.holowyth.combatDemo;

import java.util.ArrayList;
import java.util.List;

import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.UnitStats;
import com.mygdx.holowyth.util.dataobjects.Point;

public class CombatPrototyping {

	private static Point playerPos = new Point(397, 266);

	public static List<Unit> spawnSomeEnemyUnits(World world) {
		ArrayList<Unit> someUnits = new ArrayList<Unit>();

		someUnits.add(world.spawnUnit(playerPos.x + 40, playerPos.y, Unit.Side.ENEMY));
		// someUnits.add(world.spawnUnit(playerPos.x + 40, playerPos.y + 40, Unit.Side.ENEMY));
		// someUnits.add(world.spawnUnit(playerPos.x + 40, playerPos.y + 80, Unit.Side.ENEMY));

		for (Unit unit : someUnits) {
			unit.setName("Goblin");
			loadEnemyUnitStats(unit.stats);
			unit.stats.prepareUnit();
		}
		return someUnits;
	}

	/**
	 * Create some units on the player's team
	 * 
	 * @return
	 */
	public static List<Unit> spawnSomePlayerUnits(World world) {

		var someUnits = new ArrayList<Unit>();
		someUnits.add(world.spawnUnit(200, 200, Unit.Side.PLAYER));
		// someUnits.add(world.spawnUnit(playerPos.x, playerPos.y, Unit.Side.PLAYER));

		// someUnits.add(world.spawnUnit(playerPos.x - 40, playerPos.y, Unit.Side.PLAYER));
		// someUnits.add(world.spawnUnit(playerPos.x - 40, playerPos.y + 30, Unit.Side.PLAYER));
		// someUnits.add(world.spawnUnit(playerPos.x - 40, playerPos.y - 30, Unit.Side.PLAYER));
		// someUnits.add(world.spawnUnit(188, 197, Unit.Side.PLAYER));

		for (Unit unit : someUnits) {
			unit.setName("Player");
			loadPlayerUnitStats(unit.stats);
			unit.stats.prepareUnit();
		}

		return someUnits;
	}

	public static void loadEnemyUnitStats(UnitStats unit) {

		unit.testAtk = 0;
		unit.testDef = 5;

		unit.maxHpBase = 100;
		unit.maxSpBase = 50;

		unit.level = 0;

		unit.testDamage = 5;

		unit.fortBase = 12;

		unit.armorBase = 2;
		unit.percentageArmorBase = 0.15f;

	}

	public static void loadPlayerUnitStats(UnitStats unit) {

		unit.testAtk = 5;
		unit.testDef = 5;

		unit.maxHpBase = 100;
		unit.maxSpBase = 50;

		unit.level = 0;

		unit.testDamage = 7;

		unit.fortBase = 5;
		// unit.baseMoveSpeed = Holo.defaultUnitMoveSpeed * 3;
	}
}
