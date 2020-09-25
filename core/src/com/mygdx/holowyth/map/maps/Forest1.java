package com.mygdx.holowyth.map.maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.map.GameMap;
import com.mygdx.holowyth.map.UnitMarker;
import com.mygdx.holowyth.map.trigger.Trigger;
import com.mygdx.holowyth.map.trigger.UnitEntersRegion;
import com.mygdx.holowyth.map.trigger.region.RectRegion;
import com.mygdx.holowyth.skill.skill.Skills;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.units.MonsterStats;
import com.mygdx.holowyth.unit.units.MonsterTemplates;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;

public class Forest1 extends GameMap {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	{
		tilemapPath = Holo.mapsDirectory + "/forest1.tmx";
		locations.put("default_spawn_location", new Point(100, 300));
		setName("forest1");

		{
			UnitMarker m = new UnitMarker();
			m.pos.set(100, 400);
			m.side = Side.PLAYER;
			m.name = "Sally";
			m.baseStats.set(MonsterStats.baseHuman);
			m.animatedSpriteName = "pipo-charachip028d.png";
			m.activeSkills.addAll(Skills.mageSkills);
			unitMarkers.add(m);
		}
		{
			UnitMarker m = new UnitMarker(MonsterTemplates.goblin);
			m.pos.set(400, 400);
			unitMarkers.add(m);
		}
		{
			RectRegion r =new RectRegion(200, 100, 300, 200);
			r.setName("Region 1");
			putRegion(r);
		}
		{
			Trigger t = new Trigger();
			t.setTriggerEvent(new UnitEntersRegion(getRegion("Region 1")));
			t.setTriggeredAction((world) -> logger.debug("Trigger happened: Unit entered region."));
			addTrigger(t);
		}

	}
}
