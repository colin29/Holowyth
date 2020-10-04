package com.mygdx.holowyth.game.base;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.ai.AIModule;
import com.mygdx.holowyth.game.Controls;
import com.mygdx.holowyth.game.MapInstance;
import com.mygdx.holowyth.game.rendering.GameScreenRenderer;
import com.mygdx.holowyth.game.ui.GameLogDisplay;
import com.mygdx.holowyth.game.ui.GameScreenUI;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.tools.FunctionBindings;
import com.mygdx.holowyth.util.tools.Timer;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;
import com.mygdx.holowyth.world.map.trigger.TriggersHandler;

/**
 * Offers the common functionality for playing one or more levels. Has all the modules to run a
 * game. Includes the normal game UI, sub-classes can add more.
 * 
 * Has no mandatory concept of victory/defeat, and no interface. The calling screen just passes off
 * control to GameScreen, and it does whatever it wants.
 * 
 * @author Colin
 *
 */
public abstract class GameScreen extends MapLoadingScreen {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// App life-time components
	protected PathingModule pathingModule;
	protected GameScreenRenderer renderer;
	protected AIModule ai;
	private GameScreenUI ui;

	// Map life-time components
	protected MapInstance mapInstance;
	protected Controls controls;
	protected EffectsHandler gfx;
	protected TriggersHandler triggers;

	// Debugging and Convenience (App-lifetime)
	protected final DebugStore debugStore = new DebugStore();
	protected final FunctionBindings functionBindings = new FunctionBindings();
	
	/**
	 * For running game at constant FPS
	 */
	protected final Timer timer = new Timer();

	/**
	 * For camera panning
	 */
	private float snapLeftoverX;
	private float snapLeftoverY;

	protected final @NonNull InputMultiplexer multiplexer = new InputMultiplexer();
	

	// Settings
	private Color backgroundColor = HoloGL.rgb(79, 121, 66); // HoloGL.rbg(255, 236, 179);
	private boolean mouseScrollEnabled = Holo.mouseScrollEnabled;

	protected GameScreen(Holowyth game) {
		super(game);
		skin = game.skin;

		initializeAppLifetimeComponents();
		setupInputForThisAndAppLifetimeComponents();

		addGameBindings();
		addUIBindings();
		addDebugBindings();
	}

	private void setupInputForThisAndAppLifetimeComponents() {
		multiplexer.addProcessor(ui);
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(this);
	}

	/**
	 * Keyboard bindings that relate to the game, that aren't implemented elsewhere
	 */
	private void addGameBindings() {
		functionBindings.bindFunctionToKey(this::toggleGamePaused, Keys.SPACE);
		functionBindings.bindFunctionToKey(this::toggleMouseScroll, Keys.H);
	}

	/**
	 * Keyboard bindings that control the UI
	 */
	private void addUIBindings() {
		functionBindings.bindFunctionToKey(ui::toggleStatsPanelDetailedView, Keys.V);
		functionBindings.bindFunctionToKey(() -> ui.setVisibleDebugValues(!ui.isDebugValuesVisible()), Keys.GRAVE); // tilde
																													// key
	}

	/**
	 * Keyboard bindings for logging info for debug purposes
	 */
	private void addDebugBindings() {
		functionBindings.bindFunctionToKey(this::debugPrintSelectedUnitsInfo, Keys.W);
	}

	private void toggleGamePaused() {
		if (isGamePaused()) {
			resumeGame();
		} else {
			pauseGame();
		}
	}

	private void toggleMouseScroll() {
		mouseScrollEnabled = !mouseScrollEnabled;
		getGameLog().addMessage(mouseScrollEnabled ? "Mouse scroll enabled" : "Mouse scroll disabled");
	}

	private void debugPrintSelectedUnitsInfo() {
		for (Unit unit : controls.getSelectedUnits())
			unit.stats.printInfo();
	}

	@Override
	public void render(float delta) {
		stage.act(delta);
		ai.update(delta);
		ifTimeElapsedTickGame();

		handleMousePanning(delta);
		
		renderer.render(delta);
		ui.render();
	}

	/**
	 * Pan the view if the mouse is near the edge of the screen
	 */
	private void handleMousePanning(float delta) {

		final int mouseX = Gdx.input.getX();
		final int mouseY = Gdx.input.getY();

		final int screenHeight = Gdx.graphics.getHeight();
		final int screenWidth = Gdx.graphics.getWidth();

		final float scrollMargin = 40f;
		final float scrollSpeed = 300 * delta; // do X pixels per second

		if (mouseScrollEnabled && mouseY > screenHeight - scrollMargin || Gdx.input.isKeyPressed(Keys.DOWN))
			camera.translate(0, -scrollSpeed + snapLeftoverY);
		if (mouseScrollEnabled && mouseY < scrollMargin || Gdx.input.isKeyPressed(Keys.UP))
			camera.translate(0, scrollSpeed + snapLeftoverY);

		if (mouseScrollEnabled && mouseX > screenWidth - scrollMargin || Gdx.input.isKeyPressed(Keys.RIGHT))
			camera.translate(scrollSpeed + snapLeftoverX, 0);
		if (mouseScrollEnabled && mouseX < scrollMargin || Gdx.input.isKeyPressed(Keys.LEFT))
			camera.translate(-scrollSpeed + snapLeftoverX, 0);

		snapLeftoverX = 0;
		snapLeftoverY = 0;

		snapCameraAndSaveRemainder();
	}

