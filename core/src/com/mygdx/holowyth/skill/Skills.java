package com.mygdx.holowyth.skill;

import java.util.List;

import com.mygdx.holowyth.combatDemo.World;
import com.mygdx.holowyth.unit.Unit;

/**
 * Static class that holds a collection of skills
 * @author Colin Ta
 *
 */
public class Skills {
	
	/**
	 * Deals damage to all units in a radius around the caster
	 * @param unit
	 */
	public static void novaFlareEffect(Unit source, World world) {
		System.out.println("Nova flare activated");
		int damage = 30;
		int aoe = 70;
		
		List<Unit> units = world.getUnits();
		for(Unit unit: units) {
			if(Unit.getDist(source, unit) <= aoe) {
				if(unit != source) {
					unit.stats.applyDamage(damage);	
				}
			}
		}
		
	}
}
