package com.mygdx.holowyth.world.town;

/**
 * All towns are assumed to have exactly one shop, atm.
 * @author Colin
 */
public class Town {
	public final Shop shop;
	
	public Town() {
		shop = new Shop();
	}
}
