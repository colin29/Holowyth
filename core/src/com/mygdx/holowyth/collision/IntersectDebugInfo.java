package com.mygdx.holowyth.collision;

import com.badlogic.gdx.math.Vector2;

public class IntersectDebugInfo {
	public final Vector2 initial = new Vector2();
	public final Vector2 delta = new Vector2();
	public final Vector2 deltaNormalized = new Vector2();

	public final Vector2 initialToCircleCenter = new Vector2();
	public Vector2 initialToClosestPoint = new Vector2();

	public final Vector2 closestPoint = new Vector2();
	public float closestDistToCenter;

	public final Vector2 intersectPoint = new Vector2();

	/**
	 * Describes how far intersectPoint is along the motion line segment. Within [0,1] means it lies on the motion segment
	 */
	public float pOfIntersectPoint;

	/**
	 * Is the angle in rads, from circle center to intersect point, 0 degrees is at (0,radius), spinning CCW
	 */
	public float angleOfCircleAtIntersectDegrees;
}
