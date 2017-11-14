package com.mygdx.holowyth.polygon;

import com.badlogic.gdx.graphics.Camera;
import com.mygdx.holowyth.map.Field;

/**
 * Similar to a PolygonDrawer but requires a map and sets the map's altered flag upon making changes
 *
 */
public class MapPolygonDrawer extends PolygonDrawer {

	private Field map;
	public MapPolygonDrawer(Camera parentCamera) {
		super(parentCamera);
	}
	
	@Override
	protected void completePolygon(){
		super.completePolygon();
		map.hasUnsavedChanges = true;
	}
	
	public void setMap(Field map){
		this.map = map;
		this.polys = map.polys;
	}
}
