package com.mygdx.holowyth.pathfinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.util.data.Pair;
import com.mygdx.holowyth.util.data.Point;

/**
 * Currently the result of an AStarSearch can be extracted from ancestor.
 * 
 * @param graph
 *            A tile graph to be used for the search
 */
public class AStarSearch {
	public int[] ancestor;
	public boolean[] visited;
	public float[] minCost; // Mininum cost found to get here from the start

	int graphWidth, graphHeight;
	Vertex[][] graph;

	int CELL_SIZE;

	/**
	 * 
	 * Graph is assumed to be a rectangular grid.
	 * 
	 * @Usage Initialize the AStarSearch object. Then you can run arbitrary number of searches with the same object.
	 */
	public AStarSearch(int graphWidth, int graphHeight, Vertex[][] graph, int cellSize) {

		this.graph = graph;
		this.graphWidth = graphWidth;
		this.graphHeight = graphHeight;

		visited = new boolean[graphWidth * graphHeight];
		ancestor = new int[graphWidth * graphHeight];
		minCost = new float[graphWidth * graphHeight];

		this.CELL_SIZE = cellSize;
	}

	PriorityQueue<Node> q = new PriorityQueue<Node>(new Comparator<Node>() {
		public int compare(Node n1, Node n2) {
			if (n2.f - n1.f < 0) {
				return 1;
			} else {
				return -1;
			}

		}
	});

	private final float SQRT2 = 1.414214f; // rounded up slightly so that h is valid

	public Path doAStar(int startVertex, int goalVertex) {
		runAStar(startVertex, goalVertex);
		return this.retrievePath();
	}

	/**
	 * Takes in starting and goal locations instead of graph vertexes.
	 */
	public Path doAStar(float sx, float sy, float dx, float dy, ArrayList<Polygon> polys) {

		if (outsideMapBounds(dx, dy)) {
			System.out.println("AStar input: Point is outside map bounds");
			return null;
		}

		int startVertex = findClosestPathableVertex(sx, sy, polys);
		int goalVertex = findClosestPathableVertex(dx, dy, polys);
		
		//TODO: need a check to ensure that goalVertex is on a reachable vertex (found via floodfil)

		runAStar(startVertex, goalVertex);

		startX = sx;
		startY = sy;
		goalX = dx;
		goalY = dy;
		coordinateSearchUsed = true;
		return this.retrievePath();
	}

	boolean coordinateSearchUsed = false;
	float goalX, goalY;
	float startX, startY;
	boolean searchFailed;

	private void runAStar(int startVertex, int goalVertex) {
		clearSearch();
		searchFailed = false;

		coordinateSearchUsed = false;
		q.add(new Node(startVertex, 0, calculateDistSquared(startVertex, goalVertex), graphWidth));
		minCost[startVertex] = 0;

		this.startVertex = startVertex;
		this.goalVertex = goalVertex;

		while (!q.isEmpty()) {
			Node curNode = q.remove();

			// terminate if this node is the goal
			if (curNode.vertexID == goalVertex) {
				searchDone = true;
				System.out.println("Goal Reached (A* search)");
				return;
			}

			// visit this node
			visited[curNode.vertexID] = true;

			System.out.format("Current Node: (%s, %s) %s %s %n", (curNode.vertexID % graphWidth),
					(curNode.vertexID / graphWidth), curNode.costToGetHere, curNode.h);

			// Create the 8 successors of curNode, if the edges are pathable
			Vertex curVertex = graph[curNode.vertexID / graphWidth][curNode.vertexID % graphWidth];
			if (curVertex.N) {
				addNodeIfNoCheaperExists(curNode, curNode.vertexID + graphWidth, 1, goalVertex);
			}
			if (curVertex.S) {
				addNodeIfNoCheaperExists(curNode, curNode.vertexID - graphWidth, 1, goalVertex);
			}
			if (curVertex.W) {
				addNodeIfNoCheaperExists(curNode, curNode.vertexID - 1, 1, goalVertex);
			}
			if (curVertex.E) {
				addNodeIfNoCheaperExists(curNode, curNode.vertexID + 1, 1, goalVertex);
			}

			if (curVertex.NW) {
				addNodeIfNoCheaperExists(curNode, curNode.vertexID + graphWidth - 1, SQRT2, goalVertex);
			}
			if (curVertex.NE) {
				addNodeIfNoCheaperExists(curNode, curNode.vertexID + graphWidth + 1, SQRT2, goalVertex);
			}
			if (curVertex.SW) {
				addNodeIfNoCheaperExists(curNode, curNode.vertexID - graphWidth - 1, SQRT2, goalVertex);
			}
			if (curVertex.SE) {
				addNodeIfNoCheaperExists(curNode, curNode.vertexID - graphWidth + 1, SQRT2, goalVertex);
			}
		}
		System.out.println("Warning: A-star search completed without finding the goalNode");
		searchDone = true;
		searchFailed = true;
	}

