package com.mygdx.holowyth.combatDemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import com.mygdx.holowyth.knockback.CircleCBInfo;
import com.mygdx.holowyth.knockback.UnitAdapterCircleCB;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.HoloAssert;

/**
 * Holds units plus some related collections. Keeps these collections in sync.
 * 
 * @author Colin Ta
 *
 */
public class UnitCollection {

	List<Unit> units = new ArrayList<Unit>();
	List<UnitAdapterCircleCB> colBodies = new ArrayList<UnitAdapterCircleCB>();
	List<CircleCBInfo> circleCBInfoView = new ArrayList<CircleCBInfo>();
	BidiMap<Unit, CircleCBInfo> unitToColBody = new DualHashBidiMap<Unit, CircleCBInfo>();

	public void addUnit(Unit u) {

		UnitAdapterCircleCB cb = new UnitAdapterCircleCB(u);

		units.add(u);
		colBodies.add(cb);
		circleCBInfoView.add(cb);

		unitToColBody.put(u, cb);

		assertDataStructsAreEqualLength();
	}

	public void removeUnit(Unit u) {
		units.remove(u);
		colBodies.remove(unitToColBody.get(u));
		circleCBInfoView.remove(unitToColBody.get(u));

		unitToColBody.remove(u);

		assertDataStructsAreEqualLength();
	}

	private void assertDataStructsAreEqualLength() {
		HoloAssert.assertEquals(units.size(), colBodies.size());
		HoloAssert.assertEquals(units.size(), circleCBInfoView.size());
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
	public List<CircleCBInfo> getColBodies() {
		return Collections.unmodifiableList(circleCBInfoView);
	}
}
