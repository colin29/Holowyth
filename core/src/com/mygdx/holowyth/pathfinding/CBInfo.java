package com.mygdx.holowyth.pathfinding;

import com.mygdx.holowyth.unit.Unit;

/**
 * Contains information about a unit's collision body.
 */
public class CBInfo {
	public float x, y;
	public float unitRadius;
	public Unit unit;

	public CBInfo() {
	}

	public CBInfo(Unit unit) {
		this.unit = unit;
		unitRadius = unit.getRadius();
		unit.x = unit.getX();
		unit.y = unit.getY();
	}

	public Unit getUnit() {
		return this.unit;
	}
}