package com.mygdx.holowyth.ai.btree.enemy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.ai.btree.Task;
import com.mygdx.holowyth.ai.btree.util.HoloLeafTask;
import com.mygdx.holowyth.unit.UnitOrders.Order;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * Cannot fail, flees until succeeds
 * 
 * @author Colin Ta
 *
 */
public class FleeUntilReachLocation extends HoloLeafTask {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public static Point fleePoint = new Point(500, 500); // flee point;
	public static float fleePointRadius = 40f;

	int framesElapsed = 0;

	@Override
	public Status execute() {
		markLastTaskRan();
		self = getObject();

		if (self.isAttacking() && self.isRetreatOrderAllowed()) {
			self.orderRetreat(fleePoint.x, fleePoint.y);
		} else if (self.getOrder() != Order.MOVE && self.isMoveOrderAllowed()) {
			self.orderMove(fleePoint.x, fleePoint.y);
		}

		return (Point.calcDistance(self.getPos(), fleePoint) < fleePointRadius) ? Status.SUCCEEDED : Status.RUNNING;
	}

	@Override
	protected Task<UnitOrderable> copyTo(Task<UnitOrderable> task) {
		return task;
	}

}
