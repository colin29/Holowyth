package com.mygdx.holowyth.map;


public class Entrance extends Location {
	
	
	public String destMap;
	public String destLoc;
	
	public Entrance(String name, float x, float y) {
		super(name, x, y);
	}
	
	public Entrance setDest(String destMap, String destLoc) {
		this.destMap = destMap;
		this.destLoc = destLoc;
		return this;
	}
}
