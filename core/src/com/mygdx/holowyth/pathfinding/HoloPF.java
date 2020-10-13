package com.mygdx.holowyth.pathfinding;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Intersector;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;
import com.mygdx.holowyth.world.map.obstacle.OrientedSeg;

/**
 * Contains helper functions related to pathfinding and geometry
 *
 */
public class HoloPF {

	public static ArrayList<UnitPF> detectCollisionsFromUnitMoving(float x, float y, float x2, float y2, ArrayList<UnitPF> cbs,
			float thisUnitRadius) {
		@SuppressWarnings("unused")
		boolean intersects = false;
		ArrayList<UnitPF> collisions = new ArrayList<UnitPF>();
		// Check against unit circles
		for (UnitPF cb : cbs) {
			if (Line2D.ptSegDistSq(x, y, x2, y2, cb.getX(), cb.getY()) < (cb.getRadius() + thisUnitRadius)
					* (cb.getRadius() + thisUnitRadius)) {
				intersects = true;
				collisions.add(cb);
			}
		}
		return collisions;
	}

	private static Segment tempSeg = new Segment(0, 0, 0, 0);

	/**
	 * Checks pathability based on obstacles AND other units
	 */
	public static boolean isSegmentPathable(float x1, float y1, float x2, float y2, List<OrientedSeg> obstacleExpandedSegs,
			List<Point> obstaclePoints, List<@NonNull ? extends UnitPF> unitCBs,
			float thisUnitRadius) {
		tempSeg.set(x1, y1, x2, y2);
		return isSegmentPathable(tempSeg, obstacleExpandedSegs, obstaclePoints, unitCBs, thisUnitRadius);
	}

	/**
	 * Checks pathability based on obstacles AND other units
	 */
	public static boolean isSegmentPathable(Segment motion, List<OrientedSeg> obstacleExpandedSegs, List<Point> obstaclePoints, List<@NonNull ? extends UnitPF> unitCBs,
			float thisUnitRadius) {
		for (var other : unitCBs) {
			if (Intersector.distanceSegmentPoint(motion.x1, motion.y1, motion.x2, motion.y2, other.getX(), other.getY()) <= thisUnitRadius + other.getRadius()) {
				return false;
			}
		}
		return isSegmentPathableAgainstObstacles(motion, obstacleExpandedSegs, obstaclePoints, thisUnitRadius);
	}

	public static boolean isSegmentPathableAgainstObstacles(float x1, float y1, float x2, float y2, List<OrientedSeg> displacedSegs,
			List<Point> points,
			float unitRadius) {
		tempSeg.set(x1, y1, x2, y2);
		return isSegmentPathableAgainstObstacles(tempSeg, displacedSegs, points, unitRadius);
	}

