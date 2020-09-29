package com.mygdx.holowyth.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A world is basically a namespace of maps. 
 */
public class World {
	
	private String name = "Untitled World";
	private String author = "unknown";

	Logger logger = LoggerFactory.getLogger(this.getClass());
	private final StringNonNullMap<GameMap> maps = new StringNonNullMap<GameMap>();

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

	String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

}
