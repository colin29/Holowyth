package com.mygdx.holowyth.pathfinding;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.exception.ErrorCode;
import com.mygdx.holowyth.exception.HoloException;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.util.HoloIO;
import com.mygdx.holowyth.util.HoloUI;
import com.mygdx.holowyth.util.KeyTracker;
import com.mygdx.holowyth.util.Pair;
import com.mygdx.holowyth.util.constants.Holo;

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

	public PathfindingDemo(final Holowyth game) {
		this.game = game;
		this.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		stage = new Stage(new ScreenViewport());

		shapeRenderer = game.shapeRenderer;
		batch = game.batch;

		createUI();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		camera.update();

		// renderGraph();

		if (this.map != null) {
			renderMapPolygons();
			renderMapBoundaries();
		}
		
		// Rendering Test area;
		renderSearchResult();
		renderUnits();

		// UI
		stage.act(delta);
		stage.draw();

		displayTitleBarInformation();

		// Cursor related

		// User Controls

		// Testing area
//		pathing.stepAStar();
		

		int blank = 1;
		blank = blank + 1;
		
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
		System.out.println(Holo.mapsDirectory + "singleObstacle.map");
		loadMap(HoloIO.getMapFromDisk(Holo.mapsDirectory + "/singleObstacle.map"));
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

	// Pathfinding

	private int CELL_SIZE = 15;// 15; // size in pixels

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
				processVertex(graph[y][x], x, y);
			}
		}
	}

	private void processVertex(Vertex v, int ix, int iy) {
		int x = ix * CELL_SIZE;
		int y = iy * CELL_SIZE;
		// v.N = isPointWithinMap(ix+CELL_SIZE + );
		v.N = edgePathable(x, y, x, y + CELL_SIZE);
		v.S = edgePathable(x, y, x, y - CELL_SIZE);
		v.W = edgePathable(x, y, x - CELL_SIZE, y);
		v.E = edgePathable(x, y, x + CELL_SIZE, y);

		v.NW = edgePathable(x, y, x - CELL_SIZE, y + CELL_SIZE);
		v.NE = edgePathable(x, y, x + CELL_SIZE, y + CELL_SIZE);
		v.SW = edgePathable(x, y, x - CELL_SIZE, y - CELL_SIZE);
		v.SE = edgePathable(x, y, x + CELL_SIZE, y - CELL_SIZE);

		if (ix == 0)
			v.W = v.NW = v.SW = false;
		if (ix == graphWidth - 1)
			v.E = v.NE = v.SE = false;
		if (iy == 0)
			v.S = v.SW = v.SE = false;
		if (iy == graphHeight - 1)
			v.N = v.NW = v.NE = false;
	}

	private boolean edgePathable(int x, int y, int x2, int y2) {
		boolean intersects = false;
		for (Polygon polygon : map.polys) {
			for (int i = 0; i <= polygon.count - 2; i += 2) { // for each polygon edge
				if (Line2D.linesIntersect(x, y, x2, y2, polygon.vertexes[i], polygon.vertexes[i + 1],
						polygon.vertexes[(i + 2) % polygon.count], polygon.vertexes[(i + 3) % polygon.count])) {
					intersects = true;
				}
				// System.out.format("[(%d %d) (%d %d), (%f %f) (%f %f) %b]%n", x, y, x2, y2, polygon.vertexes[i],
				// polygon.vertexes[i + 1],
				// polygon.vertexes[(i + 2) % polygon.count], polygon.vertexes[(i + 3) % polygon.count],
				// Line2D.linesIntersect(x, y, x2, y2, polygon.vertexes[i], polygon.vertexes[i + 1],
				// polygon.vertexes[(i + 2) % polygon.count], polygon.vertexes[(i + 3) % polygon.count]));
			}
		}
		return !intersects;
	}

	private void renderGraph() {
		// Draw Edges
		shapeRenderer.setColor(Color.CORAL);
		shapeRenderer.begin(ShapeType.Line);
		for (int y = 0; y < graphHeight; y++) {
			for (int x = 0; x < graphWidth; x++) {
				Vertex v = graph[y][x];
				if (v.N)
					drawLine(x, y, x, y + 1);
				if (v.S)
					drawLine(x, y, x, y - 1);
				if (v.W)
					drawLine(x, y, x - 1, y);
				if (v.E)
					drawLine(x, y, x + 1, y);

				if (v.NW)
					drawLine(x, y, x - 1, y + 1);
				if (v.NE)
					drawLine(x, y, x + 1, y + 1);
				if (v.SW)
					drawLine(x, y, x - 1, y - 1);
				if (v.SE)
					drawLine(x, y, x + 1, y - 1);
			}
		}
		shapeRenderer.end();

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

	/**
	 * Converts a path from cell indexes to coordinates;
	 */
	private void scalePath(Path path) {
		for (Pair<Float, Float> p : path) {
			p = new Pair<Float, Float>(p.first() * CELL_SIZE, p.second() * CELL_SIZE);
		}
	}

	private void drawLine(int ix, int iy, int ix2, int iy2) {
		shapeRenderer.line(ix * CELL_SIZE, iy * CELL_SIZE, 0, ix2 * CELL_SIZE, iy2 * CELL_SIZE, 0);
	}

	private void drawRectLine(int ix, int iy, int ix2, int iy2, float width) {
		shapeRenderer.rectLine(ix * CELL_SIZE, iy * CELL_SIZE, ix2 * CELL_SIZE, iy2 * CELL_SIZE, width);
	}

	private void renderSearchResult() {
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.BLACK);
		for (int i = 0; i < graphHeight; i++) {
			for (int j = 0; j < graphWidth; j++) {
				int vertexId = i * graphWidth + j;
				if (pathing.ancestor[vertexId] >= 0) {
					int ancestorId = pathing.ancestor[vertexId];
					int prevIx = ancestorId % graphWidth;
					int prevIy = ancestorId / graphWidth;
					drawLine(prevIx, prevIy, j, i);
				}
			}
		}
		shapeRenderer.end();

		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.BLUE);
		for (int i = 0; i < path.size() - 1; i++) {
			Pair<Float, Float> v = path.get(i);
			Pair<Float, Float> next = path.get(i + 1);
			shapeRenderer.rectLine(v.first(), v.second(), next.first(), next.second(), 2);

		}
		shapeRenderer.end();
	}

	ArrayList<Unit> units = new ArrayList<Unit>();

	private void renderUnits() {
		for (Unit unit : units) {
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(Color.PURPLE);

			shapeRenderer.circle(unit.x, unit.y, 10);

			shapeRenderer.end();
		}

	}

	// UI and Disk

	private void createUI() {
		stage = new Stage(new ScreenViewport());

		root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		root.top().left();

		// Add Widgets here

		root.debug();

	}

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

	AStarSearch pathing;

	Path path;

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
	
	private void onMapLoad(){
		
		// Search a path between two points
		
		pathing = new AStarSearch(graphWidth, graphHeight, graph, CELL_SIZE);

		pathing.doAStar(0, 40 * 40);
		path = pathing.retrievePath();
		scalePath(path);

		// Create a unit
		
		Unit u = new Unit(0, 50);
		u.setPath(path);
		units.add(u);
	}
	
	private void tickLogicForUnits(){
		for(Unit u: units){
			u.tickLogic();
		}
	}
	private void moveUnits(){
		for(Unit u: units){
			u.x += u.vx;
			u.y += u.vy;
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
		// TODO Auto-generated method stub
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
