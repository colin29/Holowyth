package com.mygdx.holowyth.combatDemo;

import java.util.ArrayList;
import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.combatDemo.Unit.Side;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.pathfinding.CBInfo;
import com.mygdx.holowyth.pathfinding.HoloPF;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.util.DemoScreen;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloGL;
import com.mygdx.holowyth.util.HoloIO;
import com.mygdx.holowyth.util.HoloUI;
import com.mygdx.holowyth.util.data.Point;
import com.mygdx.holowyth.util.data.Segment;
import com.mygdx.holowyth.util.exception.ErrorCode;
import com.mygdx.holowyth.util.exception.HoloException;
import com.mygdx.holowyth.util.tools.KeyTracker;
import com.mygdx.holowyth.util.tools.Timer;

/**
 * Is responsible for the startup construction of the UI, and setting up the other components on both creation and map load
 * @author Colin Ta
 *
 */
public class CombatDemo extends DemoScreen implements Screen, InputProcessor {

	// Rendering and pipeline variables
	ShapeRenderer shapeRenderer;

	// Input
	private InputMultiplexer multiplexer = new InputMultiplexer();

	// Scene2D
	private Table root;

	Skin skin;

	// App Fields
	
	UnitControls unitControls;
	PathingModule pathingModule;
	
	World world;

	// Appearance
	Color initialClearColor = HoloUI.color(255, 236, 179);

	// Logic
	Timer timer = new Timer();
	
	Renderer renderer;

	public CombatDemo(final Holowyth game) {
		super(game);
		
		skin = game.skin;
		shapeRenderer = game.shapeRenderer;
		
		pathingModule = new PathingModule(camera, shapeRenderer);

		createUI();

		// Configure Input
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(this);
		
		// Load map and test units
		
		loadMapFromDisk(Holo.mapsDirectory + Holo.editorInitialMap);
		
		//Init Renderer
		renderer = new Renderer(game, camera, stage, pathingModule);
		renderer.setClearColor(initialClearColor);
		
		initGameComponentsForMapStartup();
	}

	FPSLogger fps = new FPSLogger();

	@Override
	public void render(float delta) {
		renderer.render(delta);

		displayTitleBarInformation();

		// Cursor related
		renderCursor();

		// User Controls

		// Testing area

		// for (Vertex v : nearbyPathable) {
		// HoloGL.renderCircle(v.ix * CELL_SIZE, v.iy * CELL_SIZE, shapeRenderer, Color.RED);
		// }

		// render expanded hit bodies
//		for (Unit u : units) {
//			HoloGL.renderCircleOutline(u.x, u.y, u.getRadius() + Holo.UNIT_RADIUS, shapeRenderer, Color.GRAY);
//		}

		// pathing.stepAStar();

		int blank = 1;
		blank = blank + 1;

		timer.start(1000 / 60);

		if (timer.taskReady()) {
			doOnFrame();
		}

	}


	@Override
	public void show() {
		System.out.println("Showed Pathfinding Demo");
		Gdx.input.setInputProcessor(multiplexer);
	}

	@Override
	public void dispose() {
	}

	/* vvvvvvv User Methods vvvvvvv */

	// UI
	Window testing;
	
	private void createUI() {
		stage = new Stage(new ScreenViewport());

		root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		root.top().left();

		root.addActor(debugInfo);
		stage.addActor(root);

		// Add Widgets here

		// createParameterWindow();

		root.debug();

		createCoordinateText();

		createDebugInfoDisplay();
		
		LabelStyle labelStyle = new LabelStyle(game.debugFont, Holo.debugFontColor);
		Table t = new Table();
		t.row();
		t.add(new Label("testName", labelStyle));
		t.add(new Label("testValue", labelStyle));
		debugInfo.add(t);
		debugInfo.debug();
	}

	Label coordInfo;

	private void createParameterWindow() {
		// Create a table for adjusting parameters

		testing = new Window("Parameters", skin);
		testing.setPosition(700, 400);

		// root.add(new TextButton("test", skin));
		stage.addActor(testing);
//		HoloUI.parameterSlider(0, Holo.defaultUnitMoveSpeed, "initialMoveSpeed", testing, skin,
//				(Float f) -> playerUnit.initialMoveSpeed = f);
		testing.pack();
	}

	private void createCoordinateText() {
		coordInfo = new Label("(000, 000)\n", skin);
		coordInfo.setColor(Color.BLACK);
		stage.addActor(coordInfo);
		coordInfo.setPosition(Gdx.graphics.getWidth() - coordInfo.getWidth() - 4, 4);
	}

	Table debugInfo = new Table();

	private void createDebugInfoDisplay() {
		// debugInfo.debug();
		debugInfo.setFillParent(true);
		debugInfo.top().left();
		debugInfo.pad(4);
		
		stage.addActor(debugInfo);
	}



	// *** Run on Map Load (Important!) ***//

	int CELL_SIZE = Holo.CELL_SIZE;

	/**
	 * Initializes neccesary game components. <br>
	 * Call after loading a new map. The mirror function is mapShutdown.
	 */
	private void initGameComponentsForMapStartup() {
	
		// Init Pathing
		pathingModule.initForMap(this.map);
		
		// Init World
		world = new World(this.map, pathingModule);
		
		// Init Unit controls
		if (unitControls != null) {
			multiplexer.removeProcessor(unitControls);
		}
		unitControls = new UnitControls(game, camera, world.units);
		multiplexer.addProcessor(unitControls);
		

		// Add Debugging info
		debugInfo.row();
		debugInfo.add(unitControls.getDebugTable());
		System.out.println(debugInfo.getColumns());
		
		// Set Renderer to render world and other map-lifetime components
		renderer.setWorld(world);
		renderer.setUnitControls(unitControls);

		

		/*  Test Area  */

		// Create test units
		playerUnit = world.spawnUnit(220, 220, Unit.Side.PLAYER);
		unitControls.selectedUnits.add(playerUnit);
		world.spawnSomeEnemyUnits();

//		playerUnit.orderMove(CELL_SIZE * 22 + 10, CELL_SIZE * 15 + 20);

	}

	

	/**
	 * Main function that contains game logic that is run every frame
	 */
	private void doOnFrame() {
		world.tick();
		
		// Testing area

		// System.out.format("Unit xy: (%s, %s) %s %n", playerUnit.x, playerUnit.y, playerUnit.curSpeed);
	}

	// Game and Combat Logic

//	ArrayList<Unit> units = new ArrayList<Unit>();
	Unit playerUnit; // the main unit we are using to demo pathfinding




	/* Below is boilerplate code for running a demo */

	// UI and Disk


	
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {

		if (button == Input.Buttons.LEFT && pointer == 0) {

			// Testing area

			// Vector3 vec = new Vector3();
			// vec = camera.unproject(vec.set(screenX, screenY, 0));
			// orderMoveTo(playerUnit, vec.x, vec.y);

			return false;
		}
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// update coordInfo
		Vector3 vec = new Vector3();
		vec = camera.unproject(vec.set(screenX, screenY, 0));
		coordInfo.setText("(" + (int) (vec.x) + ", " + (int) (vec.y) + ")\n" + "(" + (int) (vec.x) / CELL_SIZE + ", "
				+ (int) (vec.y) / CELL_SIZE + ")");
		return false;
	}
}
