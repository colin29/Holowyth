package com.mygdx.holowyth.knockback.collision;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.util.dataobjects.Segment;

public class World {
	private List<Circle> circles = new ArrayList<Circle>();
	private final Segment segment = new Segment(0, 0, 0, 0);
	private Circle keyCircle;

	List<Circle> getCircles() {
		return circles;
	}

	public void setKeyCircle(Circle circle) {
		if (circles.contains(circle)) {
			keyCircle = circle;
		} else {
			LoggerFactory.getLogger(this.getClass()).warn("KeyCircle must be a circle contained in World, set ignored");
		}
	}

	public Circle getKeyCircle() {
		return keyCircle;
	}

	/**
	 * Returns a fresh copy
	 */
	public Segment getSegment() {
		return new Segment(segment);
	}

	/**
	 * May also update other dependent World state
	 */
	public void setSegment(float x1, float y1, float x2, float y2) {
		segment.set(x1, y1, x2, y2);

	}

}
