package com.mygdx.holowyth.map;

import com.mygdx.holowyth.util.dataobjects.Point;

public class Location {
	
	/**Should not modify a loc's name after it is added to a map */
	public String name = "";
	public final Point pos = new Point();
	
	public Location(String name, float x, float y){
		this(x, y);
		this.name = name;
	}
	public Location(float x, float y){
		pos.x = x;
		pos.y = y;
	}
	
}
