package com.mygdx.holowyth.map.gamemaps;

import com.mygdx.holowyth.map.GameMap;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;

public class Forest1 extends GameMap{

	{
		tilemapPath = Holo.mapsDirectory + "/forest1.tmx";
		locations.put("default_spawn_location", new Point(100, 300));
	}
	
}
