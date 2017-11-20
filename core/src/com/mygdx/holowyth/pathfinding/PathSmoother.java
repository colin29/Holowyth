package com.mygdx.holowyth.pathfinding;

import java.util.ArrayList;
import java.util.ListIterator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.util.data.Point;
import com.mygdx.holowyth.util.data.Segment;

public class PathSmoother {

	public PathSmoother() {
	}

	float SEGMENT_LENGTH = 5;

	private Path path1s; // path after initial smoothing

	/**
	 * 
	 * @return A new path that is a smoothed version of the given path
	 */
	public Path smoothPath(Path origPath, ArrayList<Polygon> polys) {

		// Path is at least length 3
		if (origPath.size() <= 1) {
			return origPath;
		}

		path1s = origPath.deepCopy();

		// TODO: create a more sectioned version of the path.
		ListIterator<Point> iter = path1s.listIterator();
		Point prev, cur, next;

		prev = iter.next();
		cur = iter.next();

		while (iter.hasNext()) {
			next = iter.next();

			if (HoloPF.isEdgePathable(prev.x, prev.y, next.x, next.y, polys)) {

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

		Path path2s = doSecondarySmoothing(path1s, polys);
		return path2s;
	}

	private float threshold = 70; // in world units
	private float minSubDivision = 15;
	private float ratio = 2.0f / 3;

	ArrayList<Point> segPoints = new ArrayList<Point>();
	ArrayList<Point> nextPoints = new ArrayList<Point>();

	Segment bestCut;

	/**
	 * 
	 * @return A new path that is a smoothed version of the given path
	 */
	private Path doSecondarySmoothing(Path origPath, ArrayList<Polygon> polys) {

		Path path = origPath.deepCopy();

		if (path.size() < 3) {
			return path;
		}

		ListIterator<Point> iter = path.listIterator();
		Point a, b, c;

		a = iter.next();
		b = iter.next();
		c = iter.next();

		while (true) {
			Segment seg = new Segment(a.x, a.y, b.x, b.y);
			Segment next = new Segment(b.x, b.y, c.x, c.y);

			ArrayList<Float> segLengths = new ArrayList<Float>();
			ArrayList<Float> nextLengths = new ArrayList<Float>();

			segPoints.clear();
			nextPoints.clear();

			System.out.println(seg.getLength());

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
				segLengths.add(new Float(len));
				len *= ratio;
			}
			float lenNext = next.getLength();
			while (lenNext > minSubDivision) {
				nextLengths.add(new Float(lenNext));
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

			System.out.println(segPoints.size());
			System.out.println(nextPoints.size());

			System.out.println(segLengths.size());
			System.out.println(nextLengths.size());

			float maxScore = 0;
			float score;
			int bestSeg = -1, bestNext = -1;

			// For each combination of points, test making the cut. Keep the successful cut with the highest score
			// Score is (len*nextLen)

			for (int i = 0; i < segLengths.size(); i++) {
				for (int j = 0; j < nextLengths.size(); j++) {

					if (HoloPF.isEdgePathable(segPoints.get(i).x, segPoints.get(i).y, nextPoints.get(j).x,
							nextPoints.get(j).y, polys)) {
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
				System.out.format("Make the cut with points %s and %s %n", bestSeg, bestNext);
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
	public void render(ShapeRenderer shapeRenderer) {

		// Render the intermediate lines
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

}
