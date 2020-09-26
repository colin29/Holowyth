package com.mygdx.holowyth.map;

import com.mygdx.holowyth.map.maps.Forest1;

/**
 * Stores a bunch of 'template' maps. To use the maps you should copy construct a new instance to use.
 *
 */
public class GameMapRepo {
	
	
	private final StringNonNullMap<GameMap> maps = new StringNonNullMap<GameMap>();

	{
		GameMap m = new Forest1();
		maps.put(m.getName(), m);
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
