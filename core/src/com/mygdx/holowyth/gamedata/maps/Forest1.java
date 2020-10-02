package com.mygdx.holowyth.gamedata.maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.gamedata.units.MonsterStats;
import com.mygdx.holowyth.gamedata.units.Monsters;
import com.mygdx.holowyth.skill.skill.Skills;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.world.map.Entrance;
import com.mygdx.holowyth.world.map.GameMap;
import com.mygdx.holowyth.world.map.Location;
import com.mygdx.holowyth.world.map.UnitMarker;
import com.mygdx.holowyth.world.map.trigger.Trigger;
import com.mygdx.holowyth.world.map.trigger.UnitEntersRegion;
import com.mygdx.holowyth.world.map.trigger.region.RectRegion;

@SuppressWarnings("unused")
public class Forest1 extends GameMap {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	{
		tilemapPath = Holo.mapsDirectory + "/forest1.tmx";
		setName("forest1");

		{
			locations.add(new Location("default_spawn_location", 350, 350));
			locations.add(new Entrance("entrance_1", 35, 215).setDestToTown("testTown"));
			locations.add(new Entrance("entrance_2", 1150, 635).setDestToMap("forest2", "entrance_1"));
		}
		{
			RectRegion r = new RectRegion(200, 100, 300, 200);
			r.setName("Region 1");
			putRegion(r);
		}
		{
			Trigger t = new Trigger();
			t.setTriggerEvent(new UnitEntersRegion(getRegion("Region 1")));
			t.setTriggeredAction((world) -> logger.debug("Trigger happened: Unit entered region."));
			addTrigger(t);
		}

//		{
//			UnitMarker m = new UnitMarker();
//			m.pos.set(100, 400);
//			m.side = Side.PLAYER;
//			m.name = "Sally";
//			m.baseStats.set(MonsterStats.baseHuman);
//			m.animatedSpriteName = "pipo-charachip028d.png";
//			m.activeSkills.addAll(Skills.mageSkills);
//			unitMarkers.add(m);
//		}
		
//		{
//			UnitMarker m = new UnitMarker(Monsters.goblin);
//			m.pos.set(400, 400);
//			unitMarkers.add(m);
//		}

	}
}
