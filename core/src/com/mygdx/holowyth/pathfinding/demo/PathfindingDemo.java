package com.mygdx.holowyth.pathfinding.demo;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNull;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.world.map.simplemap.SimpleMap;
import com.mygdx.holowyth.pathfinding.UnitPF;
import com.mygdx.holowyth.pathfinding.HoloPF;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.pathfinding.PathingRenderer;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloIO;
import com.mygdx.holowyth.util.HoloUI;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;
import com.mygdx.holowyth.util.tools.TaskTimer;

public class PathfindingDemo implements Screen, InputProcessor, PFWorld {

	private final Holowyth game;

	// Rendering and pipeline variables
	OrthographicCamera camera;
	OrthographicCamera fixedCam;
	ShapeRenderer shapeRenderer;
	SpriteBatch batch;

	// Input
	private InputMultiplexer multiplexer = new InputMultiplexer();

	// Scene2D
	private Stage stage;
	private Table root;

	Skin skin = VisUI.getSkin();

	// App Fields
	SimpleMap map;
	PFUnitControls unitControls;
	PathingModule pathingModule;
	private PathingRenderer pathingRenderer;
	
	// Appearance
	Color defaultClearColor = HoloGL.rgb(255, 236, 179);
	Color clearColor = defaultClearColor;

	// Logic
	TaskTimer timer = new TaskTimer();

	

	public PathfindingDemo(final Holowyth game) {
		this.game = game;
		this.camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.fixedCam = new OrthographicCamera();
		fixedCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		stage = new Stage(new ScreenViewport());

		shapeRenderer = game.shapeRenderer;
		batch = game.batch;

		pathingModule = new PathingModule();
		pathingRenderer = new PathingRenderer(pathingModule, shapeRenderer);

		createUI();
	}

	FPSLogger fps = new FPSLogger();

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		camera.update();

		pathingRenderer.renderGraph(false);
		// renderDynamicGraph(false);

		if (this.map != null) {
			renderMapPolygons();

			renderMapBoundaries();
		}

		renderPaths(false);
		for (PFDemoUnit u : units) {
			u.renderNextWayPoint(shapeRenderer);
		}
		renderUnitDestinations(Color.GREEN);

		unitControls.renderCirclesOnSelectedUnits();
		renderUnits();

		unitControls.renderSelectionBox(PFUnitControls.defaultSelectionBoxColor);
		// Rendering Test area;

		// UI
		stage.act(delta);
		stage.draw();

		displayTitleBarInformation();

		// Cursor related
		renderCursor();

		// User Controls

		// Testing area

		// for (Vertex v : nearbyPathable) {
		// HoloGL.renderCircle(v.ix * CELL_SIZE, v.iy * CELL_SIZE, shapeRenderer, Color.RED);
		// }

		// render expanded hit bodies
		for (PFDemoUnit u : units) {
			HoloGL.renderCircleOutline(u.x, u.y, u.getRadius() + Holo.UNIT_RADIUS, Color.GRAY);
		}

		// pathing.stepAStar();

		int blank = 1;
		blank = blank + 1;

		timer.start(1000 / 60);

