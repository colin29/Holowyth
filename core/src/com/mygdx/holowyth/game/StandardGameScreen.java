package com.mygdx.holowyth.game;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.game.base.GameScreen;
import com.mygdx.holowyth.game.session.SessionData;
import com.mygdx.holowyth.game.ui.InventoryDisplay;
import com.mygdx.holowyth.game.ui.PartyUnitSelectionPanel;
import com.mygdx.holowyth.game.ui.WornEquipsDisplay;
import com.mygdx.holowyth.gamedata.items.Weapons;
import com.mygdx.holowyth.gamedata.skillsandeffects.PassiveSkills;
import com.mygdx.holowyth.gamedata.units.MonsterStats;
import com.mygdx.holowyth.gamedata.units.Players;
import com.mygdx.holowyth.graphics.GifDecoder;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.skill.skill.Skills;
import com.mygdx.holowyth.town.TownScreen;
import com.mygdx.holowyth.town.model.Town;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.tools.debugstore.DebugValues;
import com.mygdx.holowyth.vn.VNController;
import com.mygdx.holowyth.world.map.Entrance;
import com.mygdx.holowyth.world.map.Entrance.Destination;
import com.mygdx.holowyth.world.map.Entrance.MapDestination;
import com.mygdx.holowyth.world.map.Entrance.TownDestination;
import com.mygdx.holowyth.world.map.Location;
import com.mygdx.holowyth.world.map.UnitMarker;
import com.mygdx.holowyth.world.map.trigger.Trigger;

/**
 * 
 * The standard game screen. Populates the level from GameMap and manages UI for normal gameplay (as
 * opposed to a demo which does its own thing)
 * 
 * A StandardGameScreen instance correspond to a single session. If the player loads a different
 * save file, a new screen will be made.
 *
 */
public class StandardGameScreen extends GameScreen {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private VNController vn;

	private Unit lecia;

	private final @NonNull SessionData session = new SessionData();

	private @NonNull PartyUnitSelectionPanel partyPanel;

	public StandardGameScreen(Holowyth game) {
		super(game);
//		vn = new VNController(new Stage(), batch, fixedCamera, multiplexer); // have vn draw using its OWN stage
//		startConversation("myConv.conv", "default");

		functionBindings.bindFunctionToKey(this::addLeciaToMapInstance, Keys.Z);
		functionBindings.bindFunctionToKey(this::removeLeciaFromMapInstance, Keys.X);
		functionBindings.bindFunctionToKey(() -> {
//			goToMap("forest2", "entrance_1");
			goToTown("testTown");
		}, Keys.G);

		functionBindings.bindFunctionToKey(() -> { // center camera back on map
			camera.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0);
		}, Keys.MINUS);

		DebugValues debugValues = debugStore.registerComponent(this.getClass().getSimpleName());
		debugValues.add("Map name", () -> map != null ? map.getName() : "No map loaded");

		loadGameMapByName("forest1");

		var spawnPos = map.getLocation("default_spawn_location").pos;
//		session.playerUnits.addAll(testSpawnMultipleLecias(3, map.getLocation("default_spawn_location").pos));
		session.playerUnits.addAll(spawnThreeMemberParty(spawnPos));

		lecia = session.playerUnits.get(0);
		for (Unit u : session.playerUnits) {
			u.setInventory(session.ownedItems);
		}
		var inv = new InventoryDisplay(stage, skin, session.ownedItems, assets);
		partyPanel = makePartyPanel(inv);
		addTestWeaponsToInventory();

