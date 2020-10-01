package com.mygdx.holowyth.collision;

import com.mygdx.holowyth.util.dataobjects.Segment;
import com.mygdx.holowyth.world.map.obstacle.OrientedSeg;

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
