package com.mygdx.holowyth.combatDemo;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.ai.AIModule;
import com.mygdx.holowyth.combatDemo.prototyping.CombatPrototyping;
import com.mygdx.holowyth.combatDemo.rendering.Renderer;
import com.mygdx.holowyth.combatDemo.ui.CombatDemoUI;
import com.mygdx.holowyth.combatDemo.ui.GameLog;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.sprite.Animations;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloUI;
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

	Logger logger = LoggerFactory.getLogger(this.getClass());

	// Game Modules
	private PathingModule pathingModule;
	private AIModule ai;

	private Controls controls;
	private World world;
	private CombatPrototyping testing;

	// Graphical Modules
	private Renderer renderer;
	private Animations animations;
	private CombatDemoUI combatDemoUI;
	private EffectsHandler gfx; // keeps track of vfx effects

	// Debugging and Convenience
	private DebugStore debugStore = new DebugStore();
	private FunctionBindings functionBindings = new FunctionBindings();

	private InputMultiplexer multiplexer = new InputMultiplexer();
	/**
	 * For running game at constant FPS
	 */
	private Timer timer = new Timer();

	// ----- Variables ----- //
	private Color backgroundColor = HoloGL.rgb(79, 121, 66); // HoloGL.rbg(255, 236, 179);
	private boolean mouseScrollEnabled = true;

	private enum GameState {
		PLAYING, VICTORY, DEFEAT;
		public boolean isComplete() {
			return this != PLAYING;
		}
	}

	private GameState gameState = GameState.PLAYING;

	public CombatDemo(final Holowyth game) {
		super(game);

		skin = game.skin;
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
			combatDemoUI.getStatsPanelUI().toggleDetailedView();
		}, Keys.V);
	}

	@Override
	public void render(float delta) {
		stage.act();
		handleMousePanning(delta);
		renderer.render(delta);

		updateTitleBarInformation();

		combatDemoUI.onRender();

		ifTimeElapsedTickGame();
		ai.update(delta);
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

		if (mouseScrollEnabled && mouseY > screenHeight - scrollMargin ||
				Gdx.input.isKeyPressed(Keys.DOWN))
			camera.translate(0, -scrollSpeed + snapLeftoverY);
		if (mouseScrollEnabled && mouseY < scrollMargin ||
				Gdx.input.isKeyPressed(Keys.UP))
			camera.translate(0, scrollSpeed + snapLeftoverY);

		if (mouseScrollEnabled && mouseX > screenWidth - scrollMargin ||
				Gdx.input.isKeyPressed(Keys.RIGHT))
			camera.translate(scrollSpeed + snapLeftoverX, 0);
		if (mouseScrollEnabled && mouseX < scrollMargin ||
				Gdx.input.isKeyPressed(Keys.LEFT))
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

	private void ifTimeElapsedTickGame() {
		timer.start(1000 / Holo.GAME_FPS);
		if (timer.taskReady() && !gamePaused) {
			tickGame();
		}
	}

	private void tickGame() {
		world.tick();
		gfx.tick();
		handleGameOver();
	}

	private void handleGameOver() {
		var units = world.getUnits();

		if (gameState == GameState.PLAYING) {
			if (!IterableUtils.matchesAny(units, u -> u.getSide().isPlayer())) {
				onDefeat();
			} else if (!IterableUtils.matchesAny(units, u -> u.getSide().isEnemy())) {
				onVictory();
			}
		}

	}

	private void onVictory() {
		gameState = GameState.VICTORY;
		showVictoryPanel();
	}

	private void onDefeat() {
		gameState = GameState.DEFEAT;
		showDefeatPanel();
	}

	private final Table victoryPanel = new Table();
	private final Table defeatPanel = new Table();
	private final Table instructionsPanel = new Table();
	{
		createVictoryPanel();
		createDefeatPanel();
		createInstructionsPanel();
	}

	private void createVictoryPanel() {
		var largeStyle = new LabelStyle(Holowyth.fonts.borderedLargeFont(), Color.WHITE);
		var medStyle = new LabelStyle(Holowyth.fonts.borderedMediumFont(), Color.WHITE);

		Table frame = new Table();
		Label mainText = new Label("Victory!", largeStyle);
		victoryPanel.add(mainText).size(275, 200);
		victoryPanel.row();
		victoryPanel.add(new Label("Press T to restart", medStyle));

		victoryPanel.setBackground(HoloUI.getSolidBG(Color.DARK_GRAY, 0.9f));
		// victoryPanel.setVisible(false);
		mainText.setAlignment(Align.center);
		victoryPanel.center();
		frame.add(victoryPanel);
		stage.addActor(frame);
		frame.setFillParent(true);

		victoryPanel.setVisible(false);
	}

	private void createDefeatPanel() {
		var largeStyle = new LabelStyle(Holowyth.fonts.borderedLargeFont(), Color.WHITE);
		var medStyle = new LabelStyle(Holowyth.fonts.borderedMediumFont(), Color.WHITE);

		Table frame = new Table();
		Label mainText = new Label("Defeat", largeStyle);
		defeatPanel.add(mainText).size(275, 200);
		defeatPanel.row();
		defeatPanel.add(new Label("Press T to retry", medStyle));

		defeatPanel.setBackground(HoloUI.getSolidBG(Color.DARK_GRAY, 0.9f));
		// victoryPanel.setVisible(false);
		mainText.setAlignment(Align.center);
		defeatPanel.center();
		frame.add(defeatPanel);
		stage.addActor(frame);
		frame.setFillParent(true);

		defeatPanel.setVisible(false);
	}

	private final String instructionsText = "Controls:\n" +
			"Select Units: Left-Click or Left-click drag\n" +
			"Confirm Order Location/Target: Left-click\n" +
			"Order Move: Right-click\n" +
			"Order Attack: A\n" +
			"Order Retreat: R\n" +
			"Order Stop: S\n" +
			"\n" +
			"Use Skills: 1-9, 0\n" +
			"\n" +
			"Pause Time: Space   (important!)\n" +
			"Pan Camera: Arrow Keys\n" +
			"\n" +
			"Status effects:\n" +
			"Stun: Major atk/def penalty, cannot take any action\n" +
			"Reel: Moderate atk/def penalty, is slowed.\n" +
			"Blind: Prevents units from casting skills, and also interrupts ranged skills.";

	private void createInstructionsPanel() {
		var style = new LabelStyle(Holowyth.fonts.debugFont(), Color.WHITE);
		var panel = instructionsPanel;

		Table frame = new Table();

		var l1 = new Label("Instructions  (Press Q to bring up at any time)", style);
		panel.add(l1);
		panel.row();
		var l2 = new Label("", style);
		panel.add(l2);
		panel.row();

		Label mainText = new Label(instructionsText, style);
		mainText.setWrap(true);
		panel.add(mainText).size(400, 400);

		panel.setBackground(HoloUI.getSolidBG(Color.DARK_GRAY, 0.9f));
		// victoryPanel.setVisible(false);
		mainText.setAlignment(Align.left);
		panel.center();

		frame.add(panel);
		stage.addActor(frame);
		frame.setFillParent(true);

		panel.pad(20);
		panel.setVisible(false);
	}

	/**
	 * Show victory panel after a short delay
	 */
	private void showVictoryPanel() {
		victoryPanel.addAction(sequence(delay(2), run(() -> victoryPanel.setVisible(true))));
	}

	/**
	 * Show victory panel after a short delay
	 */
	private void showDefeatPanel() {
		defeatPanel.addAction(sequence(delay(2), run(() -> defeatPanel.setVisible(true))));
	}

	private void showInstructionsPanel() {
		instructionsPanel.setVisible(true);
		pauseGame();
	}

	private void hideInstructionsPanel() {
		instructionsPanel.setVisible(false);
		unpauseGame();
	}

	private void restartLevel() {

		logger.debug("Restarted level!");

		gameState = GameState.PLAYING;

		victoryPanel.setVisible(false);
		defeatPanel.setVisible(false);

		world.clearAllUnits();
		controls.clearSelectedUnits();

		testing.setupPlannedScenario();
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

		showInstructionsPanel();
	}

	@Override
	protected void mapShutdown() {
		System.out.println("mapShutdown called");
		combatDemoUI.onMapShutdown();
	}

	private void initializeAppLifetimeComponents() {
		pathingModule = new PathingModule(camera, shapeRenderer);
	
		renderer = new Renderer(game, camera, stage, pathingModule);
		renderer.setClearColor(backgroundColor);
	
		animations = new Animations();
	
		ai = new AIModule();
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
		controls = new Controls(game, camera, fixedCam, world.getUnits(), debugStore, world, combatDemoUI.getGameLog());
		multiplexer.addProcessor(controls);

		// Set Renderer to render world and other map-lifetime components
		renderer.setWorld(world);
		renderer.setTiledMap(map, mapWidth, mapHeight);
		renderer.setUnitControls(controls);
		renderer.setEffectsHandler(gfx);

		// UI
		combatDemoUI.onMapStartup();

		// Testing
		testing = new CombatPrototyping(world, controls);

	}

	

	/* Input methods */

	@Override
	public boolean keyDown(int keycode) {
		functionBindings.runBoundFunction(keycode);

		if (keycode == Keys.T) {
			if (gameState.isComplete()) {
				restartLevel();
			}
		}
		if (keycode == Keys.Q) {
			if (instructionsPanel.isVisible()) {
				hideInstructionsPanel();
			} else {
				showInstructionsPanel();
			}
		}

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (button == Input.Buttons.RIGHT && pointer == 0) {
			if (instructionsPanel.isVisible())
				hideInstructionsPanel();
		}
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
		return controls;
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
