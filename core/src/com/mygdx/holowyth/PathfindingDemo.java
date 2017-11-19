package com.mygdx.holowyth;

import java.awt.geom.Line2D;
import java.util.ArrayList;

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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.pathfinding.AStarSearch;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.PathSmoother;
import com.mygdx.holowyth.pathfinding.HoloPF;
import com.mygdx.holowyth.pathfinding.Unit;
import com.mygdx.holowyth.pathfinding.Vertex;
import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.util.HoloIO;
import com.mygdx.holowyth.util.HoloUI;
import com.mygdx.holowyth.util.constants.Holo;
import com.mygdx.holowyth.util.data.Pair;
import com.mygdx.holowyth.util.data.Point;
import com.mygdx.holowyth.util.exception.ErrorCode;
import com.mygdx.holowyth.util.exception.HoloException;
import com.mygdx.holowyth.util.tools.KeyTracker;
import com.mygdx.holowyth.util.tools.Timer;

public class PathfindingDemo implements Screen, InputProcessor {

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
	Field map;

	// Appearance
	Color defaultClearColor = HoloUI.color(255, 236, 179);
	Color clearColor = defaultClearColor;

	// Logic
	Timer timer = new Timer();

	public PathfindingDemo(final Holowyth game) {
		this.game = game;
		this.camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.fixedCam = new OrthographicCamera();
		fixedCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		stage = new Stage(new ScreenViewport());

		shapeRenderer = game.shapeRenderer;
		batch = game.batch;

		createUI();
	}

	FPSLogger fps = new FPSLogger();

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		camera.update();

		renderGraph();

		if (this.map != null) {
			renderMapPolygons();
			renderMapBoundaries();
		}

		// Rendering Test area;
		renderSearchResult();
		renderUnits();
		smoother.render(shapeRenderer);
		renderPath(smoother.path2s, Color.PINK, false);

		// UI
		stage.act(delta);
		stage.draw();

		displayTitleBarInformation();

		// Cursor related
		renderCursor();

		// User Controls

