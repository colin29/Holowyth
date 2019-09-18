package com.mygdx.holowyth.unit.behaviours;

import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.combatDemo.WorldInfo;
import com.mygdx.holowyth.unit.Unit.Order;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;

public class AggroIfIsEnemyUnit {

	static Logger logger = LoggerFactory.getLogger(AggroIfIsEnemyUnit.class);

	public static void applyTo(UnitOrderable thisUnit, WorldInfo world) {

		if (thisUnit.getSide() == Side.ENEMY && thisUnit.getCurrentOrder() == Order.NONE && !thisUnit.isAttacking()) {

			PriorityQueue<UnitOrderable> closestTargets = UnitUtil.getTargetsSortedByDistance(thisUnit, world);
			if (!closestTargets.isEmpty()) {
				UnitOrderable closestEnemy = closestTargets.remove();
				if (Point.calcDistance(thisUnit.getPos(), closestEnemy.getPos()) <= Holo.defaultAggroRange) {
					thisUnit.orderAttackUnit(closestEnemy, false);
				}
			}
		}
	}
}
