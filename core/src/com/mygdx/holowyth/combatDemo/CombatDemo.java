package com.mygdx.holowyth.combatDemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.combatDemo.rendering.Renderer;
import com.mygdx.holowyth.combatDemo.ui.CombatDemoUI;
import com.mygdx.holowyth.combatDemo.ui.GameLog;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.template.DemoScreen;
import com.mygdx.holowyth.util.tools.FunctionBindings;
import com.mygdx.holowyth.util.tools.Timer;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;

/**
 * The main class that runs the game.
 * 
 * Is responsible for setting up the all the other components.
 * 
 * @author Colin Ta
 *
 */
public class CombatDemo extends DemoScreen implements Screen, InputProcessor {

	// Rendering and pipeline variables
	Renderer renderer;

	// UI
	CombatDemoUI combatDemoUI;

	// Scene2D
	Skin skin;

	// Game Components
	Controls unitControls;
	PathingModule pathingModule;

	// Game state
	World world;

	// Graphical Components
	EffectsHandler gfx; // keeps track of vfx effects

	// Input
	private InputMultiplexer multiplexer = new InputMultiplexer();

	// Frame rate control
	Timer timer = new Timer();

	Color backgroundColor = HoloGL.rgb(79, 121, 66); // HoloGL.rbg(255, 236, 179);

	// For debugging and playtesting
	DebugStore debugStore = new DebugStore();
	private FunctionBindings functionBindings = new FunctionBindings();

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public CombatDemo(final Holowyth game) {
		super(game);

		skin = game.skin;
		shapeRenderer = game.shapeRenderer;

		initializeAppLifetimeComponents();

		combatDemoUI = new CombatDemoUI(stage, debugStore, skin, this);

		// Configure Input
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(this);

		// Load map and test units

		// loadMapFromDisk(Holo.mapsDirectory + Holo.editorInitialMap);
		loadMapFromDisk(Holo.mapsDirectory + "/forest1.tmx");

		Table debugInfo = combatDemoUI.getDebugInfo();
		functionBindings.bindFunctionToKey(() -> debugInfo.setVisible(!debugInfo.isVisible()), Keys.GRAVE); // tilde key
		functionBindings.bindFunctionToKey(() -> {
			for (Unit unit : unitControls.getSelectedUnits()) {
				unit.stats.printInfo();
			}
		}, Keys.W); // print info on all selected units
		functionBindings.bindFunctionToKey(() -> {
			goBreak = true;
		}, Keys.B); // break point
		functionBindings.bindFunctionToKey(() -> {
			if (isGamePaused()) {
				unpauseGame();
			} else {
				pauseGame();
			}
		}, Keys.Y);
	}

	public static boolean goBreak = false; // debug variable

	private void initializeAppLifetimeComponents() {
		pathingModule = new PathingModule(camera, shapeRenderer);

		renderer = new Renderer(game, camera, stage, pathingModule);
		renderer.setClearColor(backgroundColor);
	}

	@Override
	public void render(float delta) {
		stage.act();
		handleMousePanning(delta);
		renderer.render(delta);

		updateTitleBarInformation();

		combatDemoUI.onRender();

		ifTimeElapsedTickWorld();

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

		if (mouseY > screenHeight - scrollMargin)
			camera.translate(0, -scrollSpeed + snapLeftoverY);
		if (mouseY < scrollMargin)
			camera.translate(0, scrollSpeed + snapLeftoverY);

		if (mouseX > screenWidth - scrollMargin)
			camera.translate(scrollSpeed + snapLeftoverX, 0);
		if (mouseX < scrollMargin)
			camera.translate(-scrollSpeed + snapLeftoverX, 0);

		snapLeftoverX = 0;
		snapLeftoverY = 0;

		snapCameraAndSaveRemainder();
	}

	private float snapLeftoverX;
	private float snapLeftoverY;

	/*
	 * Accumulate the leftovers and apply them to later movement, in order to prevent slow-down or inconsistencies due to repeated rounding.
	 */
	private void snapCameraAndSaveRemainder() {

		float dx = Math.round(camera.position.x) - camera.position.x;
		float dy = Math.round(camera.position.y) - camera.position.y;

		camera.position.set(Math.round(camera.position.x), Math.round(camera.position.y), 0);

		snapLeftoverX = -1 * dx;
		snapLeftoverY = -1 * dy;
	}

	private boolean gamePaused = false;

	private void ifTimeElapsedTickWorld() {
		timer.start(1000 / Holo.GAME_FPS);
		if (timer.taskReady() && !gamePaused) {
			world.tick();
			gfx.tick();
		}
	}

	@Override
	public void show() {
		System.out.println("Showed Pathfinding Demo");
		Gdx.input.setInputProcessor(multiplexer);

		System.out.println("Gdx Version: " + com.badlogic.gdx.Version.VERSION);
	}

	@Override
	public void dispose() {
	}

	private CombatPrototyping testing;

	/**
	 * Initializes neccesary game components. <br>
	 * Creates map-lifetime components and sets up application-lifetime components. <br>
	 * The map in question is {@link #map} <br>
	 * Call after loading a new map. The mirror function is mapShutdown.
	 */
	@Override
	protected void mapStartup() {
		initializeMapLifetimeComponents();
		testing.setupPlannedScenario();
	}

	private void initializeMapLifetimeComponents() {

		final int mapWidth = (Integer) map.getProperties().get("widthPixels");
		final int mapHeight = (Integer) map.getProperties().get("heightPixels");

		// Init Pathing
		pathingModule.initForTiledMap(map, mapWidth, mapHeight);

		// Init World

		gfx = new EffectsHandler(game.batch, camera, stage, skin, debugStore);

		world = new World(mapWidth, mapHeight, pathingModule, debugStore, gfx);

		// Init Unit controls
		if (unitControls != null) {
			multiplexer.removeProcessor(unitControls);
		}
		unitControls = new Controls(game, camera, fixedCam, world.getUnits(), debugStore, world, combatDemoUI.getGameLog());
		multiplexer.addProcessor(unitControls);

		// Set Renderer to render world and other map-lifetime components
		renderer.setWorld(world);
		renderer.setTiledMap(map, mapWidth, mapHeight);
		renderer.setUnitControls(unitControls);
		renderer.setEffectsHandler(gfx);

		// UI
		combatDemoUI.onMapStartup();

		// Testing
		testing = new CombatPrototyping(world, unitControls);

	}

	@Override
	protected void mapShutdown() {
		System.out.println("mapShutdown called");
		combatDemoUI.onMapShutdown();
	}

	/* Input methods */

	@Override
	public boolean keyDown(int keycode) {
		functionBindings.runBoundFunction(keycode);
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		combatDemoUI.updateMouseCoordLabel(screenX, screenY, camera);
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		combatDemoUI.updateMouseCoordLabel(screenX, screenY, camera);
		return false;
	}

	public Controls getControls() {
		return unitControls;
	}

	private void pauseGame() {
		if (!gamePaused) {
			gamePaused = true;
			getGameLog().addMessage("Game Paused");
		}
	}

	/**
	 * If game is already running, has no effect
	 */
	private void unpauseGame() {
		if (gamePaused) {
			gamePaused = false;
			getGameLog().addMessage("Game Unpaused");
		}
	}

	private boolean isGamePaused() {
		return gamePaused;
	}

	public GameLog getGameLog() {
		return combatDemoUI.getGameLog();
	}

}
