package com.mygdx.holowyth.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.mygdx.holowyth.map.triggers.Region;
import com.mygdx.holowyth.util.dataobjects.Point;
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
	 * Carries tilemap and tile graphics, as well as collision info <br>
	 * Tilemap can only be null initially. Generally it's loaded/set by the tilemap loader.
	 */
	private TiledMap tilemap;
	protected final MapOfMapLocations locations = new MapOfMapLocations();
	protected final List<UnitMarker> unitMarkers = new ArrayList<UnitMarker>();
	
	protected final List<Region> regions = new ArrayList<Region>();
	
	public GameMap() {
	}
	
	public GameMap(TiledMap tiledMap){
		if(tiledMap==null)
			throw new HoloIllegalArgumentsException("tiled map can't be null");
		this.tilemap = tiledMap;
	}
	/**
	 * Copy constructor. Doesn't copy the tilemap, you must load it from disk again.
	 */
	public GameMap(GameMap src) {
		tilemapPath = src.tilemapPath;
		for(String s : src.locations.keySet()) {
			locations.put(s, src.locations.get(s));
		}
		for(UnitMarker u: src.unitMarkers) {
			unitMarkers.add(new UnitMarker(u));
		}
		
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
	
	public List<UnitMarker> getUnitMarkers() {
		return Collections.unmodifiableList(unitMarkers);
	}
	

	
}
