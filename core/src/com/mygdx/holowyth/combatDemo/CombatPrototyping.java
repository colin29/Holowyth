package com.mygdx.holowyth.combatDemo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.UnitStats;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.exceptions.HoloAssertException;

/**
 * Map-lifetime module
 * 
 * @author Colin Ta
 *
 */
public class CombatPrototyping {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final World world;
	private Controls controls;

	private final CombatScenario duelingClose = new CombatScenario();
	private final CombatScenario duelingFar = new CombatScenario();
	private final CombatScenario threeVsThreeClose = new CombatScenario();
	private final CombatScenario threeVsThreeFar = new CombatScenario();
	private final CombatScenario oneVsThreeClose = new CombatScenario();
	private final CombatScenario oneVsThreeFar = new CombatScenario();

	private enum ScenarioType {
		DuelingClose, DuelingFar, ThreeVsThreeClose, ThreeVsThreeFar, OneVsThreeClose, OneVsThreeFar;
	}

	public ScenarioType scenario = ScenarioType.DuelingClose;

	public CombatPrototyping(World world, Controls controls) {
		this.world = world;
		this.controls = controls;
		defineScenarios();
	}

	/**
	 * Sets up the scenario preset
	 */
	public void setupPlannedScenario() {
		switch (scenario) {
		case DuelingClose:
			setupScenario(duelingClose);
			break;
		case DuelingFar:
			setupScenario(duelingFar);
			break;
		case OneVsThreeClose:
			setupScenario(threeVsThreeClose);
			break;
		case OneVsThreeFar:
			setupScenario(threeVsThreeFar);
			break;
		case ThreeVsThreeClose:
			setupScenario(oneVsThreeClose);
			break;
		case ThreeVsThreeFar:
			setupScenario(oneVsThreeFar);
			break;
		default:
			throw new HoloAssertException("Unsupported scenario");

		}
	}

	private final Point mainPoint = new Point(397, 266);

	private void defineScenarios() {
		duelingClose.playerSpawnLocs.add(new Point(mainPoint.x, mainPoint.y));
		duelingClose.enemySpawnLocs.add(new Point(mainPoint.x + 40, mainPoint.y + 20));

		duelingFar.playerSpawnLocs.add(new Point(200, 200));
		duelingFar.enemySpawnLocs.add(new Point(mainPoint.x + 40, mainPoint.y + 20));

		oneVsThreeClose.playerSpawnLocs.add(new Point(mainPoint.x, mainPoint.y));
		oneVsThreeClose.enemySpawnLocs.add(new Point(mainPoint.x + 40, mainPoint.y));
		oneVsThreeClose.enemySpawnLocs.add(new Point(mainPoint.x + 40, mainPoint.y + 40));
		oneVsThreeClose.enemySpawnLocs.add(new Point(mainPoint.x, mainPoint.y + 40));

		oneVsThreeFar.playerSpawnLocs.add(new Point(200, 200));
		oneVsThreeFar.enemySpawnLocs.add(new Point(mainPoint.x + 40, mainPoint.y));
		oneVsThreeFar.enemySpawnLocs.add(new Point(mainPoint.x + 40, mainPoint.y + 40));
		oneVsThreeFar.enemySpawnLocs.add(new Point(mainPoint.x, mainPoint.y + 40));
	}

	public void setupScenario(CombatScenario scenario) {
		// Clear all units in the world

		if (scenario.playerSpawnLocs.isEmpty() && scenario.enemySpawnLocs.isEmpty()) {
			logger.debug("Scenario loaded contains no player or enemy units.");
			return;
		}

		for (var p : scenario.playerSpawnLocs) {
			var playerUnit = world.spawnUnit(p.x, p.y, Unit.Side.PLAYER);
			playerUnit.setName("Player");
			loadPlayerUnitStats(playerUnit.stats);
			playerUnit.stats.prepareUnit();
		}
		for (var p : scenario.enemySpawnLocs) {
			var enemyUnit = world.spawnUnit(p.x, p.y, Unit.Side.ENEMY);
			enemyUnit.setName("Goblin");
			loadEnemyUnitStats(enemyUnit.stats);
			enemyUnit.stats.prepareUnit();
		}
	}

	public List<Unit> spawnSomeEnemyUnits() {
		ArrayList<Unit> someUnits = new ArrayList<Unit>();

		someUnits.add(world.spawnUnit(mainPoint.x + 40, mainPoint.y, Unit.Side.ENEMY));
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
	public List<Unit> spawnSomePlayerUnits() {

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

	private void loadEnemyUnitStats(UnitStats unit) {

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

	private void loadPlayerUnitStats(UnitStats unit) {

		unit.testAtk = 5;
		unit.testDef = 5;

		unit.maxHpBase = 100;
		unit.maxSpBase = 50;

		unit.level = 0;

		unit.testDamage = 7;

		unit.fortBase = 5;
		// unit.baseMoveSpeed = Holo.defaultUnitMoveSpeed * 3;
	}

	public static class CombatScenario {
		public final List<Point> playerSpawnLocs = new ArrayList<Point>();
		public final List<Point> enemySpawnLocs = new ArrayList<Point>();
	}
}
