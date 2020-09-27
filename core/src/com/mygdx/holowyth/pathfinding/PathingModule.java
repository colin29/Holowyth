package com.mygdx.holowyth.pathfinding;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.mygdx.holowyth.map.obstacledata.OrientedPoly;
import com.mygdx.holowyth.map.obstacledata.OrientedSeg;
import com.mygdx.holowyth.map.simplemap.SimpleMap;
import com.mygdx.holowyth.pathfinding.PathSmoother.PathsInfo;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Coord;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * Handles pathfinding for the app's needs <br>
 * Knows how to render certain information about itself
 * 
 * Also exposes extracted obstacle information about the loaded map
 * 
 * Has application lifetime.
 * 
 * @author Colin Ta
 *
 */
public class PathingModule {

	// Rendering and pipeline variables
	OrthographicCamera camera;
	ShapeRenderer shapeRenderer;

	private AStarSearch astar;

	private PathSmoother smoother = new PathSmoother();

	// Map lifetime info
	private int mapWidth;
	private int mapHeight;

	private final List<Point> obstaclePoints = new ArrayList<Point>();
	private final List<OrientedSeg> obstacleSegs = new ArrayList<OrientedSeg>();
	private final List<OrientedSeg> obstacleExpandedSegs = new ArrayList<OrientedSeg>();

	// Debug (map-lifetime)
	HashMap<UnitPF, PathsInfo> intermediatePaths;

	/**
	 * @Lifetime can be from app start to app shutdown. Call initFormap() whenever a new map is loaded.
	 * @param camera
	 * @param shapeRenderer
	 */
	public PathingModule(OrthographicCamera camera, ShapeRenderer shapeRenderer) {
		this.camera = camera;
		this.shapeRenderer = shapeRenderer;

	}

	/*
	 * Re-inits for the given map
	 */
	public void initForSimpleMap(SimpleMap map) {
		mapWidth = map.width();
		mapHeight = map.height();

		List<OrientedPoly> polys = OrientedPoly.calculateOrientedPolygons(map.polys);
		initObstaclePoints(polys);
		initExpandedObstacleSegs(polys);
		addCollisionForMapBoundary();

		initCommonItems();
	}

	/*
	 * Re-inits for the given map.
	 */
	public void initForTiledMap(TiledMap map, int width, int height) {
		mapWidth = width;
		mapHeight = height;

		readObstaclesFromTiledMap(map);
		addCollisionForMapBoundary();

		initCommonItems();

	}

	/** Init items that don't vary based on map source */
	private void initCommonItems() {
		initGraphs();
		floodFillGraph();

		astar = new AStarSearch(graphWidth, graphHeight, graph, CELL_SIZE, mapWidth, mapHeight);

		intermediatePaths = new HashMap<UnitPF, PathsInfo>();
	}

	private void readObstaclesFromTiledMap(TiledMap map) {

		obstaclePoints.clear();
		obstacleSegs.clear();
		obstacleExpandedSegs.clear();

		var collisionLayer = map.getLayers().get("Collision");

		MapObjects objects = collisionLayer.getObjects();

		for (PolylineMapObject object : objects.getByType(PolylineMapObject.class)) {

			Polyline polyline = object.getPolyline();
			float[] vertices = polyline.getTransformedVertices();

			Vector2 end = new Vector2(vertices[0], vertices[1]);
			Vector2 start = new Vector2();

			// i represent the current line segment (for example with 6 floats, there are 3 points, and 2 total
			// line segments)
			for (int i = 1; i < vertices.length / 2; i += 1) {

				start.set(end);
				end.set(vertices[i * 2], vertices[i * 2 + 1]);

				var seg = new OrientedSeg(start.x, start.y, end.x, end.y);
				seg.isClockwise = false; // In the editor we draw segs so that right is the "outside".

				if (i == 1) {
					obstaclePoints.add(new Point(start.x, start.y));
				}
				obstaclePoints.add(new Point(end.x, end.y));
				obstacleSegs.add(seg);
				obstacleExpandedSegs.add(seg.getOutwardlyDisplacedSegment(Holo.UNIT_RADIUS));
			}

		}

	}

