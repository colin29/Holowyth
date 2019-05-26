package com.mygdx.holowyth.combatDemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mygdx.holowyth.knockback.CircleCB;
import com.mygdx.holowyth.unit.Unit;

public class UnitCollection {
	List<Unit> units = new ArrayList<Unit>();
	List<CircleCB> colBodies = new ArrayList<CircleCB>();

	public void addUnit(Unit u) {
		units.add(u);
		colBodies.add(null);
		// TODO:
	}

	public void removeUnit(Unit u) {
		// TODO:
	}

	public List<Unit> getUnits() {
		return Collections.unmodifiableList(units);
	}

	public List<CircleCB> getColBodies() {
		return Collections.unmodifiableList(colBodies);
	}
}
