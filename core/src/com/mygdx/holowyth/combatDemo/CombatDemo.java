package com.mygdx.holowyth.combatDemo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

		combatDemoUI = new CombatDemoUI(stage, debugStore, skin);

		// Configure Input
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(this);

		// Load map and test units

		loadMapFromDisk(Holo.mapsDirectory + Holo.editorInitialMap);

		mapStartup();

		Table debugInfo = combatDemoUI.getDebugInfo();
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

		updateTitleBarInformation();
		renderCursor();

		combatDemoUI.onRender();

		ifTimeElapsedTickWorld();
	}

	private void ifTimeElapsedTickWorld() {
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

	Unit playerUnit;

	/**
	 * Initializes neccesary game components. <br>
	 * Creates map-lifetime components and sets up application-lifetime components. <br>
	 * The map in question is {@link #map} <br>
	 * Call after loading a new map. The mirror function is mapShutdown.
	 */
	@Override
	protected void mapStartup() {

		effects = new EffectsHandler(game, camera, debugStore);

		// Init Pathing
		pathingModule.initForMap(this.map);

		// Init World
		world = new World(this.map, pathingModule, debugStore, effects);

		// Init Unit controls
		if (unitControls != null) {
			multiplexer.removeProcessor(unitControls);
		}
		unitControls = new Controls(game, camera, fixedCam, world.getUnits(), debugStore, world);
		multiplexer.addProcessor(unitControls);

		// Set Renderer to render world and other map-lifetime components
		renderer.setWorld(world);
		renderer.setUnitControls(unitControls);
		renderer.setEffectsHandler(effects);

		// UI
		combatDemoUI.onMapStartup();

		/* Test Area */

		// Create test units
		playerUnit = world.createPlayerUnit();
		unitControls.selectedUnits.add(playerUnit);
		world.spawnSomeEnemyUnits();

		// GroundSkill implosion = new Skills.Implosion();
		// implosion.pluginTargeting(playerUnit, 428, 281);
		// playerUnit.orderUseSkill(implosion);

		// playerUnit.orderMove(CELL_SIZE * 22 + 10, CELL_SIZE * 15 + 20);

	}

	@Override
	protected void mapShutdown() {
		System.out.println("test");
		combatDemoUI.getDebugInfo().clear();
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
		combatDemoUI.updateMouseCoordLabel(screenX, screenY, camera);
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		combatDemoUI.updateMouseCoordLabel(screenX, screenY, camera);
		return false;
	}

}
