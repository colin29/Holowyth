package com.mygdx.holowyth.gameScreen;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.gameScreen.combatDemo.prototyping.Equips;
import com.mygdx.holowyth.map.UnitMarker;
import com.mygdx.holowyth.map.trigger.Trigger;
import com.mygdx.holowyth.map.trigger.TriggerEvent;
import com.mygdx.holowyth.map.trigger.UnitEntersRegion;
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
public class StandardGameScreen extends GameScreen {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private VNController vn;


	private Unit lecia;
	
	public StandardGameScreen(Holowyth game) {
		super(game);
		loadGameMapByName("forest1");
		
//		vn = new VNController(new Stage(), batch, fixedCamera, multiplexer); // have vn draw using its OWN stage
//		startConversation("myConv.conv", "default");
		
		functionBindings.bindFunctionToKey(this::addLeciaToWorld, Keys.Z);
		functionBindings.bindFunctionToKey(this::removeLeciaFromWorld, Keys.X);
		
	}
	/**
	 * GameScreenBase does check triggers, but it doesn't load them from the map
	 */
	private void loadMapTriggers() {
		for(Trigger t: map.getTriggers()) {
			triggers.addTrigger(t);
		}
	}
	
	@SuppressWarnings("unused")
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

	private Unit spawnPlayerAtDefaultLocation() {
		Point pos = map.getLocations().get("default_spawn_location");
		if (pos != null) {
			return spawnPlayerUnit(pos);
		} else {
			logger.error("Couldn't spawn player, map has no default_spawn_location");
			return null;
		}
	}
	
	/**
	 * @return a list of units that were actually spawned
	 */
	private List<Unit> spawnMultiplePlayerUnits(Point spawnPos, int numUnits) {
		// fetch locations
		final List<Point> unitPlacements = pathingModule.findPathablePlacements(spawnPos, numUnits);
		if(unitPlacements.size() != numUnits) {
			logger.warn("Expected {} placements, but got {} locations. Not all units may be placed.", numUnits, unitPlacements.size());
		}

		final List<Unit> units = new ArrayList<Unit>();
		
		for(int i=0;i<Math.min(numUnits, unitPlacements.size());i++) {
			units.add(spawnPlayerUnit(unitPlacements.get(i)));
		}
		return units;
	}

	/**
	 * @param pos can't be null
	 */
	private Unit spawnPlayerUnit(Point pos) {
		var u = new Unit(pos.x, pos.y, Unit.Side.PLAYER, world);
		u.setName("Lecia");
		u.graphics.setAnimatedSprite(game.animations.get("pipo-charachip030e.png"));

		u.stats.base.set(MonsterStats.baseHuman);
		u.stats.self.skills.addSkill(PassiveSkills.basicCombatTraining);
		u.skills.slotSkills(Skills.warriorSkills);
		u.equip.equip(Equips.longSword.copy());
		world.addUnit(u);
		return u;
	}
	private void removeLeciaFromWorld() {

		
		if(!isMapLoaded()) { // in case map was closed beforehand, try removing using her world ref.
			if(lecia.getWorld() != null) {
				lecia.getWorldMutable().removeAndDetachUnitFromWorld(lecia);
			}else {
				logger.warn("Remove: Lecia is not in a world");
				return;
			}
		}else {
			controls.removeUnitFromSelection(lecia);
			world.removeAndDetachUnitFromWorld(lecia);
		}
		
		
	}
	private void addLeciaToWorld() {
		if(isMapLoaded()) {
			if(lecia.getWorld() == null) {
				Point pos = map.getLocations().get("default_spawn_location");
				((Unit) lecia).x = pos.x+200;
				((Unit) lecia).y = pos.y;
				world.addPreExistingUnit(lecia);	
			}else {
				logger.warn("Add: Lecia already has a world");
			}
		}else {
			logger.info("Map isn't loaded, can't add player to world");
		}
		
	}

	@Override
	protected void tickGame() {
		super.tickGame();
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
	public
	final void mapStartup() {
		super.mapStartup();
		spawnMultiplePlayerUnits(map.getLocation("default_spawn_location"), 6);
		if(lecia==null) {
//			lecia = spawnPlayerAtDefaultLocation();
	
		}else {
			// insert existing lecia into the world
		}
		placeUnitsAccordingToUnitMarkers();
		loadMapTriggers();
	}

	@Override
	public
	final void mapShutdown() {
		super.mapShutdown();
	}

	@Override
	public void resize(int width, int height) {
		if(vn!=null)
			vn.resize(width, height);
	}
}
