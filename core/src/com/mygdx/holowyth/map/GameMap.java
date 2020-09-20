package com.mygdx.holowyth.map;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

public class GameMap {

	/**
	 * Holds the terrain and tile data to draw the environment
	 */
	private final TiledMap tilemap;
	private final MapOfMapLocations locations = new MapOfMapLocations();
	
	
	public GameMap(TiledMap tiledMap){
		if(tiledMap==null)
			throw new HoloIllegalArgumentsException("tiled map can't be null");
		this.tilemap = tiledMap;
		
	}


	public TiledMap getTilemap() {
		return tilemap;
	}
	

	
}
