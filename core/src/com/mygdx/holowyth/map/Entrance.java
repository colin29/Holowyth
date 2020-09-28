package com.mygdx.holowyth.map;

import java.util.List;

import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;

public class Entrance extends Location {
	
	
	/**When you arrive through an entrance, the entrance will be inactive for this long */
	public static final int inactiveFramesOnArrival = Holo.GAME_FPS * 8; 
	public static final float triggerRange = Holo.UNIT_RADIUS + 20;
	
	
	public String destMap;
	public String destLoc;
	
	private transient int inactiveFramesRemaining; 
	
	
	
	public Entrance(String name, float x, float y) {
		super(name, x, y);
	}
	public Entrance(Entrance src) {
		super(src);
		destMap = src.destMap;
		destLoc = src.destLoc;
	}
	
	public Entrance setDest(String destMap, String destLoc) {
		this.destMap = destMap;
		this.destLoc = destLoc;
		return this;
	}
	
	public void tick() {
		inactiveFramesRemaining = Math.max(0, inactiveFramesRemaining-1);
	}
	
	public boolean isBeingTriggered(List<? extends UnitInfo> units) {
		if(inactiveFramesRemaining > 0)
			return false;
		for(var unit : units) {
			if(Point.dist(unit.getPos(), this.pos) <= triggerRange) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Disables the entrance for {@link #inactiveFramesOnArrival} frames
	 */
	public void disableTemporarily() {
		inactiveFramesRemaining = inactiveFramesOnArrival;
	}
	public boolean hasDestination() {
		return destMap != null && destLoc != null;
	}

	@Override
	public Location cloneObject() {
		return new Entrance(this);
	}
}
