package com.mygdx.holowyth.unit.behaviours;

import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.combatDemo.WorldInfo;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;

public class IfIdleAggroOntoNearbyTargets {

	static Logger logger = LoggerFactory.getLogger(IfIdleAggroOntoNearbyTargets.class);

	public static void applyTo(UnitOrderable unit, WorldInfo world) {

		if (unit.isCompletelyIdle()) {

			PriorityQueue<UnitOrderable> closestTargets = UnitUtil.getTargetsSortedByDistance(unit, world);
			if (!closestTargets.isEmpty()) {
				UnitOrderable closestEnemy = closestTargets.remove();

				float aggroRange = unit.getSide() == Side.PLAYER ? Holo.alliedUnitsAggroRange : Holo.defaultAggroRange;

				if (Point.calcDistance(unit.getPos(), closestEnemy.getPos()) <= aggroRange) {
					unit.orderAttackUnit(closestEnemy, false);
				}
			}
		}
	}
}
