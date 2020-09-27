package com.mygdx.holowyth.map.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.gameScreen.MapInstanceInfo;
import com.mygdx.holowyth.map.trigger.region.Region;

public class UnitEntersRegion extends TriggerEvent{

	private Region region;
	
	@SuppressWarnings("unused")
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public UnitEntersRegion(Region region) {
		this.region = region;
	}
	public UnitEntersRegion(UnitEntersRegion src) {
		region = src.region.cloneObject();
	}
	
	
	/**
	 *  Returns true if this event is detected now.  <br>
	 *  If event was detected now, event is considered triggered. Subsequent calls to check() will return false, unless the event is reset.
	 */
	public boolean check(MapInstanceInfo world) {
		if(!triggered) {
			if(region.containsAnyUnit(world.getUnits())) {
//				logger.debug("UnitEnterRegions was triggered: {}", region.getName() );
				triggered = true;
				return true;
			}
		}
		return false;
	}

	@Override
	public TriggerEvent cloneObject() {
		return new UnitEntersRegion(this);
	}
	
	
}

