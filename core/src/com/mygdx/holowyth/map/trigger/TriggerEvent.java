package com.mygdx.holowyth.map.trigger;

import com.mygdx.holowyth.gameScreen.WorldInfo;

public abstract class TriggerEvent {

	/**
	 * Most event triggers only fire once, unless reset.
	 */
	protected boolean triggered = false;
	
	public abstract boolean check(WorldInfo world);

	public abstract TriggerEvent cloneObject();

}
