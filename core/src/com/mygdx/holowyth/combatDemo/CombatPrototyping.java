package com.mygdx.holowyth.combatDemo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.skill.skillsandeffects.PassiveSkills;
import com.mygdx.holowyth.unit.AnimatedSprite;
import com.mygdx.holowyth.unit.Animations;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.UnitStats;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * Map-lifetime module
 * 
 * @author Colin Ta
 *
 */
public class CombatPrototyping {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final World world;

	final CombatScenario duelingClose = new CombatScenario();
	final CombatScenario duelingFar = new CombatScenario();
	final CombatScenario oneVsThreeClose = new CombatScenario();
	final CombatScenario oneVsThreeFar = new CombatScenario();
	final CombatScenario threeVsThreeClose = new CombatScenario();
	final CombatScenario threeVsThreeFar = new CombatScenario();

	public CombatScenario scenario = threeVsThreeClose;

	public CombatPrototyping(World world, Controls controls) {
		this.world = world;
		defineScenarios();
	}

	/**
	 * Sets up the scenario preset
	 */
	public void setupPlannedScenario() {
		setup(scenario);
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

		threeVsThreeFar.playerSpawnLocs.add(new Point(200, 200));
		threeVsThreeFar.playerSpawnLocs.add(new Point(200, 240));
		threeVsThreeFar.playerSpawnLocs.add(new Point(200, 160));
		threeVsThreeFar.enemySpawnLocs.add(new Point(mainPoint.x + 40, mainPoint.y));
		threeVsThreeFar.enemySpawnLocs.add(new Point(mainPoint.x + 40, mainPoint.y + 40));
		threeVsThreeFar.enemySpawnLocs.add(new Point(mainPoint.x, mainPoint.y + 40));

		threeVsThreeClose.playerSpawnLocs.add(new Point(mainPoint.x, mainPoint.y));
		threeVsThreeClose.playerSpawnLocs.add(new Point(mainPoint.x, mainPoint.y - 40));
		threeVsThreeClose.playerSpawnLocs.add(new Point(mainPoint.x - 40, mainPoint.y));
		threeVsThreeClose.enemySpawnLocs.add(new Point(mainPoint.x + 40, mainPoint.y));
		threeVsThreeClose.enemySpawnLocs.add(new Point(mainPoint.x + 40, mainPoint.y + 40));
		threeVsThreeClose.enemySpawnLocs.add(new Point(mainPoint.x, mainPoint.y + 40));
	}

	public void setup(CombatScenario scenario) {
		// Clear all units in the world

		if (scenario.playerSpawnLocs.isEmpty() && scenario.enemySpawnLocs.isEmpty()) {
			logger.debug("Scenario loaded contains no player or enemy units.");
			return;
		}

		List<Unit> players = new ArrayList<Unit>();
		for (var p : scenario.playerSpawnLocs) {
			var unit = new Unit(p.x, p.y, Unit.Side.PLAYER, world);
			unit.setName("Player");
			loadHumanBaseStats(unit.stats);
			addPassiveSkills(players);
			players.add(unit);
			world.addUnit(unit);
		}
		setPlayerUnitSprites(players);

		for (var p : scenario.enemySpawnLocs) {
			var unit = new Unit(p.x, p.y, Unit.Side.ENEMY, world);
			unit.setName("Goblin");
			loadEnemyUnitStats(unit.stats);
			world.addUnit(unit);
		}
	}

	private void setPlayerUnitSprites(List<Unit> players) {
		AnimatedSprite[] sprites = new AnimatedSprite[3];
		Animations animations = world.getAnimations();

		sprites[0] = animations.get("pipo-charachip001b.png");
		sprites[1] = animations.get("pipo-charachip017c.png");
		sprites[2] = animations.get("pipo-charachip028d.png");

		for (int i = 0; i < players.size(); i++) {
			players.get(i).graphics.setAnimatedSprite(sprites[i % 3]);
		}
	}

	private void addPassiveSkills(List<Unit> players) {
		for (int i = 0; i < players.size(); i++) {
			if (i % 3 == 0 || i % 3 == 1) {
				Unit u = players.get(i);
				u.skills.addSkill(PassiveSkills.basicCombatTraining);
			}
		}
	}

	private void loadEnemyUnitStats(UnitStats unit) {
		unit.base.maxHp = 100;
		unit.base.maxSp = 50;

		unit.base.damage = 5;
		unit.base.atk = 0;
		unit.base.def = 5;
		unit.base.force = 3;
		unit.base.stab = 3;
	}

	private void loadHumanBaseStats(UnitStats unit) {
		unit.base.maxHp = 100;
		unit.base.maxSp = 100;
		unit.base.damage = 2;

		unit.base.atk = 3;
		unit.base.def = 4;
		unit.base.force = 3;
		unit.base.stab = 3;
	}

	public static class CombatScenario {
		private final List<Point> playerSpawnLocs = new ArrayList<Point>();
		private final List<Point> enemySpawnLocs = new ArrayList<Point>();
	}
}