	/*
	 * Accumulate the leftovers and apply them to later movement, in order to prevent slow-down or
	 * inconsistencies due to repeated rounding.
	 */
	private void snapCameraAndSaveRemainder() {

		float dx = Math.round(camera.position.x) - camera.position.x;
		float dy = Math.round(camera.position.y) - camera.position.y;

		camera.position.set(Math.round(camera.position.x), Math.round(camera.position.y), 0);

		snapLeftoverX = -1 * dx;
		snapLeftoverY = -1 * dy;
	}

	private boolean gamePaused = false;

	private void ifTimeElapsedTickGame() {
		timer.start(1000 / Holo.GAME_FPS);
		if (timer.taskReady() && !gamePaused) {
			tickGame();
		}
	}

	protected void tickGame() {
		if(isMapLoaded()) {
			mapInstance.tick();
			controls.tick();	
			gfx.tick();
			triggers.checkTriggers();
		}
	}

	public Controls getControls() {
		return controls;
	}

	/**
	 * Not to be confused with pause() which is a libgdx app method
	 */
	public void pauseGame() {
		if (!gamePaused) {
			gamePaused = true;
			getGameLog().addMessage("Game Paused");
		}
	}

	/**
	 * Unpauses the game. If game is already running, has no effect
	 */
	public void resumeGame() {
		if (gamePaused) {
			gamePaused = false;
			getGameLog().addMessage("Game Unpaused");
		}
	}

	public boolean isGamePaused() {
		return gamePaused;
	}

	/**
	 * Initializes neccesary game components. <br>
	 * Creates map-lifetime components and sets up application-lifetime components. <br>
	 * The map in question is {@link #map} <br>
	 * Call after loading a new map. The mirror function is mapShutdown.
	 */
	@Override
	public void mapStartup() {
		final int mapWidth = (Integer) map.getTilemap().getProperties().get("widthPixels");
		final int mapHeight = (Integer) map.getTilemap().getProperties().get("heightPixels");
		
		initializeMapLifetimeComponents(mapWidth, mapHeight);
		addInputForMapLifeTimeComponents();
		
		setupAppLifetimeComponentsForNewMap(mapWidth, mapHeight);
		logger.info("Map '{}' loaded", map.getName());
	}

	@Override
	public void mapShutdown() {
		if(!isMapLoaded()) {
			logger.info("mapShutdown: no map loaded");
			return;
		}
		logger.info("Shutting down map '{}'", map.getName());

		map = null;
		
		removeInputForMapLifeTimeComponents();
		
		// Set all map-lifetime components to null
		mapInstance = null;
		controls = null;
		gfx = null;
		triggers = null;

		// Have all app-lifetime components clear map-specific data.
		pathingModule.onMapClose();
		renderer.onMapClose();
		ai.onMapClose();
		ui.onMapClose();
	}

	private void initializeAppLifetimeComponents() {
		pathingModule = new PathingModule();

		renderer = new GameScreenRenderer(game, camera, stage, pathingModule);
		renderer.setClearColor(backgroundColor);

		ai = new AIModule();

		ui = new GameScreenUI(stage, debugStore, skin, this);
	}
	
	private void initializeMapLifetimeComponents(final int mapWidth, final int mapHeight) {

		gfx = new EffectsHandler(game.batch, camera, stage, skin, debugStore);
		
		mapInstance = new MapInstance(pathingModule, debugStore, gfx, game.animatedSprites, assets);
		controls = new Controls(game, camera, fixedCamera, mapInstance.getUnits(), debugStore, mapInstance, ui.getGameLog());
		triggers = new TriggersHandler(mapInstance);
	}

	private void setupAppLifetimeComponentsForNewMap(final int mapWidth, final int mapHeight) {
		pathingModule.initForTiledMap(map.getTilemap(), mapWidth, mapHeight);
		
		renderer.setMap(map, mapWidth, mapHeight);
		renderer.setMapLifeTimeComponentsRefs(mapInstance, controls, gfx);
		
		ui.onMapStartup();
	}

	private void addInputForMapLifeTimeComponents() {
		multiplexer.addProcessor(controls);
	}
	private void removeInputForMapLifeTimeComponents() {
		multiplexer.removeProcessor(controls);
	}

	public GameLogDisplay getGameLog() {
		return ui.getGameLog();
	}

	@Override
	public boolean keyDown(int keycode) {
		functionBindings.runBoundFunction(keycode);
		return false;
	}

}
