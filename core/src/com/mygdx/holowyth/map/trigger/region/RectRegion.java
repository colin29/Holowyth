package com.mygdx.holowyth.map.trigger.region;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.dataobjects.Point;

public class RectRegion extends Region{
	
	private final Rectangle region = new Rectangle();
	
	public RectRegion() {
	}
	public RectRegion(RectRegion src) {
		super(src);
		region.set(src.region);
	}
	public RectRegion(float x, float y, float width, float height)  {
		region.set(x, y, width, height);
	}
	
	public void set(float x, float y, float width, float height) {
		region.set(x, y, width, height);
	}

	/**
	 * Returns true if the region contains the unit's pos
	 */
	@Override
	public boolean containsUnit(UnitInfo u) {
		return region.contains(u.getX(), u.getY());
	}
	public boolean containsAnyUnit(List<? extends UnitInfo> units) {
		for(UnitInfo u : units) {
			if(containsUnit(u))
				return true;
		}
		return false;
	}
	
	public float getX() {
		return region.x;
	}
	public float getY() {
		return region.y;
	}
	
	public float getWidth() {
		return region.width;
	}
	public float getHeight() {
		return region.height;
	}
	@Override
	public Region cloneObject() {
		return new RectRegion(this);
	}
	
	
	
}
