package com.mygdx.holowyth.ai.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.mygdx.holowyth.unit.Unit.Order;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.util.dataobjects.Point;

public class Flee extends LeafTask<UnitOrderable> {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public static Point fleePoint = new Point(900, 550); // flee point;

	UnitOrderable self;

	@Override
	public Status execute() {
		self = getObject();

		if (self.getCurrentOrder() != Order.MOVE && self.isMoveOrderAllowed()) {
			self.orderMove(fleePoint.x, fleePoint.y);
			logger.debug("fleeing");
		}
		return Status.RUNNING; // never terminate;
	}

	@Override
	protected Task<UnitOrderable> copyTo(Task<UnitOrderable> task) {
		return task;
	}

}
