package com.mygdx.holowyth.map;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mygdx.holowyth.map.maps.Forest1;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * Stores a bunch of 'template' maps. To use the maps you should copy construct a new instance to use.
 *
 */
public class GameMapRepo {
	
	
	private final StringNonNullMap<GameMap> maps = new StringNonNullMap<GameMap>();

	{
		maps.put("forest1", new Forest1());
	}
	
	
	public boolean putMap(String name, GameMap map) {
		map.isTemplate = true;
		return maps.put(name, map);
	}

	public boolean hasMap(String name) {
		return maps.has(name);
	}
	
	/**
	 * If map is not found, returns null
	 */
	public GameMap getNewMapInstance(String name) {
		return maps.has(name) ? new GameMap(maps.get(name)) : null;
	}

	
}
