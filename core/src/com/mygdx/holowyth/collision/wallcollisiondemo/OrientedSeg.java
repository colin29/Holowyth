package com.mygdx.holowyth.collision.wallcollisiondemo;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.util.dataobjects.Segment;

public class OrientedSeg extends Segment {

	public OrientedSeg(float x1, float y1, float x2, float y2) {
		super(x1, y1, x2, y2);
	}

	public boolean isClockwise; // clockwise means "outside" is left, relative to where the segment is pointing

	/**
	 * Returns the segment that would be found if you moved this segment X units outwards
	 */
	public OrientedSeg getOutwardlyDisplacedSegment(float distance) {

		Vector2 vec = new Vector2(x2 - x1, y2 - y1);

		// find the direction of displacement
		vec.nor();
		vec.rotate(isClockwise ? 90 : -90);

		// multiply to get the displacement vector
		vec.scl(distance);

		var outwardSeg = new OrientedSeg(x1 + vec.x, y1 + vec.y, x2 + vec.x, y2 + vec.y);
		outwardSeg.isClockwise = isClockwise;
		return outwardSeg;
	}
}