		if (timer.taskReady()) {
			doOnFrame();
		}

	}

	private void doOnFrame() {
		tickLogicForUnits();
		moveUnits();

		// Testing area

		// System.out.format("Unit xy: (%s, %s) %s %n", playerUnit.x, playerUnit.y, playerUnit.curSpeed);
	}

	private void renderMapPolygons() {
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.setColor(Color.BLACK);
		HoloGL.renderPolygons(map.polys);
	}

	private void renderMapBoundaries() {
		shapeRenderer.setProjectionMatrix(camera.combined);
		HoloGL.renderMapBoundaries(map);
	}

	@Override
	public void show() {
		System.out.println("Showed Pathfinding Demo");
		multiplexer.clear();
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(multiplexer);

		// openFileChooserToLoadMap();
		if (Holo.initialSimpleMap != null) {
			loadMap(HoloIO.getMapFromDisk(Holo.simpleMapsDirectory + Holo.initialSimpleMap));
		}
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
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

		// Add Widgets here

		// createParameterWindow();

		root.debug();

		createCoordinateText();
	}

	Label coordInfo;

	@SuppressWarnings("unused")
	private void createParameterWindow() {
		// Create a table for adjusting parameters

		testing = new Window("Parameters", skin);
		testing.setPosition(700, 400);

		// root.add(new TextButton("test", skin));
		stage.addActor(testing);
		HoloUI.parameterSlider(0.01f, 10f, "COLLISION_CLEARANCE_DISTANCE", testing, skin,
				(Float f) -> Holo.collisionClearanceDistance = f);
		HoloUI.parameterSlider(0, Holo.defaultUnitMoveSpeed, "initialMoveSpeed", testing, skin,
				(Float f) -> playerUnit.initialMoveSpeed = f);
		testing.pack();
	}

	private void createCoordinateText() {
		coordInfo = new Label("(000, 000)\n", skin);
		coordInfo.setColor(Color.BLACK);
		stage.addActor(coordInfo);
		coordInfo.setPosition(Gdx.graphics.getWidth() - coordInfo.getWidth() - 4, 4);
	}

	// Expanded Geometry

	// Rendering
	private void renderPaths(boolean renderIntermediatePaths) {
		// Render Path

		if (renderIntermediatePaths) {
			pathingRenderer.renderIntermediateAndFinalPaths(units);
		} else {
			for (PFDemoUnit unit : units) {
				if (unit.path != null) {
					renderPath(unit.path, Color.GRAY, false);
				}
			}
		}

	}

	float pathThickness = 2f;

	private void renderPath(Path path, Color color, boolean renderPoints) {
		HoloPF.renderPath(path, color, renderPoints, pathThickness, shapeRenderer);
	}

	private void renderUnitDestinations(Color color) {

		for (PFDemoUnit unit : units) {
			if (unit.path != null) {
				Point finalPoint = unit.path.get(unit.path.size() - 1);
				shapeRenderer.begin(ShapeType.Filled);
				shapeRenderer.setColor(color);
				shapeRenderer.circle(finalPoint.x, finalPoint.y, 4f);
				shapeRenderer.end();

				shapeRenderer.begin(ShapeType.Line);
				shapeRenderer.setColor(Color.BLACK);
				shapeRenderer.circle(finalPoint.x, finalPoint.y, 4f);
				shapeRenderer.end();
			}

		}
	}

	private void renderUnits() {
		for (PFDemoUnit unit : units) {
			shapeRenderer.begin(ShapeType.Filled);

			if (unit == playerUnit) {
				shapeRenderer.setColor(Color.PURPLE);
			} else {
				shapeRenderer.setColor(Color.YELLOW);
			}

			shapeRenderer.circle(unit.x, unit.y, Holo.UNIT_RADIUS);

			shapeRenderer.end();
		}

		// Render an outline around the unit
		for (PFDemoUnit unit : units) {
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(Color.BLACK);
			shapeRenderer.circle(unit.x, unit.y, Holo.UNIT_RADIUS);
			shapeRenderer.end();
		}

	}

	// *** Run on Map Load (Important!) ***//

	int CELL_SIZE = Holo.CELL_SIZE;

	/**
	 * Logically initializes a bunch of components necessary to run the map
	 */
	private void mapStartup(SimpleMap map) {
		// Unit Controls
		if (unitControls != null) {
			multiplexer.removeProcessor(unitControls);
		}
		unitControls = new PFUnitControls(game, camera, units);
		multiplexer.addProcessor(unitControls);

		// Pathfinding Graph

		pathingModule.initForSimpleMap(map);
		// Pathing

		//// ---------Test Area---------////:

		// Create units
		@NonNull PFDemoUnit unit = new PFDemoUnit(35, 20, this); 
		units.add(unit);
		unitControls.selectedUnits.add(unit);
		playerUnit = unit;
		
		createTestUnits();

		orderMoveTo(playerUnit, CELL_SIZE * 22 + 10, CELL_SIZE * 15 + 20);

	}

	private void createTestUnits() {
		units.add(new PFDemoUnit(406, 253, this));
		units.add(new PFDemoUnit(550, 122 - Holo.UNIT_RADIUS, this));
		units.add(new PFDemoUnit(750, 450, this));
	}

	// Unit Logic Related

	ArrayList<@NonNull PFDemoUnit> units = new ArrayList<@NonNull PFDemoUnit>();
	PFDemoUnit playerUnit; // the main unit we are using to demo pathfinding

	public void orderMoveTo(PFDemoUnit u, float dx, float dy) {
		u.orderMove(dx, dy);
	}

	private void tickLogicForUnits() {
		for (PFDemoUnit u : units) {
			u.handleGeneralLogic();
		}
	}

	private void moveUnits() {
		for (PFDemoUnit u : units) {

			// Validate the motion by checking against other colliding bodies.

			float dx = u.x + u.vx;
			float dy = u.y + u.vy;

			if (u.vx == 0 && u.vy == 0) {
				continue;
			}

			Segment motion = new Segment(u.x, u.y, dx, dy);

			ArrayList<UnitPF> colBodies = new ArrayList<UnitPF>();
			for (PFDemoUnit other : units) {
				if (u.equals(other)) { // don't consider the unit's own collision body
					continue;
				}

				colBodies.add(other);
			}
			ArrayList<UnitPF> collisions = HoloPF.detectCollisionsFromUnitMoving(motion.x1, motion.y1, motion.x2, motion.y2,
					colBodies, u.getRadius());
			if (collisions.isEmpty()) {
				u.x += u.vx;
				u.y += u.vy;
			} else {

				// if line intersects with one or more other units. (should be max two, since units should not be
				// overlapped)
				if (collisions.size() > 2) {
					System.out.println("Wierd case, unit colliding with more than 2 units");
				}
				// TODO: handle case where velocity is 0,0

				float curDestx = u.x + u.vx;
				float curDesty = u.y + u.vy;
				// Vector2 curVel = new Vector2(u.vx, u.vy);

				System.out.format("%s is colliding with %s bodies%n", u, collisions.size());

				for (UnitPF cb : collisions) {
					Vector2 dist = new Vector2(curDestx - cb.getX(), curDesty - cb.getY());

					// At first, dist is smaller than the combined radius, but previous push outs might have changed
					// this.
					if (dist.len() > cb.getRadius() + u.getRadius()) {
						continue;
					}

					// expand
					Vector2 pushedOut = new Vector2(dist)
							.setLength(cb.getRadius() + u.getRadius() + Holo.collisionClearanceDistance);
					// TODO: handle edge case here dist is 0

					curDestx = cb.getX() + pushedOut.x;
					curDesty = cb.getY() + pushedOut.y;
				}

				// Take this motion if it's valid, otherwise don't move unit.
				if (HoloPF.isSegmentPathable(u.x, u.y, curDestx, curDesty, pathingModule.getObstacleExpandedSegs(), pathingModule.getObstaclePoints(),
						colBodies,
						u.getRadius())) {
					u.x = curDestx;
					u.y = curDesty;
				}
				// return; //debugging, step for every movement that has collisions

			}

		}

	}

	/* vvvvvvv Boilerplate code vvvvvvv */

	// UI and Disk

	private void displayTitleBarInformation() {
		if (this.map == null) {
			Gdx.graphics.setTitle(Holo.titleName + " --- " + "No map loaded");
		} else {
			String starText;
			starText = (this.map.hasUnsavedChanges) ? "*" : "";
			Gdx.graphics.setTitle(
					Holo.titleName + " --- " + map.name + " [" + map.width() + "x" + map.height() + "] " + starText);
		}

	}

	@SuppressWarnings("unused")
	private void openFileChooserToLoadMap() {
		System.out.println("Opening Load Dialog");
		stage.addActor(game.fileChooser);

		game.fileChooser.setMode(Mode.OPEN);
		game.fileChooser.setSelectionMode(SelectionMode.FILES);
		game.fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected(Array<FileHandle> file) {
				System.out.println("Selected file: " + file.get(0).file().getAbsolutePath());

				System.out.println("Removed Load Dialog");
				loadMapFromDisk(file.get(0).file().getAbsolutePath());
			}
		});

		game.fileChooser.setDirectory(Holo.simpleMapsDirectory);
	}

	private void loadMapFromDisk(String pathname) {
			SimpleMap loadedMap = HoloIO.getMapFromDisk(pathname);
			loadMap(loadedMap);
	}

	private void loadMap(SimpleMap map) {
		System.out.println("New map loaded");
		this.map = map;
		map.hasUnsavedChanges = false;

		camera.position.set(map.width() / 2, map.height() / 2, 0);

		mapStartup(this.map);
	}

	// Cursor Related
	private void renderCursor() {
		batch.setProjectionMatrix(fixedCam.combined);
		if (true) {
			batch.begin();
			Texture cursorImg = game.assets.get("img/cursors/cursor.png", Texture.class);

			batch.draw(cursorImg, Gdx.input.getX(),
					Gdx.graphics.getHeight() - Gdx.input.getY() - cursorImg.getHeight());

			batch.end();
		}
	}


	/* ^^^^^^ End of User Methods ^^^^^^ */

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
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
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
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

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	@Override
	public ArrayList<@NonNull PFDemoUnit> getUnits() {
		return this.units;
	}

	@Override
	public PathingModule getPathingModule() {
		return this.pathingModule;
	}

}
