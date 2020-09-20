package com.mygdx.holowyth.map;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

public class MapOfMapLocations {
	/**
	 * Location name --> Location point
	 * key/values are never null
	 */
	private final Map<String, Point> locations = new LinkedHashMap<>();
	
	
	/**
	 * If location is not found, returns null
	 * @return
	 */
	public Point get(String name){
		if(name==null) {
			throw new HoloIllegalArgumentsException("can't query null name");
		}
		return locations.get(name);
	}
	public boolean has(String name) {
		return get(name)!=null;
	}
	
	public boolean put(String name, Point location) {
		if(name==null)
			throw new HoloIllegalArgumentsException("location name can't be null");
		if(location==null)
			throw new HoloIllegalArgumentsException("location coordinates can't be null");
		
		boolean oldLocExisted = has(name);
		locations.put(name, location);
		return oldLocExisted;
	}
	
	public Set<String> keySet(){
		return Collections.unmodifiableSet(locations.keySet());
	}
}