		testInitEffect();
	}

	Animation<TextureRegion> darkSpike;

	private void testInitEffect() {
		darkSpike = new Animation<TextureRegion>(0.10f, HoloGL.getKeyFrames("img/effects/dark_spike.png", 192, 192));
	}

	private void addTestWeaponsToInventory() {
		session.ownedItems.addItem(Weapons.spear.cloneObject());
		session.ownedItems.addItem(Weapons.club.cloneObject());
		session.ownedItems.addItem(Weapons.dagger.cloneObject());
		session.ownedCurrency.add(50);
	}

	private @NonNull PartyUnitSelectionPanel makePartyPanel(@NonNull InventoryDisplay inv) {
		var partyPanel = new PartyUnitSelectionPanel(session.playerUnits, inv, stage, multiplexer, skin, assets);
		addTestWeaponsToInventory();
		return partyPanel;
	}

	/**
	 * GameScreenBase does check triggers, but it doesn't load them from the map
	 */
	private void loadMapTriggers() {
		for (Trigger t : map.getTriggers()) {
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
			mapInstance.addUnit(m);
		}
	}

	@SuppressWarnings("unused")
	private Unit spawnPlayerAtDefaultLocation() {
		Point pos = map.getLocations().get("default_spawn_location").pos;
		if (pos != null) {
			return testSpawnLecia(pos);
		} else {
			logger.error("Couldn't spawn player, map has no default_spawn_location");
			return null;
		}
	}

	public void goToMap(@NonNull String mapName, String locationName) {
		if (isMapLoaded()) {
			for (UnitInfo unit : session.playerUnits) {
				mapInstance.removeAndDetachUnitFromWorld((Unit) unit);
			}
		}
		loadGameMapByName(mapName);
		// map refers to new map now
		Location arrivalLoc = map.getLocation(locationName);
		placeUnits(arrivalLoc.pos, session.playerUnits);
		// Center camera
		camera.position.set(arrivalLoc.getX(), arrivalLoc.getY(), 0);
		if (arrivalLoc instanceof Entrance)
			((Entrance) arrivalLoc).disableTemporarily();
	}

	public void goToTown(String townName) {
		mapShutdown();
		Town town = game.world.getNewTownInstance(townName);
		var townScreen = new TownScreen(game, session, this);
		townScreen.loadTown(town);
		game.setScreen(townScreen);
	}

	private List<Unit> placeUnits(Point spawnPos, List<@NonNull Unit> units) {
		final List<Point> placements = pathingModule.findPathablePlacements(spawnPos, units.size(),
				mapInstance.getUnits());
		if (placements.size() < units.size())
			logger.warn("Tried to place {} units but only room for {} could be found", units.size(), placements.size());
		for (int i = 0; i < placements.size(); i++) {
			Unit u = units.get(i);
			Point placement = placements.get(i);
			u.setPos(placement.x, placement.y);
			mapInstance.addPreExistingUnit(units.get(i));
		}

		return null;
	}

	/**
	 * @return a list of units that were actually spawned
	 */
	@SuppressWarnings("unused")
	private List<@NonNull Unit> testSpawnMultipleLecias(int numUnits, Point spawnPos) {
		// fetch locations
		final List<Point> unitPlacements = pathingModule.findPathablePlacements(spawnPos, numUnits,
				mapInstance.getUnits());
		if (unitPlacements.size() != numUnits) {
			logger.warn("Expected {} placements, but got {} locations. Not all units may be placed.", numUnits,
					unitPlacements.size());
		}

		final List<@NonNull Unit> units = new ArrayList<@NonNull Unit>();

		for (int i = 0; i < Math.min(numUnits, unitPlacements.size()); i++) {
			units.add(testSpawnLecia(unitPlacements.get(i)));
		}
		return units;
	}

	@SuppressWarnings("null")
	private List<@NonNull Unit> spawnThreeMemberParty(Point spawnPos) {
		// fetch locations
		final List<Point> unitPlacements = pathingModule.findPathablePlacements(spawnPos, 3, mapInstance.getUnits());
		if (unitPlacements.size() != 3) {
			logger.warn("Expected {} placements, but got {} locations. Not all units may be placed.", 3,
					unitPlacements.size());
		}
		final List<@NonNull Unit> units = new ArrayList<@NonNull Unit>();

//		for (int i = 0; i < Math.min(numUnits, unitPlacements.size()); i++) {
//			units.add(testSpawnLecia(unitPlacements.get(i)));
//		}
		units.add(mapInstance.addUnit(Players.lecia, unitPlacements.get(0)));
		units.add(mapInstance.addUnit(Players.sonia, unitPlacements.get(1)));
		units.add(mapInstance.addUnit(Players.elvin, unitPlacements.get(2)));

		return units;
	}

	/**
	 * @param pos can't be null
	 */
	private @NonNull Unit testSpawnLecia(Point pos) {
		var u = new Unit(pos.x, pos.y, Unit.Side.PLAYER, mapInstance);
		u.setName("Lecia");
		u.graphics.setAnimatedSprite(game.animatedSprites.get("pipo-charachip030e.png"));

		u.stats.base.set(MonsterStats.baseHuman);
		u.stats.self.skills.addSkill(PassiveSkills.basicCombatTraining);
		u.skills.slotSkills(Skills.warriorSkills);
		u.equip.equip(Weapons.longSword.cloneObject());
		mapInstance.addUnit(u);
		return u;
	}

	private void removeLeciaFromMapInstance() {
		if (!isMapLoaded()) { // in case map was closed beforehand, try removing using her world ref.
			if (lecia.getMapInstance() != null) {
				lecia.getMapInstanceMutable().removeAndDetachUnitFromWorld(lecia);
			} else {
				logger.warn("Remove: Lecia is not in a world");
				return;
			}
		} else {
			controls.removeUnitFromSelection(lecia);
			mapInstance.removeAndDetachUnitFromWorld(lecia);
		}

	}

	private void addLeciaToMapInstance() {

		@NonNull
		Unit lecia; // shadow with nonNull reference
		if (this.lecia != null) {
			lecia = this.lecia;
		} else {
			return;
		}

		if (lecia != null && isMapLoaded()) {
			if (lecia.getMapInstance() == null) {
				Point pos = map.getLocations().get("default_spawn_location").pos;
				((Unit) lecia).x = pos.x + 200;
				((Unit) lecia).y = pos.y;
				mapInstance.addPreExistingUnit(lecia);
			} else {
				logger.warn("Add: Lecia already has a world");
			}
		} else {
			logger.info("Map isn't loaded, can't add player to world");
		}

	}

	private void tickEntrances() {
		for (Entrance entrance : map.getEntrances()) {
			entrance.tick();
		}
	}

	private void transportPlayerUnitsIfStandingOnEntrance() {
		for (Entrance entrance : map.getEntrances()) {
			if (entrance.isBeingTriggered(session.playerUnits)) {

				@NonNull
				Destination dest;
				if (entrance.dest != null) {
					dest = entrance.dest;
				} else {
					continue;
				}

				if (dest instanceof MapDestination) {
					var mapDest = (MapDestination) dest;
					goToMap(mapDest.map, mapDest.loc);
					return;
				} else if (dest instanceof TownDestination) {
					var townDest = (TownDestination) dest;
					goToTown(townDest.town);
					return;
				}
			}
		}
	}

	@Override
	protected void tickGame() {
		super.tickGame();
		if (isMapLoaded()) {
			tickEntrances();
			transportPlayerUnitsIfStandingOnEntrance();
		}
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		if (vn != null)
			vn.updateAndRenderIfVisible(delta); // render vn ui on top

		batch.begin();
		batch.draw(darkSpike.getKeyFrame(timer.getTimeElapsedSeconds(), true), 200, 200);
		batch.end();
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(multiplexer);
		logger.debug("Showed Screen");
	}

	@Override
	public void hide() {
		super.hide();
		partyPanel.onScreenHide();
	}

	@Override
	public final void mapStartup() {
		super.mapStartup();

		placeUnitsAccordingToUnitMarkers();
		loadMapTriggers();
	}

	@Override
	public final void mapShutdown() {
		super.mapShutdown();
	}

	@Override
	public void resize(int width, int height) {
		if (vn != null)
			vn.resize(width, height);
	}

}
