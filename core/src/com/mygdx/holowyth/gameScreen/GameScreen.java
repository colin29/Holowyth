package com.mygdx.holowyth.gameScreen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.gameScreen.combatDemo.prototyping.Equips;
import com.mygdx.holowyth.map.UnitMarker;
import com.mygdx.holowyth.skill.skill.Skills;
import com.mygdx.holowyth.skill.skillsandeffects.PassiveSkills;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.units.MonsterStats;
import com.mygdx.holowyth.util.MiscUtil;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.vn.VNController;

/**
 * 
 * The standard game screen. Populates the level from GameMap and manages UI for normal gameplay (as
 * opposed to a demo which does its own thing)
 *
 */
public class GameScreen extends GameScreenBase {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private VNController vn;

	Texture lecia;
	
	public GameScreen(Holowyth game) {
		super(game);
		loadGameMapByName("foo");
		spawnPlayerAtDefaultLocation();

		placeUnitsAccordingToUnitMarkers();

//		vn = new VNController(new Stage(), batch, fixedCamera, multiplexer); // have vn draw using its OWN stage
//		startConversation("myConv.conv", "default");
		
		
		
	}
	
	private void startConversation(String convoName, String branch) {
		pauseGame();
		vn.setConvoExitListener(() -> {
			vn.hide();
			resumeGame();
		});
		vn.startConversation("myConv.conv", "default");
		vn.show();
	}

	private void placeUnitsAccordingToUnitMarkers() {
		for (UnitMarker m : map.getUnitMarkers()) {
			world.addUnit(m);
		}
	}

	private void spawnPlayerAtDefaultLocation() {
		Point pos = map.getLocations().get("default_spawn_location");
		if (pos != null) {
			spawnPlayerUnit(pos);
		} else {
			logger.error("Couldn't spawn player, map has no default_spawn_location");
		}
	}

	/**
	 * @param pos can't be null
	 */
	private void spawnPlayerUnit(Point pos) {
		var u = new Unit(pos.x, pos.y, Unit.Side.PLAYER, world);
		u.setName("Lecia");
		u.graphics.setAnimatedSprite(game.animations.get("pipo-charachip030e.png"));

		u.stats.base.set(MonsterStats.baseHuman);
		u.stats.self.skills.addSkill(PassiveSkills.basicCombatTraining);
		u.skills.slotSkills(Skills.warriorSkills);
		u.equip.equip(Equips.longSword.copy());
		world.addUnit(u);
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		if(vn!=null)
			vn.updateAndRenderIfVisible(delta); // render vn ui on top
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

	@Override
	public void resize(int width, int height) {
		if(vn!=null)
			vn.resize(width, height);
	}
}
