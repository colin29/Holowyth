package com.mygdx.holowyth.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.util.exceptions.HoloResourceNotFoundException;
import com.mygdx.holowyth.world.map.GameMap;
import com.mygdx.holowyth.world.map.StringNonNullMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
/**
 * A world is basically a namespace of maps. 
 */
@NonNullByDefault
public class World {
	
	private String name = "Untitled World";
	private String author = "unknown";

	@SuppressWarnings("null")
	Logger logger =  LoggerFactory.getLogger(this.getClass());
	private final StringNonNullMap<GameMap> maps = new StringNonNullMap<GameMap>();
//	private final StringNonNullMap<Town> town = new Town<>

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
	 * @throw {@link HoloResourceNotFoundException} if map isn't found
	 */
	public GameMap getNewMapInstance(String name) {
		if(maps.has(name)) {
			return new GameMap(maps.get(name));
		}else {
			throw new HoloResourceNotFoundException("Map not found: " + name);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
}