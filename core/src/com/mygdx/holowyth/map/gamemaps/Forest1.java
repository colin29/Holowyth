package com.mygdx.holowyth.map.gamemaps;

import com.mygdx.holowyth.map.GameMap;
import com.mygdx.holowyth.map.UnitMarker;
import com.mygdx.holowyth.skill.skill.Skills;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.units.MonsterStats;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;

public class Forest1 extends GameMap{

	{
		tilemapPath = Holo.mapsDirectory + "/forest1.tmx";
		locations.put("default_spawn_location", new Point(100, 300));
		
		UnitMarker m = new UnitMarker();
		m.pos.set(100, 400);
		m.side = Side.PLAYER;
		m.name = "Sally";
		m.baseStats.set(MonsterStats.baseHuman);
		m.animatedSpriteName = "pipo-charachip028d.png";
		m.activeSkills.addAll(Skills.mageSkills);
		
		unitMarkers.add(m);
	}
	
}
