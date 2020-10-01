package com.mygdx.holowyth.world.map;

public class MapOfMapLocations extends StringNonNullMap<Location> {
	public boolean add(Location loc) {
		return put(loc.name, loc);
	}
}
