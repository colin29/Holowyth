package com.mygdx.holowyth.unit.behaviours;

import java.util.Comparator;
import java.util.PriorityQueue;

import com.mygdx.holowyth.combatDemo.WorldInfo;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.util.dataobjects.Point;

public class UnitUtil {

	/**
	 * Get targets in in order from closest to farthest, by euclidean distance
	 */
	public static PriorityQueue<UnitOrderable> getTargetsSortedByDistance(UnitOrderable thisUnit, WorldInfo world) {
		Comparator<UnitOrderable> closestUnitComp = (UnitOrderable u1, UnitOrderable u2) -> {
			if (Point.calcDistanceSqr(thisUnit.getPos(), u1.getPos())
					- Point.calcDistanceSqr(thisUnit.getPos(), u2.getPos()) < 0) {
				return -1;
			} else {
				return 1;
			}
		};

		PriorityQueue<UnitOrderable> closestTargets = new PriorityQueue<UnitOrderable>(closestUnitComp);
		for (UnitOrderable otherUnit : world.getUnits()) {
			if (otherUnit == thisUnit)
				continue;
			if (otherUnit.getSide() != thisUnit.getSide()) {
				closestTargets.add(otherUnit);
			}
		}
		return closestTargets;
	}

}
