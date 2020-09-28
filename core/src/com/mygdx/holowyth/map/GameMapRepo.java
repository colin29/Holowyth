package com.mygdx.holowyth.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.map.maps.Forest1;
import com.mygdx.holowyth.map.maps.Forest2;

/**
 * Stores a bunch of 'template' maps. To use the maps you should copy construct a new instance to use.
 *
 */
public class GameMapRepo {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	private final StringNonNullMap<GameMap> maps = new StringNonNullMap<GameMap>();

	{
		putMap(new Forest1());
		putMap(new Forest2());
	}
	
	
	public boolean putMap(GameMap map) {
		map.isTemplate = true;
		if(maps.has(map.getName()))
			logger.warn("Map {} already exists in repo, replacing", map.getName());
		return maps.put(map.getName(), map);
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
