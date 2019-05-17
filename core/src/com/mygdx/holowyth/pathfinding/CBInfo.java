package com.mygdx.holowyth.pathfinding;

import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;

/**
 * Contains information about a unit's collision body.
 */
public class CBInfo {
	public float x, y;
	public float unitRadius;
	public UnitInfo unit;

	public CBInfo() {
	}

	public CBInfo(Unit unit) {
		this.unit = unit;
		unitRadius = unit.getRadius();
		x = unit.getX();
		y = unit.getY();
	}

	public UnitInfo getUnit() {
		return this.unit;
	}
}