	private Path retrievePath() {

		if (searchFailed) {
			return null;
		}

		Path path = new Path();

		if (coordinateSearchUsed) { // if using coordinate based search, the goalVertex != goal point
			path.add(new Point(goalX, goalY));
		}

		int curVertex = goalVertex;
		path.add(new Point((float) curVertex % graphWidth * CELL_SIZE,
				(float) (curVertex / graphWidth * CELL_SIZE)));

		while (ancestor[curVertex] >= 0) {
			curVertex = ancestor[curVertex];
			path.add(new Point((float) curVertex % graphWidth * CELL_SIZE,
					(float) (curVertex / graphWidth * CELL_SIZE)));
		}

		if (coordinateSearchUsed) { // if using coordinate based search, the goalVertex != goal point
			path.add(new Point(startX, startY));
		}
		Collections.reverse(path);

		// assert that backwards track lead back to the startVertex
		assert (path.get(0).y * graphWidth + path.get(0).x == startVertex);
		return path;
	}

	private void clearSearch() {
		Arrays.fill(visited, false);
		Arrays.fill(ancestor, -1);
		Arrays.fill(minCost, 10e10f);
		q.clear();
	}

	class PointData {
		public int ix;
		public int iy;
		public float dist;

		PointData(int ix, int iy, float ux, float uy) {
			this.ix = ix;
			this.iy = iy;

			float x = ix * CELL_SIZE;
			float y = iy * CELL_SIZE;
			this.dist = (float) Math.sqrt((ux - x) * (ux - x) + (uy - y) * (uy - y));
		}
	}

	boolean searchStarted;
	boolean searchDone;
	int startVertex = -1;
	int goalVertex = -1;

	private float cost;

	/**
	 * Out of the 4 closest points, returns the closest one that is pathable
	 * Assumes that the point given is within map boundaries
	 */
	private int findClosestPathableVertex(float sx, float sy, ArrayList<Polygon> polys) {
		int ix = (int) (sx / CELL_SIZE);
		int iy = (int) (sy / CELL_SIZE);

		PointData dNW, dNE, dSE, dSW;
		dNW = new PointData(ix, iy + 1, sx, sy);
		dNE = new PointData(ix + 1, iy + 1, sx, sy);
		dSW = new PointData(ix, iy, sx, sy);
		dSE = new PointData(ix + 1, iy, sx, sy);

		List<PointData> points = new ArrayList<PointData>(Arrays.asList(dNW, dNE, dSW, dSE));

		points.sort((PointData p1, PointData p2) -> ((int) (p1.dist - p2.dist)));

		int startIx = 0, startIy = 0;
		boolean found = false;
		for (int i = 0; i < 4; i++) {
			PointData p = points.get(i);
			if (HoloPF.isEdgePathable(sx, sy, p.ix * CELL_SIZE, p.iy * CELL_SIZE, polys)) {
				startIx = p.ix;
				startIy = p.iy;
				found = true;
				break;
			}
		}

		assert (found); // ("None of the 4 closest points were reachable");
		return startIy * graphWidth + startIx;
	}

	private void addNodeIfNoCheaperExists(Node curNode, int sucId, float edgeCost, int goalVertex) {
		cost = curNode.costToGetHere + edgeCost;
		System.out.format("SucNode ID: %s %s %s %n", sucId, cost, minCost[sucId]);
		if (cost < minCost[sucId]) {
			minCost[sucId] = cost;
			ancestor[sucId] = curNode.vertexID;
			q.add(new Node(sucId, cost, calculateDistSquared(sucId, goalVertex), graphWidth));
		}
	}

	private float calculateDistSquared(int startVertex, int goalVertex) {
		int gy = goalVertex / graphWidth;
		int gx = goalVertex % graphWidth;
		int sy = startVertex / graphWidth;
		int sx = startVertex % graphWidth;

		return (gx - sx) * (gx - sx) + (gy - sy) * (gy - sy);
	}

	private boolean outsideMapBounds(float x, float y) {
		int mapWidth = (graphWidth - 1) * CELL_SIZE;
		int mapHeight = (graphHeight - 1) * CELL_SIZE;
		return (x < 0 || x > mapWidth || y < 0 || y > mapHeight);
	}

}
