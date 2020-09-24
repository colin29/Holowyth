package com.mygdx.holowyth.map.trigger;

import java.util.ArrayList;
import java.util.List;

import com.mygdx.holowyth.gameScreen.World;

/**
 * Is a map-life time component
 * @author Colin
 *
 */
public class TriggersHandler {

	private final World world;
	private List<Trigger> triggers = new ArrayList<Trigger>();
	
	public TriggersHandler(World world) {
		this.world = world;
	}
	
	public void addTrigger(Trigger t) {
		triggers.add(t);
	}
	
	public void checkTriggers(){
		for(Trigger t: triggers) {
			t.check(world);
		}
		// TODO removed finished triggers
	}
	

}