		// Testing area

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
	}

	private void renderMapPolygons() {
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.setColor(Color.BLACK);
		HoloIO.renderMapPolygons(map, shapeRenderer);
	}

	private void renderMapBoundaries() {
		shapeRenderer.setProjectionMatrix(camera.combined);
		HoloIO.renderMapBoundaries(map, shapeRenderer);
	}

	@Override
	public void show() {
		System.out.println("Showed Pathfinding Demo");
		multiplexer.clear();
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(multiplexer);

		// openFileChooserToLoadMap();
		loadMap(HoloIO.getMapFromDisk(Holo.mapsDirectory + "/complexMap.map"));
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	/* vvvvvvv User Methods vvvvvvv */

	// UI
	private void createUI() {
		stage = new Stage(new ScreenViewport());

		root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		root.top().left();

		// Add Widgets here

		root.debug();

	}

	// Pathfinding

	private int CELL_SIZE = 30;// 15; // size in pixels
	Vertex[][] graph;
	int graphWidth, graphHeight;

	private void createGraph() {
		graphWidth = (int) Math.floor(map.width() / CELL_SIZE) + 1;
		graphHeight = (int) Math.floor(map.height() / CELL_SIZE) + 1;

		graph = new Vertex[graphHeight][graphWidth];
	}

	private void linearFillGraph() {
		for (int y = 0; y < graphHeight; y++) {
			for (int x = 0; x < graphWidth; x++) {
				graph[y][x] = new Vertex();
				fillInVertex(graph[y][x], x, y);
			}
		}
	}

	/**
	 * Calculates the pathing information for a single vertex
	 */
	private void fillInVertex(Vertex v, int ix, int iy) {
		int x = ix * CELL_SIZE;
		int y = iy * CELL_SIZE;
		// v.N = isPointWithinMap(ix+CELL_SIZE + );
		v.N = isEdgePathable(x, y, x, y + CELL_SIZE);
		v.S = isEdgePathable(x, y, x, y - CELL_SIZE);
		v.W = isEdgePathable(x, y, x - CELL_SIZE, y);
		v.E = isEdgePathable(x, y, x + CELL_SIZE, y);

		v.NW = isEdgePathable(x, y, x - CELL_SIZE, y + CELL_SIZE);
		v.NE = isEdgePathable(x, y, x + CELL_SIZE, y + CELL_SIZE);
		v.SW = isEdgePathable(x, y, x - CELL_SIZE, y - CELL_SIZE);
		v.SE = isEdgePathable(x, y, x + CELL_SIZE, y - CELL_SIZE);

		if (ix == 0)
			v.W = v.NW = v.SW = false;
		if (ix == graphWidth - 1)
			v.E = v.NE = v.SE = false;
		if (iy == 0)
			v.S = v.SW = v.SE = false;
		if (iy == graphHeight - 1)
			v.N = v.NW = v.NE = false;
	}

	/**
	 * @return Whether the given line segment is pathable or not.
	 */
	private boolean isEdgePathable(float x, float y, float x2, float y2) {
		return HoloPF.isEdgePathable(x, y, x2, y2, map.polys);
	}

	private void renderGraph() {
		// // Draw Edges
		// shapeRenderer.setColor(Color.CORAL);
		// shapeRenderer.begin(ShapeType.Line);
		// for (int y = 0; y < graphHeight; y++) {
		// for (int x = 0; x < graphWidth; x++) {
		// Vertex v = graph[y][x];
		// if (v.N)
		// drawLine(x, y, x, y + 1);
		// if (v.S)
		// drawLine(x, y, x, y - 1);
		// if (v.W)
		// drawLine(x, y, x - 1, y);
		// if (v.E)
		// drawLine(x, y, x + 1, y);
		//
		// if (v.NW)
		// drawLine(x, y, x - 1, y + 1);
		// if (v.NE)
		// drawLine(x, y, x + 1, y + 1);
		// if (v.SW)
		// drawLine(x, y, x - 1, y - 1);
		// if (v.SE)
		// drawLine(x, y, x + 1, y - 1);
		// }
		// }
		// shapeRenderer.end();

		// Draw vertexes as points
		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.begin(ShapeType.Filled);

		for (int y = 0; y < graphHeight; y++) {
			for (int x = 0; x < graphWidth; x++) {
				shapeRenderer.circle(x * CELL_SIZE, y * CELL_SIZE, 1.5f);
			}
		}
		shapeRenderer.end();

	}

	private void drawLine(int ix, int iy, int ix2, int iy2) {
		shapeRenderer.line(ix * CELL_SIZE, iy * CELL_SIZE, 0, ix2 * CELL_SIZE, iy2 * CELL_SIZE, 0);
	}

	private void renderSearchResult() {
		// shapeRenderer.begin(ShapeType.Line);
		// shapeRenderer.setColor(Color.BLACK);
		// for (int i = 0; i < graphHeight; i++) {
		// for (int j = 0; j < graphWidth; j++) {
		// int vertexId = i * graphWidth + j;
		// if (pathing.ancestor[vertexId] >= 0) {
		// int ancestorId = pathing.ancestor[vertexId];
		// int prevIx = ancestorId % graphWidth;
		// int prevIy = ancestorId / graphWidth;
		// drawLine(prevIx, prevIy, j, i);
		// }
		// }
		// }
		// shapeRenderer.end();

		// Render Path

		renderPath(path, Color.FIREBRICK, false);
		renderPath(pathS1, Color.BLUE, true);
		

	}

	private void renderPath(Path path, Color color, boolean renderPoints) {
		float thickness = 2f;
		if (path != null) {
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(color);
			for (int i = 0; i < path.size() - 1; i++) {

				if (i == 1) {
					shapeRenderer.setColor(color);
				}

				if (i == path.size() - 2) {
					shapeRenderer.setColor(color);
				}
				Point v = path.get(i);
				Point next = path.get(i + 1);
				shapeRenderer.rectLine(v.x, v.y, next.x, next.y, thickness);

			}
			shapeRenderer.end();

			// Draw points
			float pointSize = 4f;
			shapeRenderer.setColor(Color.GREEN);
			shapeRenderer.begin(ShapeType.Filled);
			if (renderPoints) {
				for (Point p : path) {

					shapeRenderer.circle(p.x, p.y, pointSize);

				}
			}
			shapeRenderer.end();
			shapeRenderer.setColor(Color.BLACK);
			shapeRenderer.begin(ShapeType.Line);
			if (renderPoints) {
				for (Point p : path) {

					shapeRenderer.circle(p.x, p.y, pointSize);

				}
			}
			shapeRenderer.end();

		}
	}

	// Run on Map Load

	AStarSearch pathing;
	Path path;
	Path pathS1;
	PathSmoother smoother = new PathSmoother();

	private void onMapLoad() {

		// Create a unit
		u = new Unit(35, 20);
		units.add(u);

		// Search a path between two points

		pathing = new AStarSearch(graphWidth, graphHeight, graph, CELL_SIZE);

		path = orderUnit(u, CELL_SIZE * 22 + 10, CELL_SIZE * 15 + 20, map.polys);

	}

	private Path orderUnit(Unit u, float dx, float dy, ArrayList<Polygon> polys) {
		Path newPath = pathing.doAStar(u.x, u.y, dx, dy, polys);
		path = newPath;

		if (newPath != null) {
			newPath = smoother.smoothPath(newPath, polys);
			pathS1 = newPath;
			u.setPath(newPath);
		}
		return newPath;
	}

	// Unit Related

	ArrayList<Unit> units = new ArrayList<Unit>();

	Unit u; // the main unit we are using to demo pathfinding

	private void tickLogicForUnits() {
		for (Unit u : units) {
			u.tickLogic();
		}
	}

	private void moveUnits() {
		for (Unit u : units) {
			u.x += u.vx;
			u.y += u.vy;
		}
	}

	private void renderUnits() {
		for (Unit unit : units) {
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(Color.PURPLE);

			shapeRenderer.circle(unit.x, unit.y, 10);

			shapeRenderer.end();
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

		game.fileChooser.setDirectory(Holo.mapsDirectory);
	}

	private void loadMapFromDisk(String pathname) {
		try {
			Field loadedMap = HoloIO.getMapFromDisk(pathname);
			loadMap(loadedMap);
		} catch (HoloException e) {
			if (e.code == ErrorCode.IO_EXCEPTION) {
				System.out.println("IO Error, map not loaded");
				return;
			}
		}

	}

	private void loadMap(Field map) {
		System.out.println("New map loaded");
		this.map = map;
		map.hasUnsavedChanges = false;

		camera.position.set(map.width() / 2, map.height() / 2, 0);

		// Create pathing graph

		createGraph();
		long startTime = System.nanoTime();
		linearFillGraph();
		long endTime = System.nanoTime();
		long duration = (endTime - startTime); // divide by 1000000 to get milliseconds.
		System.out.format("Time elapsed: %d mililseconds%n", duration / 1000000);

		onMapLoad();
	}

	// Cursor Related
	private void renderCursor() {
		batch.setProjectionMatrix(fixedCam.combined);
		if (true) {
			batch.begin();
			Texture cursorImg = game.assets.get("icons/cursors/cursor.png", Texture.class);

			batch.draw(cursorImg, Gdx.input.getX(),
					Gdx.graphics.getHeight() - Gdx.input.getY() - cursorImg.getHeight());

			batch.end();
		}
	}

	// Input Related

	final int[] TRACKED_KEYS = new int[] { Keys.LEFT, Keys.RIGHT, Keys.UP, Keys.DOWN };
	KeyTracker keyTracker = new KeyTracker(TRACKED_KEYS, multiplexer);

	/* ^^^^^^ End of User Methods ^^^^^^ */

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
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		System.out.println(screenX + " " + screenY);
		if (button == Input.Buttons.LEFT && pointer == 0) {

			// Order the unit to move to the location using the path from A*
			Vector3 vec = new Vector3();
			vec = camera.unproject(vec.set(screenX, screenY, 0));

			System.out.println(vec.x + " " + vec.y);

			Path newPath = orderUnit(u, vec.x, vec.y, map.polys);

			return true;
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
