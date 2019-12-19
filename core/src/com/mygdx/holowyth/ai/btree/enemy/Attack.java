package com.mygdx.holowyth.ai.btree.enemy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.ai.btree.Task;
import com.mygdx.holowyth.ai.btree.util.HoloLeafTask;
import com.mygdx.holowyth.unit.UnitUtil;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;

public class Attack extends HoloLeafTask {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public Status execute() {
		markLastTaskRan();
		self = getObject();

		var targets = UnitUtil.getTargetsSortedByDistance(self, self.getWorld());
		if (targets.size() > 0) {
			if (self.isAttackOrderAllowed() && !self.getOrder().isAttackUnit()) {
				self.orderAttackUnit(targets.peek());
			}
			return Status.RUNNING;
		} else {
			return Status.SUCCEEDED;
		}

	}

	@Override
	protected Task<UnitOrderable> copyTo(Task<UnitOrderable> task) {
		return task;
	}

}
