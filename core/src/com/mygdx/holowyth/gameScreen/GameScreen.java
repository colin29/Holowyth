package com.mygdx.holowyth.gameScreen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.combatDemo.prototyping.Equips;
import com.mygdx.holowyth.skill.skill.Skills;
import com.mygdx.holowyth.skill.skillsandeffects.PassiveSkills;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.units.MonsterStats;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * Game screen creates the level according to the GameMap (other demos like
 * CombatDemo sorta do their own thing)
 *
 */
public class GameScreen extends GameScreenBase {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public GameScreen(Holowyth game) {
		super(game);
		loadGameMapByName("foo");
		spawnPlayerAtDefaultLocation();
	}

	private void spawnPlayerAtDefaultLocation() {
		Point pos = map.getLocations().get("default_spawn_location");
		if(pos != null) {
			spawnPlayerUnit(pos);
		}else {
			logger.error("Couldn't spawn player, map has no default_spawn_location");
		}
	}

	/**
	 * Pos can't be null
	 */
	private void spawnPlayerUnit(Point pos) {
		var u = new Unit(pos.x, pos.y, Unit.Side.PLAYER, world);
		u.setName("Lecia");
		u.graphics.setAnimatedSprite(animations.get("pipo-charachip030e.png"));

		u.stats.base.set(MonsterStats.baseHuman);
		u.stats.self.skills.addSkill(PassiveSkills.basicCombatTraining);
		u.skills.slotSkills(Skills.warriorSkills);
		u.equip.equip(Equips.longSword.copy());
		world.addUnit(u);
	}
	
	@Override
	public void show() {
		Gdx.input.setInputProcessor(multiplexer);
	}
	
	@Override
	protected final void mapStartup() {
		super.mapStartup();
	}

	@Override
	protected final void mapShutdown() {
		super.mapShutdown();
	}
}
