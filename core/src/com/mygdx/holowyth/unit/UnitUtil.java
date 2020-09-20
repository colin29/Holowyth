package com.mygdx.holowyth.unit;

import java.util.Comparator;
import java.util.PriorityQueue;

import com.mygdx.holowyth.gameScreen.WorldInfo;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.util.dataobjects.Point;

public class UnitUtil {

	/**
	 * Get targets in in order from closest to farthest, by euclidean distance
	 */
	public static PriorityQueue<UnitOrderable> getTargetsSortedByDistance(UnitOrderable unit, WorldInfo world) {
		Comparator<UnitOrderable> closestUnitComp = (UnitOrderable u1, UnitOrderable u2) -> {
			if (Point.calcDistanceSqr(unit.getPos(), u1.getPos())
					- Point.calcDistanceSqr(unit.getPos(), u2.getPos()) < 0) {
				return -1;
			} else {
				return 1;
			}
		};

		PriorityQueue<UnitOrderable> closestTargets = new PriorityQueue<UnitOrderable>(closestUnitComp);
		for (UnitOrderable target : world.getUnits()) {
			if (!target.isDead() && unit.isEnemy(target)) {
				closestTargets.add(target);
			}
		}
		return closestTargets;
	}

}
