package com.mygdx.holowyth.combatDemo;

import static com.mygdx.holowyth.util.DataUtil.getAsPercentage;
import static com.mygdx.holowyth.util.DataUtil.getRoundedString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

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
import com.mygdx.holowyth.combatDemo.effects.EffectsHandler;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.pathfinding.CBInfo;
import com.mygdx.holowyth.pathfinding.HoloPF;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.statsBranch.StatsDemo;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.PresetUnits;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloGL;
import com.mygdx.holowyth.util.HoloIO;
import com.mygdx.holowyth.util.HoloUI;
import com.mygdx.holowyth.util.data.Point;
import com.mygdx.holowyth.util.data.Segment;
import com.mygdx.holowyth.util.debug.DebugDemo;
import com.mygdx.holowyth.util.debug.DebugStore;
import com.mygdx.holowyth.util.debug.DebugValue;
import com.mygdx.holowyth.util.debug.DebugValues;
import com.mygdx.holowyth.util.debug.ValueLabelMapping;
import com.mygdx.holowyth.util.exception.ErrorCode;
import com.mygdx.holowyth.util.exception.HoloException;
import com.mygdx.holowyth.util.template.DemoScreen;
import com.mygdx.holowyth.util.tools.KeyTracker;
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

	// Input
	private InputMultiplexer multiplexer = new InputMultiplexer();

	// Scene2D
	private Table root;
	Skin skin;

	// Game Components

	UnitControls unitControls;
	PathingModule pathingModule;
	EffectsHandler effects; //keeps track of vfx effects

	World world;

	// Appearance
	Color initialClearColor = HoloUI.color(255, 236, 179);

	// Misc.
	FPSLogger fps = new FPSLogger();

	// Settings
	
	// Frame rate control
	Timer timer = new Timer();
	

	int CELL_SIZE = Holo.CELL_SIZE;

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

		// Init Renderer
		renderer = new Renderer(game, camera, stage, pathingModule);
		renderer.setClearColor(initialClearColor);

		initGameComponentsForMapStartup();
	}

	@Override
	public void render(float delta) {
		renderer.render(delta);

		displayTitleBarInformation();

		// Cursor related
		renderCursor();
		
		// update debug display
		valueLabelMapping.forEach(CombatDemo::updateLabel);
		
		//Game logic
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

		// Add Widgets here

		// createParameterWindow();

		root.debug();

		createCoordinateText();

//		createParameterWindow();

		createDebugInfoDisplay();

	}

	Window parameterWindow;

	private void createParameterWindow() {
		// Create a table for adjusting parameters

		parameterWindow = new Window("Parameters", skin);
		parameterWindow.setPosition(0, 100);

		// root.add(new TextButton("test", skin));
		stage.addActor(parameterWindow);
		// HoloUI.parameterSlider(0, Holo.defaultUnitMoveSpeed, "initialMoveSpeed", testing, skin,
		// (Float f) -> playerUnit.initialMoveSpeed = f);
		parameterWindow.pack();
	}

	Label coordInfo;

	private void createCoordinateText() {
		coordInfo = new Label("(000, 000)\n", skin);
		coordInfo.setColor(Color.BLACK);
		stage.addActor(coordInfo);
		coordInfo.setPosition(Gdx.graphics.getWidth() - coordInfo.getWidth() - 4, 4);
	}

	Table debugInfo;

	private void createDebugInfoDisplay() {
		debugInfo = new Table();
		debugInfo.setFillParent(true);
		
		debugInfo.top().left();
		debugInfo.pad(4);
//		debugInfo.debug();
		
		debugInfo.defaults().spaceRight(20).left();
		
		stage.addActor(debugInfo);
	}

	// *** Run on Map Load (Important!) ***//

	Unit playerUnit;
	DebugStore debugStore = new DebugStore();

	
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
		unitControls = new UnitControls(game, camera, world.units, debugStore);
		multiplexer.addProcessor(unitControls);

		populateDebugTable();

		// Set Renderer to render world and other map-lifetime components
		renderer.setWorld(world);
		renderer.setUnitControls(unitControls);
		renderer.setEffectsHandler(effects);

		/* Test Area */

		// Create test units
		playerUnit = world.spawnUnit(320, 220, Unit.Side.PLAYER, "Elvin");
		PresetUnits.loadUnitStats(playerUnit.stats);
		PresetUnits.loadSomeEquipment(playerUnit.stats);
		PresetUnits.loadArmor(playerUnit.stats);
		playerUnit.stats.prepareUnit();
		playerUnit.stats.printInfo();
		
		unitControls.selectedUnits.add(playerUnit);
		world.spawnSomeEnemyUnits();

		// playerUnit.orderMove(CELL_SIZE * 22 + 10, CELL_SIZE * 15 + 20);

	}

	ValueLabelMapping valueLabelMapping = new ValueLabelMapping();
	private void populateDebugTable() {
		
		LabelStyle debugStyle = new LabelStyle(game.debugFont, Holo.debugFontColor);
		

		for (Map.Entry<String, DebugValues> entry : debugStore.getStore().entrySet()) {
			String componentName = entry.getKey();
			debugInfo.add(new Label(componentName, debugStyle));
			debugInfo.row();
			ArrayList<DebugValue> listOfValues = entry.getValue();

			for (DebugValue v : listOfValues) {
				Label n = new Label(" -" + v.getName(), debugStyle);
				Label l = new Label("", debugStyle);
				debugInfo.add(n, l);
				debugInfo.row();
				valueLabelMapping.registerLabel(v, l);
			}
		}
	}

	private static void updateLabel(DebugValue v, Label l) {
		String str;

		switch (v.getValueType()) {
		case FLOAT:
			if (v.shouldDisplayAsPercentage()) {
				str = getAsPercentage(v.getFloatValue());
			} else {
				str = getRoundedString(v.getFloatValue());
			}
			break;
		case INT:
			str = String.valueOf(v.getIntValue());
			break;
		case STRING:
			str = v.getStringValue();
			break;
		default:
			System.out.println("Unsupported debug value type");
			str = null;
			break;
		}
		
		l.setText(str);
//		System.out.println("Updated value:  -" + v.name + " " + str);
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
		// TODO Auto-generated method stub
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
		// update coordInfo
		Vector3 vec = new Vector3();
		vec = camera.unproject(vec.set(screenX, screenY, 0));
		coordInfo.setText("(" + (int) (vec.x) + ", " + (int) (vec.y) + ")\n" + "(" + (int) (vec.x) / CELL_SIZE + ", "
				+ (int) (vec.y) / CELL_SIZE + ")");
		return false;
	}
}
