package com.mygdx.holowyth.util.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.ai.AIModule;
import com.mygdx.holowyth.combatDemo.Controls;
import com.mygdx.holowyth.combatDemo.World;
import com.mygdx.holowyth.combatDemo.prototyping.CombatPrototyping;
import com.mygdx.holowyth.combatDemo.rendering.Renderer;
import com.mygdx.holowyth.combatDemo.ui.GameScreenBaseUI;
import com.mygdx.holowyth.combatDemo.ui.GameLog;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.sprite.Animations;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.tools.FunctionBindings;
import com.mygdx.holowyth.util.tools.Timer;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;

/**
 * Offers the common functionality for playing one or more levels.
 * Has all the modules to run a game. Includes the normal game UI, sub-classes can add more.
 * 
 * Has no mandatory concept of victory/defeat, and no interface. The calling screen just passes off control to GameScreen, and it does whatever it wants.
 * 
 * @author Colin
 *
 */
public abstract class GameScreenBase extends MapLoadingScreen {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// Game Modules
	protected PathingModule pathingModule;
	protected AIModule ai;

	protected Controls controls;
	protected World world;

	// Graphical Modules
	protected Renderer renderer;
	protected Animations animations;

	protected EffectsHandler gfx; // keeps track of vfx effects

	/**
	 * This is the base UI that appears in the game screen, sub-classes can add
	 * stuff on top.
	 */
	private GameScreenBaseUI gameUI;

	// Debugging and Convenience

	private DebugStore debugStore = new DebugStore();
	protected final FunctionBindings functionBindings = new FunctionBindings();

	protected final InputMultiplexer multiplexer = new InputMultiplexer();
	/**
	 * For running game at constant FPS
	 */
	private Timer timer = new Timer();

	// ----- Variables ----- //
	private Color backgroundColor = HoloGL.rgb(79, 121, 66); // HoloGL.rbg(255, 236, 179);
	private boolean mouseScrollEnabled = false;

	protected GameScreenBase(Holowyth game) {
		super(game);

		skin = game.skin;
		initializeAppLifetimeComponents();

		// Configure Input
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(this);

		// Load map and test units

		functionBindings.bindFunctionToKey(() -> gameUI.setVisibleDebugValues(!gameUI.isDebugValuesVisible()), Keys.GRAVE); // tilde key
		functionBindings.bindFunctionToKey(() -> {
			for (Unit unit : controls.getSelectedUnits()) {
				unit.stats.printInfo();
			}
		}, Keys.W); // print info on all selected units
		functionBindings.bindFunctionToKey(() -> {
			for (Unit unit : controls.getSelectedUnits()) {
				unit.stats.printInfo(true);
			}
		}, Keys.E); // print info+equipment
		functionBindings.bindFunctionToKey(() -> {
			if (isGamePaused()) {
				unpauseGame();
			} else {
				pauseGame();
			}
		}, Keys.SPACE);

		functionBindings.bindFunctionToKey(() -> {
			mouseScrollEnabled = !mouseScrollEnabled;
			getGameLog().addMessage(mouseScrollEnabled ? "Mouse scroll enabled" : "Mouse scroll disabled");
		}, Keys.H);

		functionBindings.bindFunctionToKey(() -> {
			gameUI.getStatsPanelUI().toggleDetailedView();
		}, Keys.V);
	}
	
	@Override
	public void render(float delta) {
		stage.act();
		handleMousePanning(delta);
		renderer.render(delta);

		updateTitleBarInformation();

		ifTimeElapsedTickGame();
		ai.update(delta);
		
		gameUI.render();
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

	private float snapLeftoverX;
	private float snapLeftoverY;

	/*
	 * Accumulate the leftovers and apply them to later movement, in order to
	 * prevent slow-down or inconsistencies due to repeated rounding.
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
		world.tick();
		gfx.tick();
	}

	public Controls getControls() {
		return controls;
	}

	public void pauseGame() {
		if (!gamePaused) {
			gamePaused = true;
			getGameLog().addMessage("Game Paused");
		}
	}

	/**
	 * If game is already running, has no effect
	 */
	public void unpauseGame() {
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
	 * Creates map-lifetime components and sets up application-lifetime components.
	 * <br>
	 * The map in question is {@link #map} <br>
	 * Call after loading a new map. The mirror function is mapShutdown.
	 */
	@Override
	protected void mapStartup() {
		initializeMapLifetimeComponents();
		
		gameUI.onMapStartup();
	}

	@Override
	protected void mapShutdown() {
		System.out.println("mapShutdown called");
		gameUI.onMapShutdown();
	}

	private void initializeAppLifetimeComponents() {
		pathingModule = new PathingModule(camera, shapeRenderer);

		renderer = new Renderer(game, camera, stage, pathingModule);
		renderer.setClearColor(backgroundColor);

		animations = new Animations();

		ai = new AIModule();

		gameUI = new GameScreenBaseUI(stage, debugStore, skin, this);
	}

	private void initializeMapLifetimeComponents() {

		final int mapWidth = (Integer) map.getProperties().get("widthPixels");
		final int mapHeight = (Integer) map.getProperties().get("heightPixels");

		// Init Pathing
		pathingModule.initForTiledMap(map, mapWidth, mapHeight);

		// Init World

		gfx = new EffectsHandler(game.batch, camera, stage, skin, debugStore);

		world = new World(mapWidth, mapHeight, pathingModule, debugStore, gfx, animations);

		// Init Unit controls
		if (controls != null) {
			multiplexer.removeProcessor(controls);
		}
		controls = new Controls(game, camera, fixedCam, world.getUnits(), debugStore, world, gameUI.getGameLog());
		multiplexer.addProcessor(controls);

		// Set Renderer to render world and other map-lifetime components
		renderer.setWorld(world);
		renderer.setTiledMap(map, mapWidth, mapHeight);
		renderer.setUnitControls(controls);
		renderer.setEffectsHandler(gfx);

	}

	public GameLog getGameLog() {
		return gameUI.getGameLog();
	}
	
	@Override
	public boolean keyDown(int keycode) {
		functionBindings.runBoundFunction(keycode);
		return false;
	}
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		gameUI.updateMouseCoordLabel(screenX, screenY, camera);
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		gameUI.updateMouseCoordLabel(screenX, screenY, camera);
		return false;
	}

}
