package com.mygdx.holowyth.map;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * Holds all the map information.
 * @author Colin
 *
 */
public class GameMap {
	
	/**
	 * Whether this map is a template and meant to be copied first before using. 
	 * 
	 * In this case, GameMap repo stores a list of GameMap templates.
	 */
	public boolean isTemplate = false;
	
	/**
	 * Indicates tilemap path to the tilemap loader
	 */
	public String tilemapPath;
	/**
	 * 
	 * Carries tilemap and tile graphics, as well as collision info
	 * 
	 * Tilemap can only be null initially. Generally it's loaded/set by the tilemap loader.
	 */
	private TiledMap tilemap;
	
	
	protected final MapOfMapLocations locations = new MapOfMapLocations();
	
	public GameMap() {
	}
	
	public GameMap(TiledMap tiledMap){
		if(tiledMap==null)
			throw new HoloIllegalArgumentsException("tiled map can't be null");
		this.tilemap = tiledMap;
		
	}
	
	public MapOfMapLocations getLocations() {
		return locations;
	}

	
	public void setTilemap(TiledMap tilemap) {
		if(tilemap==null)
			throw new HoloIllegalArgumentsException("can't set tiledmap to null");
		this.tilemap = tilemap;
	}
	public TiledMap getTilemap() {
		return tilemap;
	}
	public boolean isTilemapLoaded() {
		return tilemap != null;
	}
	

	
}
