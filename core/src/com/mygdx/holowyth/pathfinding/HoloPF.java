package com.mygdx.holowyth.pathfinding;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.PriorityQueue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Queue;
import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.polygon.Polygons;
import com.mygdx.holowyth.util.constants.Holo;
import com.mygdx.holowyth.util.data.Point;
import com.mygdx.holowyth.util.data.Segment;

import de.lighti.clipper.Point.LongPoint;
import de.lighti.clipper.Clipper.EndType;
import de.lighti.clipper.Clipper.JoinType;
import de.lighti.clipper.ClipperOffset;

/**
 * Contains helper functions related to pathfinding and geometry
 *
 */
public class HoloPF {

	
	
	/**
	 * Determines whether an edge is pathable against a set of collision bodies (map polygons and unit circles)
	 * @param x
	 * @param y
	 * @param x2
	 * @param y2
	 * @param polys
	 * @param cbs The collision bodies for units
	 * @return
	 */
	public static boolean isEdgePathable(float x, float y, float x2, float y2, Polygons polys, ArrayList<CBInfo> cbs, float unitRadius) {
		boolean intersects = false;
		for (Polygon polygon : polys) {
			for (int i = 0; i <= polygon.count - 2; i += 2) { // for each polygon edge
				if (Line2D.linesIntersect(x, y, x2, y2, polygon.floats[i], polygon.floats[i + 1],
						polygon.floats[(i + 2) % polygon.count], polygon.floats[(i + 3) % polygon.count])) {
					intersects = true;
				}
			}
		}
		
		// Check against unit circles
		for(CBInfo cb: cbs){
			if (Line2D.ptSegDistSq(x, y, x2, y2, cb.x, cb.y) < (cb.unitRadius+unitRadius) * (cb.unitRadius+unitRadius)){
				intersects = true;
			}
		}
		return !intersects;
	}
	
	/**
	 * Determines whether an edge is pathable against a set of collision bodies (just map polygons)
	 * @param x
	 * @param y
	 * @param x2
	 * @param y2
	 * @param polys
	 * @return
	 */
	public static boolean isEdgePathable(float x, float y, float x2, float y2, Polygons polys) {
		boolean intersects = false;
		for (Polygon polygon : polys) {
			for (int i = 0; i <= polygon.count - 2; i += 2) { // for each polygon edge
				if (Line2D.linesIntersect(x, y, x2, y2, polygon.floats[i], polygon.floats[i + 1],
						polygon.floats[(i + 2) % polygon.count], polygon.floats[(i + 3) % polygon.count])) {
					intersects = true;
				}
			}
		}
		return !intersects;
	}

	public static boolean isEdgePathable(float x, float y, float x2, float y2, Polygon polygon) {
		boolean intersects = false;
		for (int i = 0; i <= polygon.count - 2; i += 2) { // for each polygon edge
			if (Line2D.linesIntersect(x, y, x2, y2, polygon.floats[i], polygon.floats[i + 1],
					polygon.floats[(i + 2) % polygon.count], polygon.floats[(i + 3) % polygon.count])) {
				intersects = true;
			}
		}
		return !intersects;
	}

