package com.mygdx.holowyth.map;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

class MapOfMapLocations {
	/**
	 * Location name --> Location point
	 * key/values are never null
	 */
	private final Map<String, Point> locations = new LinkedHashMap<>();
	
	
	/**
	 * Can return null, that means location is not found 
	 * @return
	 */
	public Point getLocation(String name){
		if(name==null) {
			throw new HoloIllegalArgumentsException("can't query null name");
		}
		return locations.get(name);
	}
	public boolean hasLocation(String name) {
		return getLocation(name)!=null;
	}
	
	public boolean putLocation(String name, Point location) {
		if(name==null)
			throw new HoloIllegalArgumentsException("location name can't be null");
		if(location==null)
			throw new HoloIllegalArgumentsException("location coordinates can't be null");
		
		boolean oldLocExisted = hasLocation(name);
		locations.put(name, location);
		return oldLocExisted;
	}
}