	private void addCollisionForMapBoundary() {

		// Need to define these in CCW direction, pathable side is to the right
		var left = new OrientedSeg(0, 0, 0, mapHeight);
		var top = new OrientedSeg(0, mapHeight, mapWidth, mapHeight);
		var right = new OrientedSeg(mapWidth, mapHeight, mapWidth, 0);
		var bottom = new OrientedSeg(mapWidth, 0, 0, 0);

		var segs = new ArrayList<OrientedSeg>();
		segs.add(bottom);
		segs.add(right);
		segs.add(top);
		segs.add(left);

		obstacleSegs.addAll(segs);

		for (var seg : segs) {
			obstaclePoints.add(seg.startPoint());
			obstacleExpandedSegs.add(seg.getOutwardlyDisplacedSegment(Holo.UNIT_RADIUS));
		}
	}

	public void onMapClose() {
		mapWidth = 0;
		mapHeight = 0;

		obstaclePoints.clear();
		obstacleSegs.clear();
		obstacleExpandedSegs.clear();

		intermediatePaths.clear();

	}

	private void initObstaclePoints(List<OrientedPoly> polys) {
		obstaclePoints.clear();
		for (OrientedPoly poly : polys) {
			for (var seg : poly.segments) {
				obstaclePoints.add(seg.startPoint());
			}
		}
	}

	private void initExpandedObstacleSegs(List<OrientedPoly> polys) {
		obstacleExpandedSegs.clear();
		for (OrientedPoly poly : polys) {
			obstacleExpandedSegs.addAll(poly.segments);
		}
	}

	// Graph construction
	private int CELL_SIZE = Holo.CELL_SIZE;
	Vertex[][] graph;
	int graphWidth, graphHeight;
	Vertex[][] dynamicGraph;

	// Unit pathfinding

