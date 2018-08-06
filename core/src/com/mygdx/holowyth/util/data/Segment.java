package com.mygdx.holowyth.util.data;

public class Segment {
	public float x1, y1, x2, y2;

	public Segment(float sx, float sy, float dx, float dy) {
		this.x1 = sx;
		this.y1 = sy;
		this.x2 = dx;
		this.y2 = dy;
	}

	public Segment(Point a, Point b) {
		this.x1 = a.x;
		this.y1 = a.y;
		this.x2 = b.x;
		this.y2 = b.y;
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

		float newX2 = x1 + dy * factor;
		float newY2 = y1 + dy * factor;
		return new Segment(x1, y1, newX2, newY2);
	}
}
