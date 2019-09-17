package com.mygdx.holowyth.unit.behaviours;

import java.util.Comparator;
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

			Comparator<UnitOrderable> closestUnitComp = (UnitOrderable u1, UnitOrderable u2) -> {
				if (Point.calcDistanceSqr(thisUnit.getPos(), u1.getPos())
						- Point.calcDistanceSqr(thisUnit.getPos(), u2.getPos()) >= 0) {
					return -1;
				} else {
					return 1;
				}
			};

			PriorityQueue<UnitOrderable> closestTargets = new PriorityQueue<UnitOrderable>(closestUnitComp);
			for (UnitOrderable otherUnit : world.getUnits()) {
				if (otherUnit == thisUnit)
					continue;
				if (otherUnit.getSide() != Side.ENEMY) {
					closestTargets.add(otherUnit);
				}
			}
			if (!closestTargets.isEmpty()) {
				UnitOrderable closestEnemy = closestTargets.remove();
				if (Point.calcDistance(thisUnit.getPos(), closestEnemy.getPos()) <= Holo.idleAggroRange) {
					thisUnit.orderAttackUnit(closestEnemy);
				}
			}
		}
	}
}
