package com.mygdx.holowyth.map;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mygdx.holowyth.map.gamemaps.Forest1;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * Stores a bunch of 'template' maps. To use the maps you should copy construct a new instance to use.
 *
 */
public class GameMapRepo {
	
	/**
	 * Map name --> Location point
	 * key/values are never null, enforced by this class
	 */
	private final Map<String, GameMap> maps = new LinkedHashMap<>();

	{
		maps.put("forest1", new Forest1());
	}
	
	/**
	 * If map is not found, returns null
	 * @return
	 */
	public GameMap getMap(String name){
		if(name==null) {
			throw new HoloIllegalArgumentsException("can't query null name");
		}
		return maps.get(name);
	}
	public boolean hasMap(String name) {
		return getMap(name)!=null;
	}
	
	/**
	 * Note: sets map.isTemplate = true;
	 */
	public boolean putMap(String name, GameMap map) {
		if(name==null)
			throw new HoloIllegalArgumentsException("map name can't be null");
		if(map==null)
			throw new HoloIllegalArgumentsException("map can't be null");
		
		boolean keyPreviouslyExisted = hasMap(name);
		maps.put(name, map);
		map.isTemplate = true;
		return keyPreviouslyExisted;
	}
}
