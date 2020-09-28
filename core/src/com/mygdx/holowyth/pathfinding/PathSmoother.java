package com.mygdx.holowyth.pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.holowyth.map.obstacledata.OrientedSeg;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;

public class PathSmoother {

	public PathSmoother() {
	}

	float SEGMENT_LENGTH = 5;

	private Path path0s; // original Path
	private Path path1s; // path after initial smoothing
	private Path path2s;

	/**
	 * @param origPath
	 *            Should not be null
	 * @param unitRadius
	 *            Radius of the pathing unit
	 * @return A new path that is a smoothed version of the given path
	 */
	public Path smoothPath(Path origPath, List<OrientedSeg> obstacleExpandedSegs,
			List<Point> obstaclePoints, ArrayList<UnitCB> cbs, float unitRadius) {

		path0s = origPath.deepCopy();
		path1s = null;
		path2s = null;

		// Path is at least length 3
		if (origPath.size() <= 1) {
			return origPath;
		}

		path1s = origPath.deepCopy();
		ListIterator<Point> iter = path1s.listIterator();
		Point prev, cur, next;

		prev = iter.next();
		cur = iter.next();

		while (iter.hasNext()) {
			next = iter.next();

			if (HoloPF.isSegmentPathable(prev.x, prev.y, next.x, next.y, obstacleExpandedSegs,
					obstaclePoints, cbs, unitRadius)) {

				iter.previous();
				iter.previous();
				iter.remove();
				iter.next();

				cur = next;
			} else {
				prev = cur;
				cur = next;
			}
		}

		path2s = doSecondarySmoothing(path1s, obstacleExpandedSegs,
				obstaclePoints, cbs, unitRadius);
		// System.out.println("Number of isEdgePathableCalls: " + testCount);
		return path2s;
	}

	private float thresholdRegular = 70;// 70; // in world units
	private float minSubDivisionRegular = 15;
	private float ratio = 2.0f / 3;

	private float thresholdLast = 4; // the last part (2 segements) of a path is always smoothed. (The first part is too)
	private float minSubDivisionLast = 2;

	ArrayList<Point> segPoints = new ArrayList<Point>();
	ArrayList<Point> nextPoints = new ArrayList<Point>();

	Segment bestCut;

	int testCount;

