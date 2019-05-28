package com.mygdx.holowyth.combatDemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import com.mygdx.holowyth.collision.CircleCBInfo;
import com.mygdx.holowyth.collision.UnitAdapterCircleCB;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.HoloAssert;

/**
 * Holds units plus some related collections. Keeps these collections in sync.
 * 
 * @author Colin Ta
 *
 */
public class UnitCollection {

	private List<Unit> units = new ArrayList<Unit>();
	private List<UnitAdapterCircleCB> colBodies = new ArrayList<UnitAdapterCircleCB>();
	private BidiMap<Unit, CircleCBInfo> unitToColBody = new DualHashBidiMap<Unit, CircleCBInfo>();

	public void addUnit(Unit u) {

		UnitAdapterCircleCB cb = new UnitAdapterCircleCB(u);

		units.add(u);
		colBodies.add(cb);

		unitToColBody.put(u, cb);

		assertDataStructsAreEqualLength();
	}

	public void removeUnit(Unit u) {
		units.remove(u);
		colBodies.remove(unitToColBody.get(u));

		unitToColBody.remove(u);

		assertDataStructsAreEqualLength();
	}

	private void assertDataStructsAreEqualLength() {
		HoloAssert.assertEquals(units.size(), colBodies.size());
		HoloAssert.assertEquals(units.size(), unitToColBody.size());
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
	public List<UnitAdapterCircleCB> getColBodies() {
		return Collections.unmodifiableList(colBodies);
	}

	public Map<Unit, CircleCBInfo> unitToColBody() {
		return Collections.unmodifiableMap(unitToColBody);
	}

	public Map<CircleCBInfo, Unit> colBodyToUnit() {
		return Collections.unmodifiableMap(unitToColBody.inverseBidiMap());
	}

}
