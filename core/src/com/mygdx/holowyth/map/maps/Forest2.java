package com.mygdx.holowyth.map.maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.map.Entrance;
import com.mygdx.holowyth.map.GameMap;
import com.mygdx.holowyth.map.Location;
import com.mygdx.holowyth.map.UnitMarker;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.units.MonsterStats;
import com.mygdx.holowyth.unit.units.MonsterTemplates;
import com.mygdx.holowyth.util.Holo;

public class Forest2 extends GameMap {

	@SuppressWarnings("unused")
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	{
		tilemapPath = Holo.mapsDirectory + "/forest1.tmx"; // re-use the same tilemap
		locations.add(new Location("default_spawn_location", 100, 300));
		setName("forest2");

		{
			UnitMarker m = new UnitMarker(MonsterTemplates.goblin);
			m.pos.set(500, 700);
			unitMarkers.add(m);
		}
		{
			UnitMarker m = new UnitMarker();
			m.pos.set(60, 245);
			m.name = "Sally";
			m.baseStats.set(MonsterStats.baseHuman);
			m.side = Side.PLAYER;
			m.animatedSpriteName = "pipo-charachip028d.png";
			unitMarkers.add(m);
		}

		locations.add(new Entrance("entrance_1", 50, 220).setDest("forest1", "entrance_2"));

	}
}
