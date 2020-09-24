package com.mygdx.holowyth.map.triggers;

import com.mygdx.holowyth.unit.interfaces.UnitInfo;

/**
 * A Map region for use in map triggers
 *
 */
public abstract class Region {
	
	/**
	 * Should not be null
	 */
	public String name = "Untitled Region";
	
	/**
	 * Returns true if a unit's position is inside the region.
	 */
	abstract boolean containsUnit(UnitInfo u);

}
