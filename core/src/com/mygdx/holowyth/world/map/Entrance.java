package com.mygdx.holowyth.world.map;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;

@NonNullByDefault
public class Entrance extends Location {
	/**When you arrive through an entrance, the entrance will be inactive for this long */
	public static final int inactiveFramesOnArrival = Holo.GAME_FPS * 8; 
	public static final float triggerRange = Holo.UNIT_RADIUS + 20;
	
	/** Null if the location has no destination */
	public @Nullable Destination dest;
		
	private transient int inactiveFramesRemaining; 
	
	public static class Destination{
		public Destination() {
		}
		public Destination(Destination src) {
		}
		public Destination cloneObject() {
			return new Destination(this);
		}
	}
	
	
	public static class MapDestination extends Destination{
		public String map;
		public String loc;
		public MapDestination(String map, String loc) {
			this.map = map;
			this.loc = loc;
		}
		public MapDestination(MapDestination src) {
			map = src.map;
			loc = src.loc;
		}
		public MapDestination cloneObject() {
			return new MapDestination(this);
		}
	}
	public static class TownDestination extends Destination{
		public String town;
		public TownDestination(String town) {
			this.town=town;
		}
		public TownDestination(TownDestination src) {
			town = src.town;
		}
		public TownDestination cloneObject() {
			return new TownDestination(this);
		}
	}
	
	
	public Entrance(String name, float x, float y) {
		super(name, x, y);
	}
	public Entrance(Entrance src) {
		super(src);
		dest = src.dest!=null ? new Destination(src.dest) : null;
	}
	
	public Entrance setDest(String destMap, String destLoc) {
		dest = new MapDestination(destMap, destLoc);
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
