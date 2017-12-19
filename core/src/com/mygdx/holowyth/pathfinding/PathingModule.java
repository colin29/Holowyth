package com.mygdx.holowyth.pathfinding;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Queue;
import com.mygdx.holowyth.Unit;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.pathfinding.PathSmoother.PathsInfo;
import com.mygdx.holowyth.polygon.Polygons;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloGL;
import com.mygdx.holowyth.util.data.Coord;
import com.mygdx.holowyth.util.data.Point;

public class PathingModule {

	// Rendering and pipeline variables
	OrthographicCamera camera;
	ShapeRenderer shapeRenderer;
	private Polygons expandedMapPolys = new Polygons();
	private Field map;
	private AStarSearch pathing;
	
	PathSmoother smoother = new PathSmoother();
	
	// Debug
		HashMap<UnitInter, PathsInfo> intermediatePaths;

	/**
	 * @Lifetime can be from app start to app shutdown. Call initFormap() whenever a new map is loaded.
	 * @param camera
	 * @param shapeRenderer
	 */
	public PathingModule(OrthographicCamera camera, ShapeRenderer shapeRenderer) {
		this.camera = camera;
		this.shapeRenderer = shapeRenderer;

	}

	public void initForMap(Field map) {
		this.map = map;
		
		expandedMapPolys = HoloPF.expandPolygons(map.polys, Holo.UNIT_RADIUS); // We require one set for each distinct size of unit, for now just
		// one.
		createGraph();
		floodFillGraph();
		
		pathing = new AStarSearch(graphWidth, graphHeight, graph, CELL_SIZE, map.width(), map.height());
		
		// Debug
		intermediatePaths = new HashMap<UnitInter, PathsInfo>();
	}
	
	// Graph construction 
	private int CELL_SIZE = Holo.CELL_SIZE;
	Vertex[][] graph;
	int graphWidth, graphHeight;
	Vertex[][] dynamicGraph;

	// Unit pathfinding
	
	public Path findPathForUnit(UnitInter unit, float dx, float dy, ArrayList<? extends UnitInter> units) {
	
		// For pathfinding, need to get expanded geometry of unit collision bodies as well
	
		ArrayList<CBInfo> colBodies = new ArrayList<CBInfo>();
	
		for (UnitInter a : units) {
			if (unit.equals(a)) { // don't consider the unit's own collision body
				continue;
			}
	
			CBInfo c = new CBInfo();
			c.x = a.getX();
			c.y = a.getY();
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
		if(!Holo.debugPathfindingIgnoreUnits){
		setDynamicGraph(colBodies, unit);
		}
		Path newPath = pathing.doAStar(unit.getX(), unit.getY(), dx, dy, expandedMapPolys, colBodies, dynamicGraph,
				unit.getRadius()); // use the dynamic graph
	
		if (newPath != null) {
			Path finalPath = smoother.smoothPath(newPath, expandedMapPolys, colBodies, unit.getRadius());
			PathsInfo info = smoother.getPathInfo();
			intermediatePaths.put(unit, info);
			return finalPath;
		}
		return newPath;
	}

	//Getters
	
	/**
	 * Note: expanded map polygons are used both by the pathfinding module and for collision detection, but they are kept with pathfinding by convention.
	 * @return
	 */
	public Polygons getExpandedMapPolys(){
		return expandedMapPolys;
	}

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
		v.N = HoloPF.isEdgePathable(x, y, x, y + CELL_SIZE, polys);
		v.S = HoloPF.isEdgePathable(x, y, x, y - CELL_SIZE, polys);
		v.W = HoloPF.isEdgePathable(x, y, x - CELL_SIZE, y, polys);
		v.E = HoloPF.isEdgePathable(x, y, x + CELL_SIZE, y, polys);

		v.NW = HoloPF.isEdgePathable(x, y, x - CELL_SIZE, y + CELL_SIZE, polys);
		v.NE = HoloPF.isEdgePathable(x, y, x + CELL_SIZE, y + CELL_SIZE, polys);
		v.SW = HoloPF.isEdgePathable(x, y, x - CELL_SIZE, y - CELL_SIZE, polys);
		v.SE = HoloPF.isEdgePathable(x, y, x + CELL_SIZE, y - CELL_SIZE, polys);

		if (ix == 0)
			v.W = v.NW = v.SW = false;
		if (ix == graphWidth - 1)
			v.E = v.NE = v.SE = false;
		if (iy == 0)
			v.S = v.SW = v.SE = false;
		if (iy == graphHeight - 1)
			v.N = v.NW = v.NE = false;
	}
	
	// Unit pathfinding
	
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
	private void setDynamicGraph(ArrayList<CBInfo> infos, UnitInter u) {

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
	 * @param unit
	 *            Radius of the radius of the unit which is pathing. Used to get expanded geometry.
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

		// an edge is pathable if it's closest distance to the center of the circle is equal or greater to than the
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
	 * Reverts the dynamic graph back to the orignal graph. The current method is simple brute-force.
	 */
	private void revertDynamicGraph() {
		for (int y = 0; y < graphHeight; y++) { // change later to use a stack of modifications or something.
			for (int x = 0; x < graphWidth; x++) {
				dynamicGraph[y][x].set(graph[y][x]);
			}
		}
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
	public void renderIntermediatePaths(ArrayList<? extends UnitInter> units){
		for(UnitInter unit: units){
			PathsInfo info = intermediatePaths.get(unit);
			if(info != null && (unit.getPath() != null || Holo.continueShowingPathAfterArrival)){
				if(info.finalPath != null){
					renderPath(info.pathSmoothed0, Color.PINK, false);
					renderPath(info.pathSmoothed1, Color.FIREBRICK, true);
					renderPath(info.finalPath, Color.BLUE, false);
				}
			}
		}
	}
	
	public void renderExpandedMapPolygons() {
		shapeRenderer.setProjectionMatrix(camera.combined);
		HoloGL.renderPolygons(expandedMapPolys, shapeRenderer, Color.GRAY);
	}
	
	
	//Getters
	
	private void drawLine(int ix, int iy, int ix2, int iy2) {
		shapeRenderer.line(ix * CELL_SIZE, iy * CELL_SIZE, 0, ix2 * CELL_SIZE, iy2 * CELL_SIZE, 0);
	}

	private void renderPath(Path path, Color color, boolean renderPoints) {
		float pathThickness = 2f;
		HoloPF.renderPath(path, color, renderPoints, pathThickness, shapeRenderer);
	}

}