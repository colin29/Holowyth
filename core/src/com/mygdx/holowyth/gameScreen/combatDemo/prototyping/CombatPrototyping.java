package com.mygdx.holowyth.gameScreen.combatDemo.prototyping;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.gameScreen.Controls;
import com.mygdx.holowyth.gameScreen.World;
import com.mygdx.holowyth.skill.skill.Skills;
import com.mygdx.holowyth.skill.skillsandeffects.PassiveSkills;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.sprite.AnimatedSprite;
import com.mygdx.holowyth.unit.sprite.Animations;
import com.mygdx.holowyth.unit.units.MonsterStats;
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

	public CombatScenario scenario = threeVsThreeFar;

	public CombatPrototyping(World world, Controls controls) {
		this.world = world;
		defineScenarios();
	}

	/**
	 * Sets up the preset scenario (ie. by creating the units and adding them to the world)
	 */
	public void setupPlannedScenario() {
		setup(scenario);
	}

	private final Point mainPoint = new Point(700, 450);// new Point(397, 266);

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

		threeVsThreeFar.playerSpawnLocs.add(new Point(200, 240));
		threeVsThreeFar.playerSpawnLocs.add(new Point(200, 200));
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
			unit.stats.base.set(MonsterStats.baseHuman);
			players.add(unit);
			world.addUnit(unit);
		}

		setPlayerUnitSprites(players);
		setUpThreeUnitScenario(players);

		Animations animations = world.getAnimations();
		for (var p : scenario.enemySpawnLocs) {
			var unit = new Unit(p.x, p.y, Unit.Side.ENEMY, world);
			unit.setName("Goblin");
			unit.stats.base.set(MonsterStats.goblin);
			unit.skills.slotSkills(Skills.warriorSkills);
			unit.graphics.setAnimatedSprite(animations.get("goblin1.png"));
			world.addUnit(unit);
		}
	}

	private void setPlayerUnitSprites(List<Unit> players) {
		AnimatedSprite[] sprites = new AnimatedSprite[3];
		Animations animations = world.getAnimations();

		// default sprites
		sprites[0] = animations.get("pipo-charachip001b.png");
		sprites[1] = animations.get("pipo-charachip001b.png");
		sprites[2] = animations.get("pipo-charachip028d.png");

		for (int i = 0; i < players.size(); i++) {
			players.get(i).graphics.setAnimatedSprite(sprites[i % 3]);
		}
	}

	private void setUpThreeUnitScenario(List<Unit> players) {

		if (players.size() < 3) {
			return;
		}
		Animations animations = world.getAnimations();

		{
			var u = players.get(0);
			u.setName("Lecia");
			u.graphics.setAnimatedSprite(animations.get("pipo-charachip030e.png"));

			u.stats.self.skills.addSkill(PassiveSkills.basicCombatTraining);
			u.skills.addSkills(Skills.warriorSkills);
			u.skills.slotSkills(Skills.warriorSkills);
			u.equip.equip(Equips.longSword.copy());
		}
		{
			var u = players.get(1);
			u.setName("Elvin");
			u.graphics.setAnimatedSprite(animations.get("pipo-charachip001b.png"));

			u.skills.addSkill(PassiveSkills.basicCombatTraining);
			u.skills.addSkills(Skills.warriorSkills);
			u.skills.slotSkills(Skills.warriorSkills);
			u.stats.getEquip().equip(Equips.longSword.copy());
		}
		{
			var u = players.get(2);
			u.setName("Sonia");
			u.graphics.setAnimatedSprite(animations.get("pipo-charachip028d.png"));

			u.skills.addSkills(Skills.mageSkills);
			u.skills.slotSkills(
					Skills.mageSkills);
			u.stats.getEquip().equip(Equips.staff.copy());

		}
	}

	public static class CombatScenario {
		private final List<Point> playerSpawnLocs = new ArrayList<Point>();
		private final List<Point> enemySpawnLocs = new ArrayList<Point>();
	}
}
