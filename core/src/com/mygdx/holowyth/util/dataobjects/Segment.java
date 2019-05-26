package com.mygdx.holowyth.util.dataobjects;

import com.mygdx.holowyth.util.exceptions.HoloAssertException;

public class Segment {
	public float x1, y1, x2, y2;

	public Segment(float x1, float y1, float x2, float y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	public Segment(Point a, Point b) {
		this.x1 = a.x;
		this.y1 = a.y;
		this.x2 = b.x;
		this.y2 = b.y;
	}

	public Segment(Segment source) {
		this.x1 = source.x1;
		this.y1 = source.y1;
		this.x2 = source.x2;
		this.y2 = source.y2;
	}

	public void set(float x1, float y1, float x2, float y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	public float getLength() {
		return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}

	/**
	 * 
	 * 
	 * @return the angle of the line segment, in rads
	 */
	public float getAngle() {
		float angle = (float) Math.acos((x2 - x1) / getLength());
		if ((y2 - y1) < 0) {
			angle = (float) (2 * Math.PI - angle);
		}
		return angle;
	}

	/**
	 * Extends the segment's end point
	 * 
	 * @param factor
	 * @return A new scaled segment
	 */
	public Segment scaleBy(float factor) {
		float dx = x2 - x1;
		float dy = y2 - y1;

		float newX2 = x1 + dx * factor;
		float newY2 = y1 + dy * factor;
		return new Segment(x1, y1, newX2, newY2);
	}

	/**
	 * Modifying returned point does not affect the original Segment
	 */
	public Point startPoint() {
		return new Point(x1, y1);
	}

	/**
	 * Modifying returned point does not affect the original Segment
	 */
	public Point endPoint() {
		return new Point(x2, y2);
	}

	/**
	 * Warning: Comparison is exact, does not use margin of error
	 */
	public void assertEquals(Segment other) {
		if (this.x1 != other.x1 || this.y1 != other.y1 || this.x2 != other.x2 || this.y2 != other.y2) {
			throw new HoloAssertException("segments are not (exactly) equal in value");
		}
	}
}
