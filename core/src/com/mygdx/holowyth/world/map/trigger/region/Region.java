package com.mygdx.holowyth.world.map.trigger.region;

import java.util.List;

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
	public abstract boolean containsUnit(UnitInfo u);
	public abstract boolean containsAnyUnit(List<? extends UnitInfo> units);
	
	
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
