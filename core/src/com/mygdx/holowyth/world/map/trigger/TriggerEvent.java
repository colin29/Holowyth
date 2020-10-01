package com.mygdx.holowyth.world.map.trigger;

import com.mygdx.holowyth.gameScreen.MapInstanceInfo;

public abstract class TriggerEvent {

	/**
	 * Most event triggers only fire once, unless reset.
	 */
	protected boolean triggered = false;
	
	public abstract boolean check(MapInstanceInfo world);

	public abstract TriggerEvent cloneObject();

}
