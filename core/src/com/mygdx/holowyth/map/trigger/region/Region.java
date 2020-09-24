package com.mygdx.holowyth.map.trigger.region;

import com.mygdx.holowyth.unit.interfaces.UnitInfo;

/**
 * A Map region for use in map triggers
 *
 */
public abstract class Region {
	
	/**
	 * Should not be null
	 */
	String name = "Untitled Region";
	
	protected Region() {
	}
	protected Region(Region src) {
		name = src.name;
	}
	
	/**
	 * Returns true if a unit's position is inside the region.
	 */
	abstract boolean containsUnit(UnitInfo u);
	
	public String getName() {
		return name;
	}
	/**
	 * Do NOT modify name of  a region inside a set, as name is being used as a key
	 * @return
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Polymorphic clone method
	 */
	public abstract Region cloneObject();
	
}