	public static void renderPath(Path path, Color color, boolean renderPoints, float thickness,
			ShapeRenderer shapeRenderer) {
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

	/**
	 * @return returns a new set of polygons which are expanded
	 */
	public static Polygons expandPolygons(Polygons origPolys, float delta) {

		Polygons polys = new Polygons();

		de.lighti.clipper.Paths solution = new de.lighti.clipper.Paths();

		de.lighti.clipper.Path solPath = new de.lighti.clipper.Path();

		for (Polygon poly : origPolys) {
			ClipperOffset co = new ClipperOffset(1.4, 0.25f);

			de.lighti.clipper.Path path = new de.lighti.clipper.Path();
			// load the polygon data into the path
			for (int i = 0; i < poly.count; i += 2) {
				path.add(new LongPoint((long) poly.floats[i], (long) poly.floats[i + 1]));
			}
			co.addPath(path, JoinType.MITER, EndType.CLOSED_POLYGON);
			co.execute(solution, delta);

			solPath = solution.get(0);
			// Polygon newPoly = new Polygon();
			// vertexes

			// Reload the result data back into a polygon
			float[] polyData = new float[solPath.size() * 2];

			for (int i = 0; i < solPath.size(); i++) {
				polyData[2 * i] = solPath.get(i).getX();
				polyData[2 * i + 1] = solPath.get(i).getY();
			}

			polys.add(new Polygon(polyData, solPath.size() * 2));
		}

		return polys;

	}

	/**
	 * @param maxCellDistance how many cells away to draw from (e.g. distance 1 would consider 9 cells)
	 * @return A list of the nearest reachable vertexes, sorted in order of distance. (Note these vertexes are live references to the graph's vertexes)
	 */
	public static ArrayList<Vertex> findNearbyReachableVertexes(Point p, Vertex[][] graph, int graphWidth, int graphHeight,
			int maxCellDistance) {

		int ix = (int) (p.x / Holo.CELL_SIZE); ; // corresponds to the closest lower left vertex
		int iy = (int) (p.y /  Holo.CELL_SIZE);

		ArrayList<Vertex> prospects = new ArrayList<Vertex>();
		
		//Only consider vertexes inside the graph
		for (int cx = ix - maxCellDistance; cx <= ix + maxCellDistance; cx++) { // cx "current x"
			for(int cy = iy - maxCellDistance; cy <= iy + maxCellDistance; cy++){
				Vertex v = new Vertex(cx, cy);
				if(isVertexInMap(v, graphWidth, graphHeight)){
					prospects.add(v);
				}
			}
		}
		
		ArrayList<VertexDist> vds = new ArrayList<VertexDist>();
		for(Vertex v: prospects){
			if(graph[v.iy][v.ix].reachable){
				vds.add(new VertexDist(graph[v.iy][v.ix], p.x, p.y, Holo.CELL_SIZE));
			}
		}
		vds.sort((VertexDist vd1, VertexDist vd2) ->  (vd1.dist - vd2.dist >= 0) ? 1 : -1);
		
		ArrayList<Vertex> vertexes = new ArrayList<Vertex>();
		for(VertexDist vd: vds){
			vertexes.add(vd.vertex); // note these are live graph references
		}
		return vertexes;
	}
	


	public static Vertex findClosestReachableVertex(Point p, Vertex[][] graph, int graphWidth, int graphHeight,
			int maxCellDistance) {
		ArrayList<Vertex> result = findNearbyReachableVertexes(p, graph, graphWidth, graphHeight, maxCellDistance);
		return result.size() > 0 ? result.get(0) : null;
	}
	
	
	public static boolean isPointInMap(Point p, int mapWidth, int mapHeight){
		if(p.x <0 || p.x> mapWidth)
			return false;
		if(p.y <0 || p.y > mapHeight)
			return false;
		return true;
	}
	
	/**
	 * @return true if the vertex is within the map's graph 
	 */
	public static boolean isVertexInMap(Vertex v, int graphWidth, int graphHeight){
		if(v.ix <0 || v.ix>=graphWidth)
			return false;
		if(v.iy <0 || v.iy >= graphHeight)
			return false;
		return true;
	}
	public static boolean isIndexInMap(int ix, int iy, int graphWidth, int graphHeight){
		if(ix <0 || ix>=graphWidth)
			return false;
		if(iy <0 || iy >= graphHeight)
			return false;
		return true;
	}

	/**
	 * 
	 * Small data class for assisting with sorting vertexes via distance
	 */
	private static class VertexDist {
		public Vertex vertex;
		public float dist;

		VertexDist(Vertex v, float ux, float uy, int CELL_SIZE) {
			this.vertex = v;
			float x = v.ix * CELL_SIZE;
			float y = v.iy * CELL_SIZE;
			this.dist = (float) Math.sqrt((ux - x) * (ux - x) + (uy - y) * (uy - y));
		}
	}
}
