package com.mygdx.holowyth.combatDemo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.combatDemo.effects.EffectsHandler;
import com.mygdx.holowyth.combatDemo.ui.DebugStoreUI;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloGL;
import com.mygdx.holowyth.util.HoloUtil;
import com.mygdx.holowyth.util.data.Point;
import com.mygdx.holowyth.util.debug.DebugStore;
import com.mygdx.holowyth.util.template.DemoScreen;
import com.mygdx.holowyth.util.tools.FunctionBindings;
import com.mygdx.holowyth.util.tools.Timer;

/**
 * Is responsible for the startup construction of the UI, and setting up the other components on both creation and map
 * load
 * 
 * @author Colin Ta
 *
 */
public class CombatDemo extends DemoScreen implements Screen, InputProcessor {

	// Rendering and pipeline variables
	ShapeRenderer shapeRenderer;
	Renderer renderer;

	// UI
	DebugStoreUI ui;

	// Scene2D
	private Table root;
	Skin skin;

	// Game Components
	Controls unitControls;
	PathingModule pathingModule;

	// Game state
	World world;

	// Graphical Components
	EffectsHandler effects; // keeps track of vfx effects

	// Input
	private InputMultiplexer multiplexer = new InputMultiplexer();

	// Frame rate control
	Timer timer = new Timer();

	Color backgroundColor = HoloGL.rbg(79, 121, 66); // HoloGL.rbg(255, 236, 179);

	// For debugging and playtesting
	DebugStore debugStore = new DebugStore();
	private FunctionBindings functionBindings = new FunctionBindings();

	public CombatDemo(final Holowyth game) {
		super(game);

		skin = game.skin;
		shapeRenderer = game.shapeRenderer;

		initializeAppLifetimeComponents();

		ui = new DebugStoreUI(stage, debugStore);
		createUI();

		// Configure Input
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(this);

		// Load map and test units

		loadMapFromDisk(Holo.mapsDirectory + Holo.editorInitialMap);

		initGameComponentsForMapStartup();

		functionBindings.bindFunctionToKey(() -> debugInfo.setVisible(!debugInfo.isVisible()), Keys.GRAVE); // tilde key
	}

	private void initializeAppLifetimeComponents() {
		pathingModule = new PathingModule(camera, shapeRenderer);

		renderer = new Renderer(game, camera, stage, pathingModule);
		renderer.setClearColor(backgroundColor);
	}

	@Override
	public void render(float delta) {
		renderer.render(delta);

		displayTitleBarInformation();

		// Cursor related
		renderCursor();

		// update debug display
		ui.updateDebugValueDisplay();

		// Game logic
		timer.start(1000 / 60);

		if (timer.taskReady()) {
			world.tick();
			effects.tick();
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

	private void createUI() {
		stage = new Stage(new ScreenViewport());

		root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		root.top().left();
		stage.addActor(root);

		root.debug();

		// Add Widgets here
		createCoordinateText();
		ui.createDebugInfoDisplay();
	}

	Window parameterWindow;

	@SuppressWarnings("unused")
	/**
	 * A parameter window produces a slider which can be used to adjust variables dynamically
	 */
	private void createParameterWindow() {

		parameterWindow = new Window("Parameters", skin);
		parameterWindow.setPosition(0, 100);

		// root.add(new TextButton("test", skin));
		stage.addActor(parameterWindow);
		// HoloUI.parameterSlider(0, Holo.defaultUnitMoveSpeed,
		// "initialMoveSpeed", testing, skin,
		// (Float f) -> playerUnit.initialMoveSpeed = f);
		parameterWindow.pack();
	}

	Label coordInfo;

	/**
	 * Adds a small coordinate text that displays the mouse cursor position in world coordinates
	 */
	private void createCoordinateText() {
		coordInfo = new Label("(000, 000)\n", skin);
		coordInfo.setColor(Color.BLACK);
		stage.addActor(coordInfo);
		coordInfo.setPosition(Gdx.graphics.getWidth() - coordInfo.getWidth() - 4, 4);
	}

	Table debugInfo;

	Unit playerUnit;

	/**
	 * Initializes neccesary game components. <br>
	 * Call after loading a new map. The mirror function is mapShutdown.
	 */
	private void initGameComponentsForMapStartup() {

		effects = new EffectsHandler(game, camera, debugStore);

		// Init Pathing
		pathingModule.initForMap(this.map);

		// Init World
		world = new World(this.map, pathingModule, debugStore, effects);

		// Init Unit controls
		if (unitControls != null) {
			multiplexer.removeProcessor(unitControls);
		}
		unitControls = new Controls(game, camera, fixedCam, world.units, debugStore, world);
		multiplexer.addProcessor(unitControls);

		// Set Renderer to render world and other map-lifetime components
		renderer.setWorld(world);
		renderer.setUnitControls(unitControls);
		renderer.setEffectsHandler(effects);

		/* Test Area */

		// Create test units
		playerUnit = world.createPlayerUnit();
		unitControls.selectedUnits.add(playerUnit);
		world.spawnSomeEnemyUnits();

		// playerUnit.orderMove(CELL_SIZE * 22 + 10, CELL_SIZE * 15 + 20);
		ui.populateDebugValueDisplay();

	}

	@Override
	protected void mapShutdown() {
		System.out.println("test");
		debugInfo.clear();
	}

	/* Input methods */

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		functionBindings.runBoundFunction(keycode);
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {

		if (button == Input.Buttons.LEFT && pointer == 0) {

			// Vector3 vec = new Vector3();
			// vec = camera.unproject(vec.set(screenX, screenY, 0));
			// orderMoveTo(playerUnit, vec.x, vec.y);

			return false;
		}
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		updateMouseCoordLabel(screenX, screenY);
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		updateMouseCoordLabel(screenX, screenY);
		return false;
	}

	private void updateMouseCoordLabel(int screenX, int screenY) {
		Point p = HoloUtil.getCursorInWorldCoords(camera);
		coordInfo.setText(
				"(" + (int) (p.x) + ", " + (int) (p.y) + ")\n" + "(" + (int) (p.x) / Holo.CELL_SIZE + ", "
						+ (int) (p.y) / Holo.CELL_SIZE + ")");

	}
}
