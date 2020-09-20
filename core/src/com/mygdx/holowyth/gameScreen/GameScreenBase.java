package com.mygdx.holowyth.gameScreen;

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
import com.mygdx.holowyth.combatDemo.ui.GameBaseUI;
import com.mygdx.holowyth.combatDemo.ui.GameLogDisplay;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.tiled.GameMapLoadingScreen;
import com.mygdx.holowyth.tiled.TiledMapLoadingScreen;
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
public abstract class GameScreenBase extends GameMapLoadingScreen {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// Game Modules
	protected PathingModule pathingModule;
	protected AIModule ai;

	protected Controls controls;
	protected World world;

	// Graphical Modules
	protected Renderer renderer;
	protected Animations animations;
	
	protected EffectsHandler gfx;
	
	/**
	 * The base UI that appears while playing the game
	 */
	private GameBaseUI ui;

	// Debugging and Convenience
	private final DebugStore debugStore = new DebugStore();
	protected final FunctionBindings functionBindings = new FunctionBindings();
	
	/**
	 * For running game at constant FPS
	 */
	private Timer timer = new Timer();
	
	
	/**
	 *  For camera panning
	 */
	private float snapLeftoverX;
	private float snapLeftoverY;
	
	protected final InputMultiplexer multiplexer = new InputMultiplexer();

	// Settings
	private Color backgroundColor = HoloGL.rgb(79, 121, 66); // HoloGL.rbg(255, 236, 179);
	private boolean mouseScrollEnabled = false;

	protected GameScreenBase(Holowyth game) {
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
		functionBindings.bindFunctionToKey(() -> ui.setVisibleDebugValues(!ui.isDebugValuesVisible()), Keys.GRAVE); // tilde key
	}
	/**
	 * Keyboard bindings for logging info for debug purposes
	 */
	private void addDebugBindings() {
		functionBindings.bindFunctionToKey(this::debugPrintSelectedUnitsInfo, Keys.W);
	}
	
	private void toggleGamePaused() {
		if (isGamePaused()) {
			unpauseGame();
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
		stage.act();
		handleMousePanning(delta);
		renderer.render(delta);

		ifTimeElapsedTickGame();
		ai.update(delta);
		
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
		setupInputForMapLifeTimeComponents();
		
		ui.onMapStartup();
	}

	@Override
	protected void mapShutdown() {
		System.out.println("mapShutdown called");
		ui.onMapShutdown();
	}

	private void initializeAppLifetimeComponents() {
		pathingModule = new PathingModule(camera, shapeRenderer);

		renderer = new Renderer(game, camera, stage, pathingModule);
		renderer.setClearColor(backgroundColor);

		animations = new Animations();

		ai = new AIModule();

		ui = new GameBaseUI(stage, debugStore, skin, this);
	}

	private void initializeMapLifetimeComponents() {

		final int mapWidth = (Integer) map.getTilemap().getProperties().get("widthPixels");
		final int mapHeight = (Integer) map.getTilemap().getProperties().get("heightPixels");

		// Init Pathing
		pathingModule.initForTiledMap(map.getTilemap(), mapWidth, mapHeight);

		// Init World

		gfx = new EffectsHandler(game.batch, camera, stage, skin, debugStore);

		world = new World(mapWidth, mapHeight, pathingModule, debugStore, gfx, animations);

		// Init Unit controls
		controls = new Controls(game, camera, fixedCam, world.getUnits(), debugStore, world, ui.getGameLog());
		
		// Set Renderer to render world and other map-lifetime components
		renderer.setWorld(world);
		renderer.setTiledMap(map.getTilemap(), mapWidth, mapHeight);
		renderer.setUnitControls(controls);
		renderer.setEffectsHandler(gfx);

	}
	private void setupInputForMapLifeTimeComponents() {
		if (controls != null) {
			multiplexer.removeProcessor(controls);
		}
		multiplexer.addProcessor(controls);
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
