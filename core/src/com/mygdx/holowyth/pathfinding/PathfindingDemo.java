package com.mygdx.holowyth.pathfinding;

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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.polygon.Polygons;
import com.mygdx.holowyth.util.HoloGL;
import com.mygdx.holowyth.util.HoloIO;
import com.mygdx.holowyth.util.HoloUI;
import com.mygdx.holowyth.util.constants.Holo;
import com.mygdx.holowyth.util.data.Coord;
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

		// renderGraph();
		renderDynamicGraph(true);

		if (this.map != null) {
			// renderExpandedPolygons();
			renderMapPolygons();

			renderMapBoundaries();
		}

		// Rendering Test area;
		renderPaths();
		renderPathEndPoint(pathSmoothed, Color.GREEN);
		renderUnits();

		HoloGL.renderPolygons(expandedMapPolys, shapeRenderer, Color.GRAY);

		// UI
		stage.act(delta);
		stage.draw();

		displayTitleBarInformation();

		// Cursor related
		renderCursor();

		// User Controls

		// Testing area

//		 for (Vertex v : nearbyPathable) {
//		 HoloGL.renderCircle(v.ix * CELL_SIZE, v.iy * CELL_SIZE, shapeRenderer, Color.RED);
//		 }
		
		//render expanded hit bodies
		for (Unit u: units){
			HoloGL.renderEmptyCircle(u.x, u.y, u.getRadius() + Holo.UNIT_RADIUS, shapeRenderer, Color.GRAY);
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
		HoloGL.renderPolygons(map.polys, shapeRenderer);
	}

	private void renderExpandedPolygons() {
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.setColor(Color.GRAY);
		HoloGL.renderPolygons(expandedMapPolys, shapeRenderer);
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
		if (Holo.editorInitialMap != null) {
			loadMap(HoloIO.getMapFromDisk(Holo.mapsDirectory + Holo.editorInitialMap));
		}
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

		// createParameterWindow();

		root.debug();

		createCoordinateText();
	}

	Label coordInfo;

	private void createParameterWindow() {
		// Create a table for adjusting parameters

		testing = new Window("Parameters", skin);
		testing.setPosition(700, 400);

		// root.add(new TextButton("test", skin));
		stage.addActor(testing);
		HoloUI.parameterSlider(0, 0.04f, "linearAccel", testing, skin, (Float f) -> playerUnit.linearAccelRate = f);
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

	// Graph Construction (For pathfinding)

	private int CELL_SIZE = Holo.CELL_SIZE;
	Vertex[][] graph;
	int graphWidth, graphHeight;
	Vertex[][] dynamicGraph;

	private void createGraph() {
		graphWidth = (int) Math.floor(map.width() / CELL_SIZE) + 1;
		graphHeight = (int) Math.floor(map.height() / CELL_SIZE) + 1;

		graph = new Vertex[graphHeight][graphWidth];
		for (int y = 0; y < graphHeight; y++) {
			for (int x = 0; x < graphWidth; x++) {
				graph[y][x] = new Vertex(x, y);
			}
		}
		dynamicGraph = new Vertex[graphHeight][graphWidth];
		for (int y = 0; y < graphHeight; y++) {
			for (int x = 0; x < graphWidth; x++) {
				dynamicGraph[y][x] = new Vertex(x, y);
			}
		}

	}

	private void floodFillGraph() {

		// Start with 0,0

		Queue<Coord> q = new Queue<Coord>();
		q.ensureCapacity(graphWidth); // for a graph size x * y, you'd expect max entries on the order of max(x, y)

		q.addLast(new Coord(0, 0));

		Coord c;
		Vertex v;
		Vertex suc;
		while (q.size > 0) {
			c = q.removeFirst();
			v = graph[c.y][c.x];

			v.reachable = true;
			fillInVertex(v, c.x, c.y, expandedMapPolys);

			if (v.N && !(suc = graph[c.y + 1][c.x]).reachable) {
				q.addLast(new Coord(c.x, c.y + 1));
				suc.reachable = true;
			}

			if (v.S && !(suc = graph[c.y - 1][c.x]).reachable) { // really hate to do this formatting but...
				q.addLast(new Coord(c.x, c.y - 1));
				suc.reachable = true;
			}

			if (v.W && !(suc = graph[c.y][c.x - 1]).reachable) {
				q.addLast(new Coord(c.x - 1, c.y));
				suc.reachable = true;
			}

			if (v.E && !(suc = graph[c.y][c.x + 1]).reachable) {
				q.addLast(new Coord(c.x + 1, c.y));
				suc.reachable = true;
			}

			if (v.NW && !(suc = graph[c.y + 1][c.x - 1]).reachable) {
				q.addLast(new Coord(c.x - 1, c.y + 1));
				suc.reachable = true;
			}

			if (v.NE && !(suc = graph[c.y + 1][c.x + 1]).reachable) {
				q.addLast(new Coord(c.x + 1, c.y + 1));
				suc.reachable = true;
			}

			if (v.SW && !(suc = graph[c.y - 1][c.x - 1]).reachable) {
				q.addLast(new Coord(c.x - 1, c.y - 1));
				suc.reachable = true;
			}

			if (v.SE && !(suc = graph[c.y - 1][c.x + 1]).reachable) {
				q.addLast(new Coord(c.x + 1, c.y - 1));
				suc.reachable = true;
			}
		}
	}

	/**
	 * Calculates the pathing information for a single vertex
	 */
	private void fillInVertex(Vertex v, int ix, int iy, Polygons polys) {
		int x = ix * CELL_SIZE;
		int y = iy * CELL_SIZE;
		// v.N = isPointWithinMap(ix+CELL_SIZE + );
		v.N = isEdgePathable(x, y, x, y + CELL_SIZE, polys);
		v.S = isEdgePathable(x, y, x, y - CELL_SIZE, polys);
		v.W = isEdgePathable(x, y, x - CELL_SIZE, y, polys);
		v.E = isEdgePathable(x, y, x + CELL_SIZE, y, polys);

		v.NW = isEdgePathable(x, y, x - CELL_SIZE, y + CELL_SIZE, polys);
		v.NE = isEdgePathable(x, y, x + CELL_SIZE, y + CELL_SIZE, polys);
		v.SW = isEdgePathable(x, y, x - CELL_SIZE, y - CELL_SIZE, polys);
		v.SE = isEdgePathable(x, y, x + CELL_SIZE, y - CELL_SIZE, polys);

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
	 * Given a vertex, restrict it's pathability based on the expanded collision circle of the unit
	 * @param unit Radius of the radius of the unit which is pathing. Used to get expanded geometry. 
	 */
	public void restrictVertex(Vertex v, CBInfo cb, float unitRadius) {
		float x = v.ix * CELL_SIZE;
		float y = v.iy * CELL_SIZE;
		
		//if the vertex is inside the unit's expanded pathing body, we can immediately block it.
		
		Point p1 = new Point(x, y);
		Point p2 = new Point(cb.x, cb.y);
		float dist = Point.calcDistance(p1, p2);
		
		float expandedRadius = cb.unitRadius + unitRadius;
		float radSquared = expandedRadius * expandedRadius;
		
		if(dist < expandedRadius){
			v.block();
			return;
		}
		
		//If it's outside, we still have to check its edges to block the right ones.
		
		//an edge is pathable if it's closest distance to the center of the circle is equal or greater to than the expanded radius

		v.N = v.N && (Line2D.ptSegDistSq(x, y, x, y + CELL_SIZE, cb.x, cb.y) >= radSquared);
		v.S = v.S && (Line2D.ptSegDistSq(x, y, x, y - CELL_SIZE, cb.x, cb.y) >= radSquared);
		v.W = v.W && (Line2D.ptSegDistSq(x, y, x - CELL_SIZE, y, cb.x, cb.y) >= radSquared);
		v.E = v.E && (Line2D.ptSegDistSq(x, y, x + CELL_SIZE, y, cb.x, cb.y) >= radSquared);

		v.NW = v.NW && (Line2D.ptSegDistSq(x, y, x - CELL_SIZE, y + CELL_SIZE, cb.x, cb.y) >= radSquared);
		v.NE = v.NE && (Line2D.ptSegDistSq(x, y, x + CELL_SIZE, y + CELL_SIZE, cb.x, cb.y) >= radSquared);
		v.SW = v.SW && (Line2D.ptSegDistSq(x, y, x - CELL_SIZE, y - CELL_SIZE, cb.x, cb.y) >= radSquared);
		v.SE = v.SE && (Line2D.ptSegDistSq(x, y, x + CELL_SIZE, y - CELL_SIZE, cb.x, cb.y) >= radSquared);
	}

	/**
	 * @return Whether the given line segment is pathable or not.
	 */
	private boolean isEdgePathable(float x, float y, float x2, float y2, Polygons polys) {
		return HoloPF.isEdgePathable(x, y, x2, y2, polys);
	}

	private void renderGraph() { // Draw Edges
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
				if (graph[y][x].reachable) {
					shapeRenderer.circle(x * CELL_SIZE, y * CELL_SIZE, 1f);
				}

			}
		}
		shapeRenderer.end();

	}

	private void renderDynamicGraph(boolean renderEdges) {

		if (renderEdges) {
			// Draw Edges
			shapeRenderer.setColor(Color.CORAL);
			shapeRenderer.begin(ShapeType.Line);
			for (int y = 0; y < graphHeight; y++) {
				for (int x = 0; x < graphWidth; x++) {
					Vertex v = dynamicGraph[y][x];
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
		}

		// Draw vertexes as points
		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.begin(ShapeType.Filled);

		for (int y = 0; y < graphHeight; y++) {
			for (int x = 0; x < graphWidth; x++) {
				if (dynamicGraph[y][x].reachable) {
					shapeRenderer.circle(x * CELL_SIZE, y * CELL_SIZE, 1f);
				}

			}
		}
		shapeRenderer.end();

	}

	private void drawLine(int ix, int iy, int ix2, int iy2) {
		shapeRenderer.line(ix * CELL_SIZE, iy * CELL_SIZE, 0, ix2 * CELL_SIZE, iy2 * CELL_SIZE, 0);
	}

	// Expanded Geometry

	private Polygons expandPolygons(Polygons polys) {
		return HoloPF.expandPolygons(polys, Holo.UNIT_RADIUS);
	}

	// Rendering Paths
	private void renderPaths() {
		// Render Path
		smoother.render(shapeRenderer);
		renderPath(pathSmoothed, Color.BLUE, false);
	}

	float pathThickness = 2f;

	private void renderPath(Path path, Color color, boolean renderPoints) {
		HoloPF.renderPath(path, color, renderPoints, pathThickness, shapeRenderer);
	}

	private void renderPathEndPoint(Path path, Color color) {
		if (pathSmoothed != null) {
			Point finalPoint = pathSmoothed.get(pathSmoothed.size() - 1);
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

	// *** Run on Map Load (Important!) ***//

	Path pathSmoothed; // for demo use
	Polygons expandedMapPolys = new Polygons();

	private void mapStartup() {
		expandedMapPolys = expandPolygons(map.polys);
		// Create pathing graph
		createGraph();
		// long startTime = System.nanoTime();
		floodFillGraph();
		// long endTime = System.nanoTime();
		// long duration = (endTime - startTime); // divide by 1000000 to get milliseconds.
		// System.out.format("Time elapsed: %d mililseconds%n", duration / 1000000);

		// Create units
		playerUnit = new Unit(35, 20);
		units.add(playerUnit);

		createTestUnits();

		// Search a path between two points

		pathing = new AStarSearch(graphWidth, graphHeight, graph, CELL_SIZE, map.width(), map.height());

		orderMoveTo(playerUnit, CELL_SIZE * 22 + 10, CELL_SIZE * 15 + 20);

	}

	// Unit Related

	ArrayList<Unit> units = new ArrayList<Unit>();
	Unit playerUnit; // the main unit we are using to demo pathfinding

	private void createTestUnits() {
		units.add(new Unit(406, 253));
		units.add(new Unit(550, 122 - Holo.UNIT_RADIUS));
		units.add(new Unit(750, 450));
	}

	private void orderMoveTo(Unit u, float dx, float dy) {
		Path path = findPathForUnit(u, dx, dy);
		if (path != null) {
			u.setPath(path);
			if (u == playerUnit) {
				this.pathSmoothed = path;
			}
		}
	}

	private Path findPathForUnit(Unit unit, float dx, float dy) {

		// For pathfinding, need to get expanded geometry of unit collision bodies as well

		ArrayList<CBInfo> colBodies = new ArrayList<CBInfo>();

		for (Unit a : units) {
			if (unit.equals(a)) { // don't consider the unit's own collision body
				continue;
			}

			CBInfo c = new CBInfo();
			c.x = a.x;
			c.y = a.y;
			c.unitRadius = Holo.UNIT_RADIUS;
			colBodies.add(c);
		}
		
		// Generate dynamic graph;

		// Start with the base graph
		for (int y = 0; y < graphHeight; y++) {
			for (int x = 0; x < graphWidth; x++) {
				dynamicGraph[y][x].set(graph[y][x]);
			}
		}

		// modify it for every unit
		for (int y = 0; y < graphHeight; y++) {
			for (int x = 0; x < graphWidth; x++) {
				dynamicGraph[y][x].set(graph[y][x]);
			}
		}

		revertDynamicGraph();
		setDynamicGraph(colBodies, unit);
		Path newPath = pathing.doAStar(unit.x, unit.y, dx, dy, expandedMapPolys, colBodies, dynamicGraph, unit.getRadius()); // use the dynamic graph

		if (newPath != null) {
			return smoother.smoothPath(newPath, expandedMapPolys, colBodies, unit.getRadius());
		}
		return newPath;
	}

	ArrayList<Vertex> prospects;
	ArrayList<Vertex> blocked = new ArrayList<Vertex>();

	/**
	 * Modifies the graph by restricting vertices according to the additional
	 * 
	 * @param infos
	 *            List of the expanded polygons of units, excluding the pathing unit, with some extra information.
	 * @param u
	 *            The pathing unit
	 */
	private void setDynamicGraph(ArrayList<CBInfo> infos, Unit u) {

		for (CBInfo cb : infos) {
			prospects = new ArrayList<Vertex>();
			float x1, x2, y1, y2; // boundaries of the bounding box of the expanded colliding body
			x1 = cb.x - cb.unitRadius - u.getRadius();
			x2 = cb.x + cb.unitRadius + u.getRadius();
			y1 = cb.y - cb.unitRadius - u.getRadius();
			y2 = cb.y + cb.unitRadius + u.getRadius();

			int upperIndexX = (int) Math.ceil(x2 / CELL_SIZE);
			int upperIndexY = (int) Math.ceil(y2 / CELL_SIZE);

			for (int ix = (int) (x1 / CELL_SIZE); ix <= upperIndexX; ix += 1) {
				for (int iy = (int) (y1 / CELL_SIZE); iy <= upperIndexY; iy += 1) {
					if (HoloPF.isIndexInMap(ix, iy, graphWidth, graphHeight))
						prospects.add(dynamicGraph[iy][ix]);
				}
			}

			// System.out.println("prospects: " + prospects.size());

			for (Vertex v : prospects) {
				restrictVertex(v, cb, u.getRadius());
			}

		}

	}

	/**
	 * Reverts the dynamic graph back to the orignal graph. The current method is simple brute-force.
	 */
	private void revertDynamicGraph() {
		for (int y = 0; y < graphHeight; y++) { // change later to use a stack of modifications or something.
			for (int x = 0; x < graphWidth; x++) {
				dynamicGraph[y][x].set(graph[y][x]);
			}
		}
	}

	/**
	 * When a unit's collision body is factored in to the dynamic graph, any points within the expanded geometry need to
	 * be blocked. We use floodfill. For simplicity in finding a first vertex, we assumes that minimum unit radius *
	 * SQRT2 > cell_width, that the vertex to the left and down will be inside the unit's expanded geometry
	 */
	private void blockVertexesWithinUnit() {
		// TODO:
	}

	private void tickLogicForUnits() {
		for (Unit u : units) {
			u.tickLogic();
		}
	}

	private void moveUnits() {
		for (Unit u : units) {
			u.move();
		}
	}

	private void renderUnits() {
		for (Unit unit : units) {
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
		for (Unit unit : units) {
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(Color.BLACK);
			shapeRenderer.circle(unit.x, unit.y, Holo.UNIT_RADIUS);
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

	ArrayList<Vertex> nearbyPathable = new ArrayList<Vertex>();

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {

		// System.out.println("Touch: " + screenX + " " + screenY);
		if (button == Input.Buttons.LEFT && pointer == 0) {

			// Order the unit to move to the location using the path from A*
			Vector3 vec = new Vector3();

			vec = camera.unproject(vec.set(screenX, screenY, 0));
			// if(testing.hit(vec.x, vec.y, true) != null){// if this is the case, then some stage actor is blocking the
			// click and we should act like that
			// return false;
			// }

			orderMoveTo(playerUnit, vec.x, vec.y);

			// Testing area

			nearbyPathable = HoloPF.findNearbyReachableVertexes(new Point(vec.x, vec.y), dynamicGraph, graphWidth,
					graphHeight, 2);
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

		// update coordInfo
		Vector3 vec = new Vector3();
		vec = camera.unproject(vec.set(screenX, screenY, 0));
		coordInfo.setText("(" + (int) (vec.x) + ", " + (int) (vec.y) + ")\n" + "(" + (int) (vec.x) / CELL_SIZE + ", "
				+ (int) (vec.y) / CELL_SIZE + ")");
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
