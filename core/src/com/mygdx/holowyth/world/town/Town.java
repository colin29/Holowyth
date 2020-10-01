package com.mygdx.holowyth.world.town;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * All towns are assumed to have exactly one shop, atm.
 * @author Colin
 */
@NonNullByDefault
public class Town {
	public final Shop shop;
	private String name = "Unnamed Town";
	
	public Town() {
		shop = new Shop();
	}

	public Town(Town src) {
		shop = src.shop.cloneObject();
		name = src.name;
	}
	
	public Town cloneObject() {
		return new Town(this);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
