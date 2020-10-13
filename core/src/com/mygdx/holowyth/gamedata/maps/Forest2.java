package com.mygdx.holowyth.gamedata.maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.gamedata.units.MonsterStats;
import com.mygdx.holowyth.gamedata.units.Monsters;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.world.map.Entrance;
import com.mygdx.holowyth.world.map.GameMap;
import com.mygdx.holowyth.world.map.Location;
import com.mygdx.holowyth.world.map.UnitMarker;

public class Forest2 extends GameMap {

	@SuppressWarnings("unused")
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	{
		tilemapPath = Holo.mapsDirectory + "/forest2.1.tmx";
		locations.add(new Location("default_start_location", 350, 350));
		setName("forest2");

		locations.add(new Entrance("entrance_1", 30, 325).setDestToMap("forest1", "entrance_2"));
		locations.add(new Entrance("entrance_2", 1420, 290));
		
		addUnitMarker(720, 400, Monsters.goblin);

	}
}
