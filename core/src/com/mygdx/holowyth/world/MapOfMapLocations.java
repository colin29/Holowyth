package com.mygdx.holowyth.world;

import com.mygdx.holowyth.world.map.Location;
import com.mygdx.holowyth.world.map.StringNonNullMap;

public class MapOfMapLocations extends StringNonNullMap<Location> {
	public boolean add(Location loc) {
		return put(loc.name, loc);
	}
}
