package com.mygdx.holowyth.map;

import com.mygdx.holowyth.util.dataobjects.Point;

public class MapOfMapLocations extends StringNonNullMap<Location> {
	public boolean add(Location loc) {
		return put(loc.name, loc);
	}
}
