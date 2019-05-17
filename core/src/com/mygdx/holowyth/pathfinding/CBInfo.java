package com.mygdx.holowyth.pathfinding;

import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.UnitOrderable;

/**
 * Contains information about a unit's collision body.
 */
public class CBInfo {
	public float x, y;
	public float unitRadius;
	public UnitOrderable unit;

	public CBInfo() {
	}

	public CBInfo(Unit unit) {
		this.unit = unit;
		unitRadius = unit.getRadius();
		x = unit.getX();
		y = unit.getY();
	}

	public UnitOrderable getUnit() {
		return this.unit;
	}
}