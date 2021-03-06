package com.mygdx.holowyth.pathfinding;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.eclipse.jdt.annotation.NonNull;

import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.world.map.obstacle.OrientedSeg;

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
	int mapWidth, mapHeight;
	Vertex[][] graph;

	int CELL_SIZE;

	/**
	 * 
	 * @param graph
	 *            The search graph to be used by default. Graph is assumed to be a rectangular grid.
	 * 
	 * @Usage Initialize the AStarSearch object. Then you can run arbitrary number of searches with the same object.
	 */
	public AStarSearch(int graphWidth, int graphHeight, Vertex[][] graph, int cellSize, int mapWidth, int mapHeight) {

		this.graph = graph;
		this.graphWidth = graphWidth;
		this.graphHeight = graphHeight;

		visited = new boolean[graphWidth * graphHeight];
		ancestor = new int[graphWidth * graphHeight];
		minCost = new float[graphWidth * graphHeight];

		this.CELL_SIZE = cellSize;

		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
	}

	PriorityQueue<Node> q = new PriorityQueue<Node>(new Comparator<Node>() {
		@Override
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
		runAStar(startVertex, goalVertex, this.graph);
		return this.retrievePath();
	}

	/**
	 * Takes in starting and goal locations instead of graph vertexes. Searches on the default (initial) graph
	 */
	public Path doAStar(float sx, float sy, float dx, float dy, List<OrientedSeg> obstacleExpandedSegs,
			List<Point> obstaclePoints, float unitRadius) {
		return doAStar(sx, sy, dx, dy, obstacleExpandedSegs, obstaclePoints, null, this.graph, unitRadius);
	}

	/**
	 * Like method above but takes in a modified graph. Used for dynamic pathfinding.
	 * 
	 * @return
	 */
	public Path doAStar(float sx, float sy, float dx, float dy, List<OrientedSeg> obstacleExpandedSegs,
			List<Point> obstaclePoints, List<@NonNull UnitPF> unitCBs, Vertex[][] graph, float unitRadius) {

		startX = sx;
		startY = sy;
		goalX = dx;
		goalY = dy;

		if (isOutsideMapBounds(dx, dy)) {
			System.out.println("AStar input: Goal point is outside map bounds");
			return null;
		}

		int startVertex = findClosestPathableVertex(sx, sy, obstacleExpandedSegs, obstaclePoints, unitCBs, graph, unitRadius, false, null);
		Point substitutePoint = new Point(0, 0);
		int goalVertex = findClosestPathableVertex(dx, dy, obstacleExpandedSegs, obstaclePoints, unitCBs, graph, unitRadius, true, substitutePoint);
		if (goalVertex < 0) {
			System.out.println("No valid goal substitute could be found. Aborting search.");
			return null;
		}
		if (startVertex == -1) {
			System.out.println("Warning: Start position was unpathable (Improperly placed unit?). Aborting Search.");
			searchFailed = true;
			return null;
		}

		if (substituteLocationFound) {
			goalX = substitutePoint.x;
			goalY = substitutePoint.y;
		}

		runAStar(startVertex, goalVertex, graph); // note AStar only is operating on vertexes, and not coordinates

		coordinateSearchUsed = true;
		return this.retrievePath();
	}

	boolean coordinateSearchUsed = false;
	float goalX, goalY;
	float startX, startY;
	boolean searchFailed;

	private void runAStar(int startVertex, int goalVertex, Vertex[][] graph) {

		Vertex gv = graph[goalVertex / graphWidth][goalVertex % graphWidth];
		if (!gv.reachable) {
			System.out.println("Detected goal was an unreachable vertex, short-circuting:");
			searchFailed = true;
			return;
		}
		Vertex sv = graph[startVertex / graphWidth][startVertex % graphWidth];
		if (!sv.reachable) {
			System.out.println("Detected start was an unreachable vertex, short-circuting:");
			searchFailed = true;
			return;
		}

		clearSearch();
		searchFailed = false;

		coordinateSearchUsed = false;
		q.add(new Node(startVertex, 0, calculateDistSquared(startVertex, goalVertex), graphWidth));
		minCost[startVertex] = 0; // if this throws an indexException, this is likely because the unit is out of map
									// bounds

		this.startVertex = startVertex;
		this.goalVertex = goalVertex;

		while (!q.isEmpty()) {
			Node curNode = q.remove();

			// terminate if this node is the goal
			if (curNode.vertexID == goalVertex) {
				searchDone = true;
				// System.out.println("Goal Reached (A* search)");
				return;
			}

			// visit this node
			visited[curNode.vertexID] = true;

			// System.out.format("Current Node: (%s, %s) %s %s %n", (curNode.vertexID % graphWidth),
			// (curNode.vertexID / graphWidth), curNode.costToGetHere, curNode.h);

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
		path.add(new Point((float) curVertex % graphWidth * CELL_SIZE, curVertex / graphWidth * CELL_SIZE));

		while (ancestor[curVertex] >= 0) {
			curVertex = ancestor[curVertex];
			path.add(new Point((float) curVertex % graphWidth * CELL_SIZE,
					curVertex / graphWidth * CELL_SIZE));
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

	private boolean substituteLocationFound = false;

	/**
	 * Will attempt to correct the location if unreachable
	 * 
	 * @param correctedLocation
	 *            recieves the corrected location, if the goal point was corrected
	 * 
	 * @return -1 if the location cannot be found and search should be aborted. If if substituteLocationFound is set to true, the original location
	 *         was invalid but a substitute has been found.
	 */
	public int findClosestPathableVertex(float goalX, float goalY, List<OrientedSeg> obstacleExpandedSegs, List<Point> obstaclePoints,
			List<@NonNull UnitPF> unitCbs,
			Vertex[][] graph, float unitRadius, boolean allowCorrection, Point correctedLocation) {
		substituteLocationFound = false;
		Point goalPoint = new Point(goalX, goalY);

		List<Vertex> reachable = HoloPF.findNearbyReachableVertexes(goalPoint, graph, graphWidth, graphHeight, 5);

		int startIx = 0, startIy = 0;
		boolean found = false;

		for (Vertex v : reachable) {
			// there must be a pathable line from the target destination to the vertex, AND that vertex must be a
			// reachable one
			if (HoloPF.isSegmentPathable(goalX, goalY, v.ix * CELL_SIZE, v.iy * CELL_SIZE, obstacleExpandedSegs, obstaclePoints, unitCbs, unitRadius)
					&& v.reachable) {
				startIx = v.ix;
				startIy = v.iy;
				found = true;
				break;
			}
		}

		if (!found && !allowCorrection) {
			return -1;
		}

		// If there is no path to a reachable vertex, substitute the goal destination with the closest reachable
		// vertex.

		if (!found) {
			if (reachable.size() > 0) {
				Vertex v = reachable.get(0);
				startIx = v.ix;
				startIy = v.iy;
				correctedLocation.x = v.ix * CELL_SIZE;
				correctedLocation.y = v.iy * CELL_SIZE;

				found = true;
				substituteLocationFound = true;
			}
		}

		if (!found) {
			return -1;
		}
		return startIy * graphWidth + startIx;
	}

	private void addNodeIfNoCheaperExists(Node curNode, int sucId, float edgeCost, int goalVertex) {
		cost = curNode.costToGetHere + edgeCost;
		// System.out.format("SucNode ID: %s %s %s %n", sucId, cost, minCost[sucId]);
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

	private boolean isOutsideMapBounds(float x, float y) {
		return !HoloPF.isPointInMap(new Point(x, y), mapWidth, mapHeight);
	}

}
