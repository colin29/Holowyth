package com.mygdx.holowyth.pathfinding;

import com.mygdx.holowyth.unit.interfaces.UnitInfo;

/**
 * Contains information about a unit's (circular) collision body.
 */
public class CBInfo {
	public float x, y;
	public float unitRadius;

	public CBInfo() {
	}

	public CBInfo(UnitInfo unit) {
		unitRadius = unit.getRadius();
		x = unit.getX();
		y = unit.getY();
	}

}