package com.mygdx.holowyth.world.map.trigger;

import java.util.ArrayList;
import java.util.List;

import com.mygdx.holowyth.game.MapInstance;

/**
 * Is a map-life time component
 * @author Colin
 *
 */
public class TriggersHandler {

	private final MapInstance mapInstance;
	private List<Trigger> triggers = new ArrayList<Trigger>();
	
	public TriggersHandler(MapInstance mapInstance) {
		this.mapInstance = mapInstance;
	}
	
	public void addTrigger(Trigger t) {
		triggers.add(t);
	}
	
	public void checkTriggers(){
		for(Trigger t: triggers) {
			t.check(mapInstance);
		}
		// TODO removed finished triggers
	}
	

}
