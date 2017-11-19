package com.mygdx.holowyth.pathfinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.util.data.Point;
import com.mygdx.holowyth.util.data.Segment;

public class PathSmoother {

	public PathSmoother() {
	}

	float SEGMENT_LENGTH = 5;

	public Path path2s;

	public Path smoothPath(Path origPath, ArrayList<Polygon> polys) {

		// Path is at least length 3
		if (origPath.size() <= 1) {
			return origPath;
		}

		Path path = new Path();
		for (Point p : origPath) {
			path.add(new Point(p));
		}

		// create a more sectioned version of the path.
		ListIterator<Point> iter = path.listIterator();
		Point prev, cur, next;

		prev = iter.next();
		cur = iter.next();

		while (iter.hasNext()) {
			next = iter.next();

			if (HoloPF.isEdgePathable(prev.x, prev.y, next.x, next.y, polys)) {
				// Can connect prev directly to next, so can remove cur from list.

				// System.out.println("Size of array: " + path.size());
				// System.out.println(iter.nextIndex());
				iter.previous();
				iter.previous();
				// System.out.println(iter.nextIndex());
				// System.out.println(iter.previousIndex());
				iter.remove();
				// System.out.println("Size of array: " + path.size());
				// System.out.println(iter.nextIndex());
				iter.next();
				cur = next;
			} else {
				prev = cur;
				cur = next;
			}
		}

		// Path pathFinal = new Path();
		// for(Point p : path) {
		// pathFinal.add(new Point(p));
		// }

		path2s = new Path();
		for (Point p : path) {
			path2s.add(new Point(p));
		}

		doSecondarySmoothing(path2s, polys);

		return path;
	}

	private float threshold = 100; // in world units
	private float minSubDivision = 10;
	private float ratio = 2.0f / 3;

	ArrayList<Point> segPoints = new ArrayList<Point>();
	ArrayList<Point> nextPoints = new ArrayList<Point>();

	Segment bestCut;

	public Path doSecondarySmoothing(Path path, ArrayList<Polygon> polys) {
		

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

			// consider every segment that is longer than the threshold

			if (seg.getLength() >= threshold) {

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

				// calculate the points

				float dx, dy;
				dx = b.x - a.x;
				dy = b.y - a.y;
				for (Float segLen : segLengths) {
					segPoints.add(new Point(b.x - dx * segLen / seg.getLength(), b.y - dy * segLen / seg.getLength()));
				}
				dx = c.x - b.x;
				dy = c.y - b.y;
				for (Float segLen : nextLengths) {
					nextPoints
							.add(new Point(b.x + dx * segLen / next.getLength(), b.y + dy * segLen / next.getLength()));
				}

				System.out.println(segPoints.size());
				System.out.println(nextPoints.size());

				System.out.println(segLengths.size());
				System.out.println(nextLengths.size());

				float maxScore = 0;
				float score;
				int bestSeg = -1, bestNext = -1;

				// For each combination, try making a single cut and keep the successful cut with the highest score
				// (len*nextLen)

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
					bestCut = new Segment(segPoints.get(bestSeg).x, segPoints.get(bestSeg).y,
							nextPoints.get(bestNext).x, nextPoints.get(bestNext).y);

					int netVertexes = -1; // net change in number of vertexes in the path. If a successful cut was made
											// with
					// 0,0, that means 1 vertex was removed and none added

					// Remove b. The iterator is currently at the end of c.
					iter.previous();
					iter.previous();
					iter.remove();

					// Insert the new vertices. Iterator is currently between "a" and "c".
					if (bestSeg > 0) {
						iter.add(new Point(segPoints.get(bestSeg)));
						netVertexes += 1;
					}
					if (bestNext > 0) {
						iter.add(new Point(nextPoints.get(bestNext)));
						netVertexes += 1;
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
				}
			} else {
				// if the segment is not longer than the threshold, we want to advance by one

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
	 * Render some points for debugging
	 */
	public void render(ShapeRenderer shapeRenderer) {
		shapeRenderer.setColor(Color.MAGENTA);
		shapeRenderer.begin(ShapeType.Filled);
		for (Point p : segPoints) {
			shapeRenderer.circle(p.x, p.y, 3f);
		}
		shapeRenderer.end();
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.begin(ShapeType.Filled);
		for (Point p : nextPoints) {
			shapeRenderer.circle(p.x, p.y, 3f);
		}
		shapeRenderer.end();

		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.rectLine(bestCut.sx, bestCut.sy, bestCut.dx, bestCut.dy, 3f);
		shapeRenderer.end();

	}

}
