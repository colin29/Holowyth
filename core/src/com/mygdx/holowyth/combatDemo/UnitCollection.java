package com.mygdx.holowyth.combatDemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mygdx.holowyth.knockback.CircleCB;
import com.mygdx.holowyth.unit.Unit;

/**
 * Holds units plus some related collections. Keeps these collections in sync.
 * 
 * @author Colin Ta
 *
 */
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

	/**
	 * The list itself is read-only, though elements can be modified
	 */
	public List<Unit> getUnits() {
		return Collections.unmodifiableList(units);
	}

	/**
	 * The list itself is read-only, though elements can be modified
	 */
	public List<CircleCB> getColBodies() {
		return Collections.unmodifiableList(colBodies);
	}
}
