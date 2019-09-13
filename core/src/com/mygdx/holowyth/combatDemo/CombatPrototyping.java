package com.mygdx.holowyth.combatDemo;

import java.util.ArrayList;
import java.util.List;

import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.UnitStats;
import com.mygdx.holowyth.unit.UnitStats.UnitType;
import com.mygdx.holowyth.util.dataobjects.Point;

public class CombatPrototyping {

	private static Point playerPos = new Point(320, 220);

	public static List<Unit> spawnSomeEnemyUnits(World world) {
		ArrayList<Unit> someUnits = new ArrayList<Unit>();

		someUnits.add(world.spawnUnit(playerPos.x + 40, playerPos.y, Unit.Side.ENEMY));
		someUnits.add(world.spawnUnit(playerPos.x + 40, playerPos.y + 40, Unit.Side.ENEMY));
		someUnits.add(world.spawnUnit(playerPos.x - 40, playerPos.y + 40, Unit.Side.ENEMY));

		for (Unit unit : someUnits) {
			unit.setName("Goblin");

			loadEnemyUnitStats(unit.stats);

			unit.stats.prepareUnit();
			unit.stats.testDamage = 5;
		}
		return someUnits;
	}

	private static Unit playerUnit;

	/**
	 * Create a custom player unit. Calling multiple times will just return the existing player unit.
	 * 
	 * @return
	 */
	public static Unit spawnPlayerUnit(World world) {
		if (playerUnit == null) {
			playerUnit = world.spawnUnit(playerPos.x, playerPos.y, Unit.Side.PLAYER, "Elvin");
			loadPlayerUnitStats(playerUnit.stats);

			playerUnit.stats.prepareUnit();
			playerUnit.stats.printInfo();

			// playerUnit.motion.setSpeedAndScaleAccel(Holo.defaultUnitMoveSpeed);

			return playerUnit;
		} else {
			return playerUnit;
		}
	}

	public static void loadEnemyUnitStats(UnitStats unit) {

		unit.testAtk = 0;
		unit.testDef = 5;

		unit.maxHpBase = 100;
		unit.maxSpBase = 50;

		unit.level = 0;

		unit.unitType = UnitType.PLAYER;
	}

	public static void loadPlayerUnitStats(UnitStats unit) {

		unit.testAtk = 5;
		unit.testDef = 5;

		unit.maxHpBase = 100;
		unit.maxSpBase = 50;

		unit.level = 0;

		unit.testDamage = 7;

		unit.unitType = UnitType.PLAYER;
	}
}
