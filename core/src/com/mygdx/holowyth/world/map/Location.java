package com.mygdx.holowyth.world.map;

import org.eclipse.jdt.annotation.NonNull;

import com.mygdx.holowyth.util.dataobjects.Point;

public class Location {
	
	/**Should not modify a loc's name after it is added to a map */
	public @NonNull String name = "";
	public final Point pos = new Point();
	
	public Location(@NonNull String name, float x, float y){
		this(x, y);
		this.name = name;
	}
	public Location(float x, float y){
		pos.x = x;
		pos.y = y;
	}
	
	public Location(Location src) {
		pos.set(src.pos.x,  src.pos.y);
		name = src.name;
	}
	
	public Location cloneObject() {
		return new Location(this);
	}
	
	public float getX() {
		return this.pos.x;
	}
	public float getY() {
		return this.pos.y;
	}
	
}
