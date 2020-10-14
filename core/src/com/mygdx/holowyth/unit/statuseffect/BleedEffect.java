package com.mygdx.holowyth.unit.statuseffect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.UnitStats.DamageInstance;
import com.mygdx.holowyth.unit.UnitStats.DamageType;

public class BleedEffect {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final Unit unit;
	
	private float damagePerTick;
	private int tickInterval;
	private int totalTicks;
	
	private int framesElapsed = 0;
	private int ticksDone = 0;
	
	private DamageInstance dmgInstance;
	
	public BleedEffect(Unit unit, float damagePerTick, int totalTicks, int totalFrames) {
		this.unit = unit;
		this.damagePerTick = damagePerTick;
		this.totalTicks = totalTicks;
		tickInterval = totalFrames / totalTicks;
		
		dmgInstance = new DamageInstance(damagePerTick);
		dmgInstance.type = DamageType.BLEED; 
		
		if(totalTicks > totalFrames) {
			logger.warn("totalTicks should not be greater than total frames");
		}
	}
	
	public int getTicksRemaining() {
		return totalTicks - ticksDone;
	}
	public int getTickInterval() {
		return tickInterval;
	}

	public void tick() {
		if(framesElapsed >0 && framesElapsed % tickInterval == 0) {
			unit.stats.applyDamage(dmgInstance);
			ticksDone++;
		}
		framesElapsed++;
	}

	public boolean isExpired() {
		return ticksDone >= totalTicks;
	}
}
