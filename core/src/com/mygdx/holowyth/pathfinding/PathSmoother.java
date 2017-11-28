package com.mygdx.holowyth.pathfinding;

import java.util.ArrayList;
import java.util.ListIterator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.polygon.Polygons;
import com.mygdx.holowyth.util.data.Point;
import com.mygdx.holowyth.util.data.Segment;

public class PathSmoother {

	public PathSmoother() {
	}

	float SEGMENT_LENGTH = 5;

	private Path path0s; // original Path
	private Path path1s; // path after initial smoothing

	/**
	 * 
	 * @param unitRadius Radius of the pathing unit
	 * @return A new path that is a smoothed version of the given path
	 */
	public Path smoothPath(Path origPath, Polygons polys, ArrayList<CBInfo> cbs, float unitRadius) {

		// Path is at least length 3
		if (origPath.size() <= 1) {
			return origPath;
		}

		path0s = origPath.deepCopy();
		path1s = origPath.deepCopy();

		ListIterator<Point> iter = path1s.listIterator();
		Point prev, cur, next;

		prev = iter.next();
		cur = iter.next();

		while (iter.hasNext()) {
			next = iter.next();

			if (HoloPF.isEdgePathable(prev.x, prev.y, next.x, next.y, polys, cbs, unitRadius)) {

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

		Path path2s = doSecondarySmoothing(path1s, polys, cbs, unitRadius);
//		System.out.println("Number of isEdgePathableCalls: "  + testCount);
		return path2s;
	}

	private float s1SegLength = 15f;

	/**
	 * @param path
	 *            Assumes path is at least length 2
	 * @return A path that has been further broken up into segments no more than a set length.
	 */
	@SuppressWarnings("unused")
	private Path segmentPath(Path origPath) {

		Path path = origPath.deepCopy();

		ListIterator<Point> iter = path.listIterator();
		Point a, b;
		a = iter.next();
		b = iter.next();
		
		float dx, dy;
		int numParts;
		Point p;
		
		while (true) {
			dx = b.x - a.x;
			dy = b.y - a.y;

			Segment seg = new Segment(a, b);
			
			float len = seg.getLength();
			if (len > s1SegLength) {
				numParts = (int) Math.ceil((len / s1SegLength));
				
				//need to insert n-1 parts in between "a" and "b"
				//iterator is currently after "b"
				iter.previous();
				for(int i=1; i<numParts; i++){
					p = new Point(a.x + dx*i/numParts, a.y + dy*i/numParts); 
					iter.add(p);
				}
				//iterator is currently before "b". We want it after "b".
				iter.next();
			}
			
			if(iter.hasNext()){
				a = b;
				b = iter.next();
			}else{
				System.out.format("Slicer: returned a path with length %s %n", path.size());
				return path;
			}
		}

	}

	private float thresholdRegular = 70;//70; // in world units
	private float minSubDivisionRegular = 15;
	private float ratio = 2.0f / 3;

	private float thresholdLast = 4; //the last part (2 segements) of a path is always smoothed. (The first part is too)
	private float minSubDivisionLast = 2;
	
	ArrayList<Point> segPoints = new ArrayList<Point>();
	ArrayList<Point> nextPoints = new ArrayList<Point>();

	Segment bestCut;

	
	int testCount;
	/**
	 * 
	 * @return A new path that is a smoothed version of the given path
	 */
	private Path doSecondarySmoothing(Path origPath, Polygons polys, ArrayList<CBInfo> cbs, float unitRadius) {
		
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


			
			//We smooth the first and last parts of the path more rigorously
			
			//iterator is after "c". If this is the last part, iter.next() should return false;
			
			float threshold;
			float minSubDivision;
			
			if(!iter.hasNext() || isFirstSeg){
				threshold = thresholdLast;
				minSubDivision = minSubDivisionLast;
				isFirstSeg = false;
			}else{
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

			float maxScore = 0;
			float score;
			int bestSeg = -1, bestNext = -1;

			// For each combination of points, test making the cut. Keep the successful cut with the highest score
			// Score is (len*nextLen)

			for (int i = 0; i < segLengths.size(); i++) {
				for (int j = 0; j < nextLengths.size(); j++) {
					testCount+=1;
					if (HoloPF.isEdgePathable(segPoints.get(i).x, segPoints.get(i).y, nextPoints.get(j).x,
							nextPoints.get(j).y, polys, cbs, unitRadius)) {
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
//				System.out.format("Make the cut with points %s and %s %n", bestSeg, bestNext);
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

}