	public Path findPathForUnit(UnitPF unit, float dx, float dy, List<? extends UnitPF> allUnits) {

		// For pathfinding, need to get expanded geometry of unit collision bodies as well

		ArrayList<CBInfo> colBodies = new ArrayList<CBInfo>();

		for (UnitPF u : allUnits) {
			if (unit.equals(u)) { // don't consider the unit's own collision body
				continue;
			}

			CBInfo c = new CBInfo();
			c.x = u.getX();
			c.y = u.getY();
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
		if (!Holo.debugPathfindingIgnoreUnits) {
			setDynamicGraph(colBodies, unit);
		}
		Path newPath = astar.doAStar(unit.getX(), unit.getY(), dx, dy, obstacleExpandedSegs, obstaclePoints, colBodies,
				dynamicGraph, unit.getRadius()); // use the dynamic graph

		if (newPath != null) {
			Path finalPath = smoother.smoothPath(newPath, obstacleExpandedSegs, obstaclePoints, colBodies,
					unit.getRadius());
			// PathsInfo info = smoother.getPathInfo();
			// intermediatePaths.put(unit, info);
			return finalPath;
		} else {
			return null;
		}
	}

	// Getters

	private void initGraphs() {
		graphWidth = (int) Math.floor(mapWidth / CELL_SIZE) + 1;
		graphHeight = (int) Math.floor(mapHeight / CELL_SIZE) + 1;

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
		visited = new boolean[graphHeight][graphWidth];
	}

	private void clearVisited() {
		for (int y = 0; y < graphHeight; y++) {
			for (int x = 0; x < graphWidth; x++) {
				visited[y][x] = false;
			}
		}
	}

	private void floodFillGraph() {

		Queue<Coord> q = new Queue<Coord>();
		q.ensureCapacity(graphWidth);

		q.addLast(new Coord(2, 2)); // start flood fill from this position

		Coord c;
		Vertex vertex;
		Vertex suc;
		while (q.size > 0) {
			c = q.removeFirst();
			vertex = graph[c.y][c.x];

			vertex.reachable = true;
			fillInVertex(vertex, c.x, c.y);

			if (vertex.N && !(suc = graph[c.y + 1][c.x]).reachable) {
				q.addLast(new Coord(c.x, c.y + 1));
				suc.reachable = true;
			}

			if (vertex.S && !(suc = graph[c.y - 1][c.x]).reachable) {
				q.addLast(new Coord(c.x, c.y - 1));
				suc.reachable = true;
			}

			if (vertex.W && !(suc = graph[c.y][c.x - 1]).reachable) {
				q.addLast(new Coord(c.x - 1, c.y));
				suc.reachable = true;
			}

			if (vertex.E && !(suc = graph[c.y][c.x + 1]).reachable) {
				q.addLast(new Coord(c.x + 1, c.y));
				suc.reachable = true;
			}

			if (vertex.NW && !(suc = graph[c.y + 1][c.x - 1]).reachable) {
				q.addLast(new Coord(c.x - 1, c.y + 1));
				suc.reachable = true;
			}

			if (vertex.NE && !(suc = graph[c.y + 1][c.x + 1]).reachable) {
				q.addLast(new Coord(c.x + 1, c.y + 1));
				suc.reachable = true;
			}

			if (vertex.SW && !(suc = graph[c.y - 1][c.x - 1]).reachable) {
				q.addLast(new Coord(c.x - 1, c.y - 1));
				suc.reachable = true;
			}

			if (vertex.SE && !(suc = graph[c.y - 1][c.x + 1]).reachable) {
				q.addLast(new Coord(c.x + 1, c.y - 1));
				suc.reachable = true;
			}
		}
	}

	/**
	 * Calculates the pathing information for a single vertex
	 */
	private void fillInVertex(Vertex vertex, int indexX, int indexY) {
		float x = indexX * CELL_SIZE;
		float y = indexY * CELL_SIZE;
		// v.N = isPointWithinMap(ix+CELL_SIZE + );

		float unitRadius = Holo.UNIT_RADIUS;
		vertex.N = HoloPF.isSegmentPathableAgainstObstacles(x, y, x, y + CELL_SIZE, obstacleExpandedSegs,
				obstaclePoints, unitRadius);
		vertex.S = HoloPF.isSegmentPathableAgainstObstacles(x, y, x, y - CELL_SIZE, obstacleExpandedSegs,
				obstaclePoints, unitRadius);
		vertex.W = HoloPF.isSegmentPathableAgainstObstacles(x, y, x - CELL_SIZE, y, obstacleExpandedSegs,
				obstaclePoints, unitRadius);
		vertex.E = HoloPF.isSegmentPathableAgainstObstacles(x, y, x + CELL_SIZE, y, obstacleExpandedSegs,
				obstaclePoints, unitRadius);

		vertex.NW = HoloPF.isSegmentPathableAgainstObstacles(x, y, x - CELL_SIZE, y + CELL_SIZE, obstacleExpandedSegs,
				obstaclePoints, unitRadius);
		vertex.NE = HoloPF.isSegmentPathableAgainstObstacles(x, y, x + CELL_SIZE, y + CELL_SIZE, obstacleExpandedSegs,
				obstaclePoints, unitRadius);
		vertex.SW = HoloPF.isSegmentPathableAgainstObstacles(x, y, x - CELL_SIZE, y - CELL_SIZE, obstacleExpandedSegs,
				obstaclePoints, unitRadius);
		vertex.SE = HoloPF.isSegmentPathableAgainstObstacles(x, y, x + CELL_SIZE, y - CELL_SIZE, obstacleExpandedSegs,
				obstaclePoints, unitRadius);

		if (indexX == 0)
			vertex.W = vertex.NW = vertex.SW = false;
		if (indexX == graphWidth - 1)
			vertex.E = vertex.NE = vertex.SE = false;
		if (indexY == 0)
			vertex.S = vertex.SW = vertex.SE = false;
		if (indexY == graphHeight - 1)
			vertex.N = vertex.NW = vertex.NE = false;
	}

	// Unit pathfinding

	ArrayList<Vertex> prospects;
	ArrayList<Vertex> blocked = new ArrayList<Vertex>();

	/**
	 * Modifies the graph by restricting vertices (and their edges) according to the additional
	 * colliding bodies given
	 * 
	 * @param infos List of the expanded polygons of units, excluding the pathing unit, with some extra
	 *              information.
	 * @param u     The pathing unit
	 */
	private void setDynamicGraph(ArrayList<CBInfo> infos, UnitPF u) {

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
	 * Given a vertex, restrict it's pathability based on the expanded collision circle of the unit
	 * 
	 * @param self Radius of the radius of the unit which is pathing. Used to get expanded geometry.
	 */
	private void restrictVertex(Vertex v, CBInfo cb, float unitRadius) {
		float x = v.ix * CELL_SIZE;
		float y = v.iy * CELL_SIZE;

		// if the vertex is inside the unit's expanded pathing body, we can immediately block it.

		Point p1 = new Point(x, y);
		Point p2 = new Point(cb.x, cb.y);
		float dist = Point.calcDistance(p1, p2);

		float expandedRadius = cb.unitRadius + unitRadius;
		float radSquared = expandedRadius * expandedRadius;

		if (dist < expandedRadius) {
			v.block();
			return;
		}

		// If it's outside, we still have to check its edges to block the right ones.

		// an edge is pathable if it's closest distance to the center of the circle is equal or greater to
		// than the
		// expanded radius

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
	 * Reverts the dynamic graph back to the orignal graph. The method currently sets every vertex back
	 */
	private void revertDynamicGraph() {
		for (int y = 0; y < graphHeight; y++) { // change later to use a stack of modifications or something.
			for (int x = 0; x < graphWidth; x++) {
				dynamicGraph[y][x].set(graph[y][x]);
			}
		}
	}

	/**
	 * 
	 * Find pathable placements near a specified point Assumes all units have radius Holo.UNIT_RADIUS
	 * 
	 * @return A list of non-overlapping pathable points up to length numPlacements. If initial location
	 *         is unpathable, returns empty list. If only a partial number of placements could be found,
	 *         returns that partial list.
	 */
	public List<Point> findPathablePlacements(Point spawnPoint, int numPlacements) {
		List<Point> placements = new ArrayList<>();

		final Vertex closestVertex = HoloPF.findClosestReachableVertex(spawnPoint, graph, graphWidth, graphHeight);
		if (closestVertex == null) {
			return placements; // empty list
		}
		placements.add(closestVertex.getAsPoint(CELL_SIZE));

		Point newPlacement = new Point();
		clearVisited();
		callMethodOnEveryNodeInFloodfillOrder(new Coord(closestVertex.ix, closestVertex.iy), (Coord c) -> {
			if (placements.size() >= numPlacements)
				return;
			newPlacement.set(c.x * Holo.CELL_SIZE, c.y * Holo.CELL_SIZE);
			addPlacementIfNotConflicting(newPlacement, placements);
		}, () -> (placements.size() >= numPlacements));

		return placements;
	}
	
	private static void addPlacementIfNotConflicting(Point p, List<Point> placements) {
		if(!placementConflicts(p, placements)){
			placements.add(new Point(p));
		}
	}
	private static boolean placementConflicts(Point placement, List<Point> prevPlacements) {
		for(Point other : prevPlacements) {
			if(Point.calcDistance(placement, other) < Holo.UNIT_RADIUS *2 + Holo.epsilon)
				return true;
		}
		return false;
	}

	/** Method should not modifiy the coord */
	private boolean[][] visited;

	private void callMethodOnEveryNodeInFloodfillOrder(Coord startCoord, Consumer<Coord> method,
			BooleanSupplier stopCondition) {
		Queue<Coord> q = new Queue<Coord>();
		q.ensureCapacity(graphWidth);

		q.addLast(startCoord); // start flood fill from this position

		// graph is already filled out, don't modify it

		Coord cur;
		Vertex vertex;
		Vertex suc;
		while (q.size > 0) {
			if (stopCondition.getAsBoolean() == true) {
				return;
			}
			cur = q.removeFirst();
			visited[cur.y][cur.x] = true;
			vertex = graph[cur.y][cur.x];

			// call method as we pop vertex
			method.accept(cur);

			suc = graph[cur.y + 1][cur.x];
			if (vertex.N && !visited[suc.iy][suc.ix]) {
				addCoordToQueueAndMarkVisited(suc.ix, suc.iy, q);
			}

			suc = graph[cur.y - 1][cur.x];
			if (vertex.S && !visited[suc.iy][suc.ix]) {
				addCoordToQueueAndMarkVisited(suc.ix, suc.iy, q);
			}

			suc = graph[cur.y][cur.x - 1];
			if (vertex.W && !visited[suc.iy][suc.ix]) {
				addCoordToQueueAndMarkVisited(suc.ix, suc.iy, q);
			}

			suc = graph[cur.y][cur.x + 1];
			if (vertex.E && !visited[suc.iy][suc.ix]) {
				addCoordToQueueAndMarkVisited(suc.ix, suc.iy, q);
			}

			suc = graph[cur.y + 1][cur.x - 1];
			if (vertex.NW && !visited[suc.iy][suc.ix]) {
				addCoordToQueueAndMarkVisited(suc.ix, suc.iy, q);
			}

			suc = graph[cur.y + 1][cur.x + 1];
			if (vertex.NE && !visited[suc.iy][suc.ix]) {
				addCoordToQueueAndMarkVisited(suc.ix, suc.iy, q);
			}

			suc = graph[cur.y - 1][cur.x - 1];
			if (vertex.SW && !visited[suc.iy][suc.ix]) {
				addCoordToQueueAndMarkVisited(suc.ix, suc.iy, q);
			}

			suc = graph[cur.y - 1][cur.x + 1];
			if (vertex.SE && !visited[suc.iy][suc.ix]) {
				addCoordToQueueAndMarkVisited(suc.ix, suc.iy, q);
			}
		}
	}
	private void addCoordToQueueAndMarkVisited(int ix, int iy, Queue<Coord> q) {
		q.addLast(new Coord(ix, iy));
		visited[iy][ix] = true;
	}

	// Render functions

	public void renderGraph(boolean renderEdges) {

		if (renderEdges) {
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
		}

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

	public void renderDynamicGraph(boolean renderEdges) {

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

	/**
	 * Render intermediate paths for all units in the list
	 */
	public void renderIntermediateAndFinalPaths(List<? extends UnitPF> units) {
		for (UnitPF unit : units) {
			PathsInfo info = intermediatePaths.get(unit);
			if (info != null && (unit.getPath() != null || Holo.continueShowingPathAfterArrival)) {
				if (info.finalPath != null) {
					renderPath(info.pathSmoothed0, Color.PINK, false);
					renderPath(info.pathSmoothed1, Color.FIREBRICK, true);
					renderPath(info.finalPath, Color.BLUE, false);
				}
			}
		}
	}

	// Getters

	private void drawLine(int ix, int iy, int ix2, int iy2) {
		shapeRenderer.line(ix * CELL_SIZE, iy * CELL_SIZE, 0, ix2 * CELL_SIZE, iy2 * CELL_SIZE, 0);
	}

	private void renderPath(Path path, Color color, boolean renderPoints) {
		float pathThickness = 2f;
		HoloPF.renderPath(path, color, renderPoints, pathThickness, shapeRenderer);
	}

	public List<Point> getObstaclePoints() {
		return Collections.unmodifiableList(obstaclePoints);
	}

	public List<OrientedSeg> getObstacleExpandedSegs() {
		return Collections.unmodifiableList(obstacleExpandedSegs);
	}

	/**
	 * Get the original, non-expanded obstacle segs
	 * 
	 * @return
	 */
	public List<OrientedSeg> getObstacleSegs() {
		return Collections.unmodifiableList(obstacleSegs);
	}

}