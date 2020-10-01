package com.mygdx.holowyth.world.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;
import com.mygdx.holowyth.util.exceptions.HoloResourceNotFoundException;
import com.mygdx.holowyth.world.MapOfMapLocations;
import com.mygdx.holowyth.world.map.trigger.Trigger;
import com.mygdx.holowyth.world.map.trigger.region.Region;

/**
 * Holds all the map information.
 * @author Colin
 *
 */
public class GameMap {
	
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	private @NonNull String name = "Untitled Map";
	
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
	protected final StringNonNullMap<Region> regions = new StringNonNullMap<Region>();
	protected final List<Trigger> triggers = new ArrayList<>();
	
	
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
		name = src.name;
		tilemapPath = src.tilemapPath;
		for(String s : src.locations.keySet()) {
			Location loc = src.locations.get(s);
			locations.put(s, loc.cloneObject());
		}

		
		
		for(UnitMarker u: src.unitMarkers) {
			unitMarkers.add(new UnitMarker(u));
		}
		for(String name: src.regions.keySet()) {
			regions.put(name, src.regions.get(name).cloneObject());
		}
		for(Trigger t: src.triggers) {
			triggers.add(t.cloneObject());
		}
		
	}
	
	public MapOfMapLocations getLocations() {
		return locations;
	}
	
	/**
	 * @throw {@link #HoloResourceNotFoundException} if location not found
	 */
	public Location getLocation(String name) {
		Location loc = locations.get(name);
		if(loc == null) 
			throw new HoloResourceNotFoundException("Location '" + name + "' not found in map '" + this.name + "'");
		return loc;
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
	
	public @NonNull String getName() {
		return name;
	}
	/**
	 * Should not modify a map's name inside MapRepo, because map key should remain the same as name 
	 * @param name
	 */
	public void setName(@NonNull String name) {
		this.name = name;
	}

	public List<UnitMarker> getUnitMarkers() {
		return Collections.unmodifiableList(unitMarkers);
	}
	
	public void putRegion(Region r) {
		if(regions.has(r.getName())) {
			logger.info("Replaced old map region with same name");
		}
		regions.put(r.getName(), r);
	}
	public Region getRegion(String name) {
		return regions.get(name);
	}
	public void addTrigger(Trigger t) {
		triggers.add(t);
	}
	public boolean removeTrigger(Trigger t) {
		return triggers.remove(t);
	}
	public Collection<Region> getRegions(){
		return regions.values();
	}
	public List<Trigger> getTriggers(){
		return Collections.unmodifiableList(triggers);
	}
	public List<Entrance> getEntrances(){
		List<Entrance> entrances = new ArrayList<>();
		for(var loc : locations.values()) {
			if(loc instanceof Entrance)
				entrances.add((Entrance) loc);
		}
		return entrances;
	}
	
	
}
