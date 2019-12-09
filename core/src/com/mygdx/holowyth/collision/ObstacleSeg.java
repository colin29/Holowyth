package com.mygdx.holowyth.collision;

import com.mygdx.holowyth.util.dataobjects.OrientedSeg;
import com.mygdx.holowyth.util.dataobjects.Segment;

public class ObstacleSeg implements Collidable {
	private final OrientedSeg wrapped;

	public ObstacleSeg(OrientedSeg wrapped) {
		this.wrapped = wrapped;
	}

	public Segment getSegment() {
		return new Segment(wrapped);
	}

	public boolean isClockwise() {
		return wrapped.isClockwise;
	}

}
