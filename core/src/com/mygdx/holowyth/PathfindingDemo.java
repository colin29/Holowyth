package com.mygdx.holowyth;

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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.pathfinding.AStarSearch;
import com.mygdx.holowyth.pathfinding.HoloPF;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.PathSmoother;
import com.mygdx.holowyth.pathfinding.Unit;
import com.mygdx.holowyth.pathfinding.Vertex;
import com.mygdx.holowyth.polygon.Polygons;
import com.mygdx.holowyth.util.HoloGL;
import com.mygdx.holowyth.util.HoloIO;
import com.mygdx.holowyth.util.HoloUI;
import com.mygdx.holowyth.util.constants.Holo;
import com.mygdx.holowyth.util.data.Coord;
import com.mygdx.holowyth.util.data.Point;
import com.mygdx.holowyth.util.data.Segment;
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
	
	AStarSearch pathing;
	PathSmoother smoother = new PathSmoother();

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
			renderExpandedPolygons();
			renderMapPolygons();
			
			renderMapBoundaries();
		}

		// Rendering Test area;
//		renderPaths();
		renderPathEndPoint(pathSmoothed, Color.GREEN);
		renderUnits();

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
		
		//Testing area
		
//		System.out.format("Unit xy: (%s, %s) %s %n", playerUnit.x, playerUnit.y, playerUnit.curSpeed);
	}

	private void renderMapPolygons() {
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.setColor(Color.BLACK);
		HoloGL.renderPolygons(map.polys, shapeRenderer);
	}
	private void renderExpandedPolygons() {
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.setColor(Color.GRAY);
		HoloGL.renderPolygons(expandedPolys, shapeRenderer);
	}
	

	private void renderMapBoundaries() {
		shapeRenderer.setProjectionMatrix(camera.combined);
		HoloGL.renderMapBoundaries(map, shapeRenderer);
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
	Window testing;
	private void createUI() {
		stage = new Stage(new ScreenViewport());

		root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		root.top().left();

		// Add Widgets here

		root.debug();
		
		// Create a table for adjusting parameters
		
		testing = new Window("Parameters", skin);
		testing.setPosition(700, 400);

		
//		root.add(new TextButton("test", skin));
		stage.addActor(testing);
		
		
		HoloUI.parameterSlider(0, 0.04f, "linearAccel", testing, skin, (Float f) -> playerUnit.linearAccelRate = f);
		HoloUI.parameterSlider(0, Holo.defaultUnitMoveSpeed, "initialMoveSpeed", testing, skin, (Float f) -> playerUnit.initialMoveSpeed = f);
		testing.pack();
		
		
	}

	// Graph Construction (For pathfinding)

	private int CELL_SIZE = Holo.CELL_SIZE;
	Vertex[][] graph;
	int graphWidth, graphHeight;

	private void createGraph() {
		graphWidth = (int) Math.floor(map.width() / CELL_SIZE) + 1;
		graphHeight = (int) Math.floor(map.height() / CELL_SIZE) + 1;

		graph = new Vertex[graphHeight][graphWidth];
	}


	private void floodFillGraph() {
		
		//initialize graph first
		for (int y = 0; y < graphHeight; y++) {
			for (int x = 0; x < graphWidth; x++) {
				graph[y][x] = new Vertex();
			}
		}
		
		// Start with 0,0

		Queue<Coord> q = new Queue<Coord>();
		q.ensureCapacity(graphWidth); // for a graph size x * y, you'd expect max entries on the order of max(x, y)

		q.addLast(new Coord(0,0));

		Coord c;
		Vertex v;
		Vertex suc;
		while (q.size > 0) {
			c = q.removeFirst();
			v = graph[c.y][c.x];
			
			v.reachable =  true;
			fillInVertex(v, c.x, c.y);

			
			if(v.N && !(suc = graph[c.y+1][c.x]).reachable){
				q.addLast(new Coord(c.x, c.y+1));
				suc.reachable = true;
			}
			
			if(v.S && !(suc = graph[c.y-1][c.x]).reachable){ //really hate to do this formatting but...
				q.addLast(new Coord(c.x, c.y-1));
				suc.reachable = true;
			}
			
			if(v.W && !(suc =graph[c.y][c.x-1]).reachable){
				q.addLast(new Coord(c.x-1, c.y));
				suc.reachable = true;
			}
			
			if(v.E && !(suc =graph[c.y][c.x+1]).reachable){
				q.addLast(new Coord(c.x+1, c.y));
				suc.reachable = true;
			}
			
			
			if(v.NW && !(suc =graph[c.y+1][c.x-1]).reachable){
				q.addLast(new Coord(c.x-1, c.y+1));
				suc.reachable = true;
			}
			
			if(v.NE && !(suc =graph[c.y+1][c.x+1]).reachable){
				q.addLast(new Coord(c.x+1, c.y+1));
				suc.reachable = true;
			}
			
			if(v.SW && !(suc = graph[c.y-1][c.x-1]).reachable){
				q.addLast(new Coord(c.x-1, c.y-1));
				suc.reachable = true;
			}
			
			if(v.SE && !(suc = graph[c.y-1][c.x+1]).reachable){
				q.addLast(new Coord(c.x+1, c.y-1));
				suc.reachable = true;
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
		return HoloPF.isEdgePathable(x, y, x2, y2, expandedPolys);
	}

	private void renderGraph() {
		// // Draw Edges
//		shapeRenderer.setColor(Color.CORAL);
//		shapeRenderer.begin(ShapeType.Line);
//		for (int y = 0; y < graphHeight; y++) {
//			for (int x = 0; x < graphWidth; x++) {
//				Vertex v = graph[y][x];
//				if (v.N)
//					drawLine(x, y, x, y + 1);
//				if (v.S)
//					drawLine(x, y, x, y - 1);
//				if (v.W)
//					drawLine(x, y, x - 1, y);
//				if (v.E)
//					drawLine(x, y, x + 1, y);
//
//				if (v.NW)
//					drawLine(x, y, x - 1, y + 1);
//				if (v.NE)
//					drawLine(x, y, x + 1, y + 1);
//				if (v.SW)
//					drawLine(x, y, x - 1, y - 1);
//				if (v.SE)
//					drawLine(x, y, x + 1, y - 1);
//			}
//		}
//		shapeRenderer.end();

		// Draw vertexes as points
		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.begin(ShapeType.Filled);

		for (int y = 0; y < graphHeight; y++) {
			for (int x = 0; x < graphWidth; x++) {
				if(graph[y][x].reachable){
					shapeRenderer.circle(x * CELL_SIZE, y * CELL_SIZE, 1f);
				}
				
			}
		}
		shapeRenderer.end();

	}
	
	// Expanded Geometry
	
	private Polygons expandPolygons(Polygons polys){
		return HoloPF.expandPolygons(polys, UNIT_RADIUS);
	}
	
	// Rendering Paths
	private void renderPaths() {
		// Render Path
		// smoother.render(shapeRenderer);
		renderPath(pathSmoothed, Color.BLUE, false);
	}

	float pathThickness = 2f;
	private void renderPath(Path path, Color color, boolean renderPoints) {
		HoloPF.renderPath(path, color, renderPoints, pathThickness, shapeRenderer);
	}
	
	private void renderPathEndPoint(Path path, Color color){
		Point finalPoint = pathSmoothed.get(pathSmoothed.size()-1); 
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(color);
		shapeRenderer.circle(finalPoint.x, finalPoint.y, 4f);
		shapeRenderer.end();
		
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.circle(finalPoint.x, finalPoint.y, 4f);
		shapeRenderer.end();
	}

	//*** Run on Map Load (Important!) ***//

	Path pathSmoothed;
	Polygons expandedPolys = new Polygons();
	private void mapStartup() {
		expandedPolys = expandPolygons(map.polys);
		// Create pathing graph
		createGraph();
		// long startTime = System.nanoTime();
		floodFillGraph();
		// long endTime = System.nanoTime();
		// long duration = (endTime - startTime); // divide by 1000000 to get milliseconds.
		// System.out.format("Time elapsed: %d mililseconds%n", duration / 1000000);
		
		// Create a unit
		playerUnit = new Unit(35, 20);
		units.add(playerUnit);

		// Search a path between two points

		pathing = new AStarSearch(graphWidth, graphHeight, graph, CELL_SIZE);

		orderUnit(playerUnit, CELL_SIZE * 22 + 10, CELL_SIZE * 15 + 20);

	}

	
	// Unit Related

	ArrayList<Unit> units = new ArrayList<Unit>();
	Unit playerUnit; // the main unit we are using to demo pathfinding

	private Path orderUnit(Unit u, float dx, float dy) {
		Path newPath = pathing.doAStar(u.x, u.y, dx, dy, expandedPolys);

		if (newPath != null) {
			this.pathSmoothed = smoother.smoothPath(newPath, expandedPolys);
			u.setPath(pathSmoothed);
		}
		return newPath;
	}

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

	private float UNIT_RADIUS = 10;
	private void renderUnits() {
		for (Unit unit : units) {
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(Color.PURPLE);

			shapeRenderer.circle(unit.x, unit.y, UNIT_RADIUS);

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



		mapStartup();
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
		
		
//		System.out.println("Touch: " + screenX + " " + screenY);
		if (button == Input.Buttons.LEFT && pointer == 0) {

			// Order the unit to move to the location using the path from A*
			Vector3 vec = new Vector3();

			
			
			vec = camera.unproject(vec.set(screenX, screenY, 0));
//			if(testing.hit(vec.x, vec.y, true) != null){// if this is the case, then some stage actor is blocking the click and we should act like that
//				return false;
//			}
					
			orderUnit(playerUnit, vec.x, vec.y);
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
