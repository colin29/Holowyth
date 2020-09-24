package com.mygdx.holowyth.map.triggers;

import com.badlogic.gdx.math.Rectangle;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.dataobjects.Point;

public class RectRegion extends Region{
	
	private final Rectangle region = new Rectangle();
	
	public void set(Point botLeft, float width, float height) {
		region.set(botLeft.x, botLeft.y, width, height);
	}

	/**
	 * Returns true if a unit is inside the region. Does not care about unit size, only uses position
	 */
	@Override
	public boolean containsUnit(UnitInfo u) {
		return region.contains(u.getX(), u.getY());
	}
	
	
}