	/**
	 * 
	 * @return A new path that is a smoothed version of the given path
	 */
	private Path doSecondarySmoothing(Path origPath, List<OrientedSeg> obstacleExpandedSegs,
			List<Point> obstaclePoints, ArrayList<UnitCB> cbs, float unitRadius) {

		testCount = 0;

		Path path = origPath.deepCopy();

		if (path.size() < 3) {
			return path;
		}

		ListIterator<Point> iter = path.listIterator();
		Point a, b, c;

		a = iter.next();
		b = iter.next();
		c = iter.next();

		boolean isFirstSeg = true;

		while (true) {
			Segment seg = new Segment(a.x, a.y, b.x, b.y);
			Segment next = new Segment(b.x, b.y, c.x, c.y);

			ArrayList<Float> segLengths = new ArrayList<Float>();
			ArrayList<Float> nextLengths = new ArrayList<Float>();

			segPoints.clear();
			nextPoints.clear();

			// We smooth the first and last parts of the path more rigorously

			// iterator is after "c". If this is the last part, iter.next() should return false;

			float threshold;
			float minSubDivision;

			if (!iter.hasNext() || isFirstSeg) {
				threshold = thresholdLast;
				minSubDivision = minSubDivisionLast;
				isFirstSeg = false;
			} else {
				threshold = thresholdRegular;
				minSubDivision = minSubDivisionRegular;
			}

			// Consider every segment that is longer than the threshold:

			// If the segment is not longer than the threshold, move on to the next segment
			if (seg.getLength() < threshold)
				if (iter.hasNext()) {
					a = b;
					b = c;
					c = iter.next();
					continue;
				} else {
					return path;
				}

			float len = seg.getLength();
			while (len > minSubDivision) {
				segLengths.add(Float.valueOf(len));
				len *= ratio;
			}
			float lenNext = next.getLength();
			while (lenNext > minSubDivision) {
				nextLengths.add(Float.valueOf(lenNext));
				lenNext *= ratio;
			}

			// calculate the points for the potential cut

			float dx, dy;
			dx = b.x - a.x;
			dy = b.y - a.y;
			for (Float segLen : segLengths) {
				segPoints.add(new Point(b.x - dx * segLen / seg.getLength(), b.y - dy * segLen / seg.getLength()));
			}
			dx = c.x - b.x;
			dy = c.y - b.y;
			for (Float segLen : nextLengths) {
				nextPoints.add(new Point(b.x + dx * segLen / next.getLength(), b.y + dy * segLen / next.getLength()));
			}

			float maxScore = 0;
			float score;
			int bestSeg = -1, bestNext = -1;

			// For each combination of points, test making the cut. Keep the successful cut with the highest score
			// Score is (len*nextLen)

			for (int i = 0; i < segLengths.size(); i++) {
				for (int j = 0; j < nextLengths.size(); j++) {
					testCount += 1;
					if (HoloPF.isSegmentPathable(segPoints.get(i).x, segPoints.get(i).y, nextPoints.get(j).x,
							nextPoints.get(j).y, obstacleExpandedSegs,
							obstaclePoints, cbs, unitRadius)) {
						score = segLengths.get(i) * nextLengths.get(j);
						if (score > maxScore) {
							maxScore = score;
							bestSeg = i;
							bestNext = j;
						}
					}
				}
			}
			if (maxScore > 0) {
				// If there was a legal best cut, make that cut:
				// System.out.format("Make the cut with points %s and %s %n", bestSeg, bestNext);
				bestCut = new Segment(segPoints.get(bestSeg).x, segPoints.get(bestSeg).y, nextPoints.get(bestNext).x,
						nextPoints.get(bestNext).y);

				// Remove b. The iterator is currently at the end of c.
				iter.previous();
				iter.previous();
				iter.remove();

				// Insert the new vertices. Iterator is currently between "a" and "c".
				if (bestSeg > 0) {
					iter.add(new Point(segPoints.get(bestSeg)));
				}
				if (bestNext > 0) {
					iter.add(new Point(nextPoints.get(bestNext)));
				}

				// Iterator is currently before "c". We wish to have a,b,c = {previous vertex}, c, c.next();

				a = iter.previous();
				iter.next();
				b = iter.next();
				if (iter.hasNext()) {
					c = iter.next();
				} else {
					return path;
				}
			} else { // if no legal cut was found, do nothing
				if (iter.hasNext()) {
					a = b;
					b = c;
					c = iter.next();
				} else {
					return path;
				}
			}

		}
	}

	/**
	 * Render debugging info
	 */
	public void renderIntermediateLinesDebug(ShapeRenderer shapeRenderer) {

		// Render the initial and intermediate lines
		HoloPF.renderPath(this.path0s, Color.PINK, false, 2f, shapeRenderer);
		HoloPF.renderPath(this.path1s, Color.FIREBRICK, true, 2f, shapeRenderer);

		// Render some of the points used in secondary smoothing

		// shapeRenderer.setColor(Color.MAGENTA);
		// shapeRenderer.begin(ShapeType.Filled);
		// for (Point p : segPoints) {
		// shapeRenderer.circle(p.x, p.y, 3f);
		// }
		// shapeRenderer.end();
		// shapeRenderer.setColor(Color.GREEN);
		// shapeRenderer.begin(ShapeType.Filled);
		// for (Point p : nextPoints) {
		// shapeRenderer.circle(p.x, p.y, 3f);
		// }
		// shapeRenderer.end();
		//
		// shapeRenderer.setColor(Color.BLACK);
		// shapeRenderer.begin(ShapeType.Filled);
		// shapeRenderer.rectLine(bestCut.sx, bestCut.sy, bestCut.dx, bestCut.dy, 3f);
		// shapeRenderer.end();

	}

	/**
	 * @return The unsmoothed and partially smoothed path of the last unit processed. The fields can be null if the pathfinding failed.
	 */
	public PathsInfo getPathInfo() {
		PathsInfo paths = new PathsInfo();
		paths.pathSmoothed0 = this.path0s;
		paths.pathSmoothed1 = this.path1s;
		paths.finalPath = this.path2s;
		return paths;
	}

	/**
	 * Data class, stores the unsmoothed and partially smoothed paths of a unit.
	 */
	public static class PathsInfo {
		public Path pathSmoothed0;
		public Path pathSmoothed1;
		public Path finalPath;
	}

}
