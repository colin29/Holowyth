package com.mygdx.holowyth.pathfinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.mygdx.holowyth.util.Pair;

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

	AStarSearch(int graphWidth, int graphHeight, Vertex[][] graph, int cellSize) {

		this.graph = graph;
		this.graphWidth = graphWidth;
		this.graphHeight = graphHeight;

		visited = new boolean[graphWidth * graphHeight];
		ancestor = new int[graphWidth * graphHeight];
		Arrays.fill(ancestor, -1);
		minCost = new float[graphWidth * graphHeight];
		Arrays.fill(minCost, 99999999);

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

	public void doAStar(int startVertex, int goalVertex) {
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
			int sucId;
			if (curVertex.N) {
				addNodeIfNoCheaperExists(curNode, curNode.vertexID + graphWidth, 1, goalVertex);
			}
			if (curVertex.S) {
				addNodeIfNoCheaperExists(curNode, curNode.vertexID - graphWidth, 1, goalVertex);
			}
			if (curVertex.W) {
				sucId = curNode.vertexID - 1;
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
		if (q.isEmpty()) {
			System.out.println("Warning: A-star search completed without finding the goalNode");
			searchDone = true;
		}
	}

	boolean searchStarted;
	boolean searchDone;
	int startVertex = -1;
	int goalVertex = -1;

	public void initStepAStar(int startVertex, int goalVertex) {
		q.add(new Node(startVertex, 0, calculateDistSquared(startVertex, goalVertex), graphWidth));
		minCost[startVertex] = 0;
		searchStarted = true;
		searchDone = false;

		this.startVertex = startVertex;
		this.goalVertex = goalVertex;
	}

	public void stepAStar() {
		if (!searchStarted || searchDone) {
			return;
		}
		if (!q.isEmpty()) {
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
			int sucId;
			if (curVertex.N) {
				addNodeIfNoCheaperExists(curNode, curNode.vertexID + graphWidth, 1, goalVertex);
			}
			if (curVertex.S) {
				addNodeIfNoCheaperExists(curNode, curNode.vertexID - graphWidth, 1, goalVertex);
			}
			if (curVertex.W) {
				sucId = curNode.vertexID - 1;
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

		System.out.format("Next Node in Queue:  (%s) %s %s %n", q.peek().coordinates, q.peek().f,
				q.peek().costToGetHere);

		if (q.isEmpty()) {
			System.out.println("Warning: A-star search completed without finding the goalNode");
			searchDone = true;
		}
	}

	private float cost;

	private void addNodeIfNoCheaperExists(Node curNode, int sucId, float edgeCost, int goalVertex) {
		cost = curNode.costToGetHere + edgeCost;
		System.out.format("SucNode ID: %s %s %s %n", sucId, cost, minCost[sucId]);
		if (cost < minCost[sucId]) {
			minCost[sucId] = cost;
			ancestor[sucId] = curNode.vertexID;
			q.add(new Node(sucId, cost, calculateDistSquared(sucId, goalVertex), graphWidth));
		}
	}

	/**
	 * @param startVertex
	 * @param goalVertex
	 * @return the square of the distance from current to goal vertex, in cells;
	 */
	private float calculateDistSquared(int startVertex, int goalVertex) {
		int gy = goalVertex / graphWidth;
		int gx = goalVertex % graphWidth;
		int sy = startVertex / graphWidth;
		int sx = startVertex % graphWidth;

		return (gx - sx) * (gx - sx) + (gy - sy) * (gy - sy);
	}

	public Path retrievePath() {
		Path path = new Path();
		int curVertex = goalVertex;
		path.add(new Pair<Float, Float>((float) curVertex % graphWidth * CELL_SIZE,
				(float) curVertex / graphWidth * CELL_SIZE));

		while (ancestor[curVertex] >= 0) {
			curVertex = ancestor[curVertex];
			path.add(new Pair<Float, Float>((float) curVertex % graphWidth * CELL_SIZE,
					(float) curVertex / graphWidth * CELL_SIZE));
		}
		Collections.reverse(path);

		// assert that backwards track lead back to the startVertex
		assert (path.get(0).second() * graphWidth + path.get(0).first() == startVertex);
		return path;
	}

}
