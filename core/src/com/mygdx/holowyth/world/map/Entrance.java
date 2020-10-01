package com.mygdx.holowyth.world.map;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;

public class Entrance extends Location {
	
	
	/**When you arrive through an entrance, the entrance will be inactive for this long */
	public static final int inactiveFramesOnArrival = Holo.GAME_FPS * 8; 
	public static final float triggerRange = Holo.UNIT_RADIUS + 20;
	
	/** Null if the location has no destination */
	public Destination dest;
		
	private transient int inactiveFramesRemaining; 
	
	
	public static class Destination{
		public @NonNull String map;
		public @NonNull String loc;
		public Destination(@NonNull String map, @NonNull String loc) {
			this.map = map;
			this.loc = loc;
		}
		public Destination(Destination src) {
			map = src.map;
			loc = src.loc;
		}
	}
	
	
	public Entrance(@NonNull String name, float x, float y) {
		super(name, x, y);
	}
	public Entrance(Entrance src) {
		super(src);
		dest = src.dest!=null ? new Destination(src.dest) : null;
	}
	
	public Entrance setDest(@NonNull String destMap, @NonNull String destLoc) {
		dest = new Destination(destMap, destLoc);
		return this;
	}
	public void clearDest() {
		dest = null;
	}
	
	public void tick() {
		inactiveFramesRemaining = Math.max(0, inactiveFramesRemaining-1);
	}
	
	public boolean isBeingTriggered(List<@NonNull ? extends UnitInfo> units) {
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
	
	@Override
	public Location cloneObject() {
		return new Entrance(this);
	}
}