	/**
	 * @param motion
	 *            The motion of the unit
	 * @param obstacleExpandedSegs
	 *            The obstacle segs, already displaced outwards by {unit radius}
	 * @param obstaclePoints
	 * @param unitRadius
	 * @return
	 */
	public static boolean isSegmentPathableAgainstObstacles(Segment motion, List<OrientedSeg> obstacleExpandedSegs, List<Point> obstaclePoints,
			float unitRadius) {

		for (var seg : obstacleExpandedSegs) {
			if (Line2D.linesIntersect(motion.x1, motion.y1, motion.x2, motion.y2, seg.x1, seg.y1, seg.x2, seg.y2)) {
				return false;
			}
		}
		for (var point : obstaclePoints) {
			if (Intersector.distanceSegmentPoint(motion.x1, motion.y1, motion.x2, motion.y2, point.x, point.y) <= unitRadius) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Use this when have irregular sized entities
	 */
	public static boolean isSegmentPathableAgainstObstaclesNonExpandedSeg(Segment motion, List<OrientedSeg> obstacleSegs,
			List<Point> obstaclePoints,
			float objectRadius) {

		var expandedSegs = new ArrayList<OrientedSeg>();
		for (var seg : obstacleSegs) {
			expandedSegs.add(seg.getOutwardlyDisplacedSegment(objectRadius));
		}

		for (var seg : expandedSegs) {
			if (Line2D.linesIntersect(motion.x1, motion.y1, motion.x2, motion.y2, seg.x1, seg.y1, seg.x2, seg.y2)) {
				return false;
			}
		}
		for (var point : obstaclePoints) {
			if (Intersector.distanceSegmentPoint(motion.x1, motion.y1, motion.x2, motion.y2, point.x, point.y) <= objectRadius) {
				return false;
			}
		}
		return true;
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
	 * @param maxCellDistance
	 *            how many cells away consider as candidate (e.g. distance 1 would consider 9 cells)
	 * @return A list of the nearest reachable vertexes, sorted in order of distance. (Note: these are live graph references)
	 * 
	 * @reachable reachable is a vertex property in the graph saying that the vertex was explored during floodfill after handling map polygons. It
	 *            doesn't guarantee pathability in the dynamic context because the floodfill isn't redone each time the dynamic graph is set (against
	 *            unit bodies).
	 */
	public static List<Vertex> findNearbyReachableVertexes(Point p, Vertex[][] graph, int graphWidth,
			int graphHeight, int maxCellDistance) {

		int ix = (int) (p.x / Holo.CELL_SIZE);
		; // corresponds to the closest lower left vertex
		int iy = (int) (p.y / Holo.CELL_SIZE);

		ArrayList<Vertex> prospects = new ArrayList<Vertex>();

		// Only consider vertexes inside the graph
		for (int cx = ix - maxCellDistance; cx <= ix + maxCellDistance; cx++) { // cx "current x"
			for (int cy = iy - maxCellDistance; cy <= iy + maxCellDistance; cy++) {
				Vertex v = new Vertex(cx, cy);
				if (isVertexWithinMapBoundaries(v, graphWidth, graphHeight)) {
					prospects.add(v);
				}
			}
		}

		ArrayList<VertexDist> vds = new ArrayList<VertexDist>();
		for (Vertex v : prospects) {
			if (graph[v.iy][v.ix].reachable) {
				vds.add(new VertexDist(graph[v.iy][v.ix], p.x, p.y, Holo.CELL_SIZE));
			}
		}
		vds.sort((VertexDist vd1, VertexDist vd2) -> (vd1.dist - vd2.dist >= 0) ? 1 : -1);

		ArrayList<Vertex> vertexes = new ArrayList<Vertex>();
		for (VertexDist vd : vds) {
			vertexes.add(vd.vertex); // note these are live graph nodes
		}
		return vertexes;
	}

	/**
	 *            
	 * @param maxCellDistance how many cells away consider as candidate (e.g. distance 1 would consider 9 cells)
	 * @return
	 */
	public static Vertex findClosestReachableVertex(Point p, Vertex[][] graph, int graphWidth, int graphHeight) {
		List<Vertex> result = findNearbyReachableVertexes(p, graph, graphWidth, graphHeight, 1);
		return result.size() > 0 ? result.get(0) : null;
	}

	public static boolean isPointInMap(Point p, int mapWidth, int mapHeight) {
		if (p.x < 0 || p.x > mapWidth)
			return false;
		if (p.y < 0 || p.y > mapHeight)
			return false;
		return true;
	}

	/**
	 * @return true if the vertex is within the map's graph
	 */
	public static boolean isVertexWithinMapBoundaries(Vertex v, int graphWidth, int graphHeight) {
		if (v.ix < 0 || v.ix >= graphWidth)
			return false;
		if (v.iy < 0 || v.iy >= graphHeight)
			return false;
		return true;
	}

	public static boolean isIndexInMap(int ix, int iy, int graphWidth, int graphHeight) {
		if (ix < 0 || ix >= graphWidth)
			return false;
		if (iy < 0 || iy >= graphHeight)
			return false;
		return true;
	}

	/**
	 * 
	 * Small data class that also stores distance for sorting purposes
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
