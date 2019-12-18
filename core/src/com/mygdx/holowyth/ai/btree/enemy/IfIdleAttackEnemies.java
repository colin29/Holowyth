package com.mygdx.holowyth.ai.btree.enemy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.ai.btree.Task;
import com.mygdx.holowyth.ai.btree.util.HoloLeafTask;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;

public class IfIdleAttackEnemies extends HoloLeafTask {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	UnitOrderable self;

	@Override
	public Status execute() {
		markLastTaskRan();
		self = getObject();
		logger.debug("attacking enemies");
		return Status.SUCCEEDED;
	}

	@Override
	protected Task<UnitOrderable> copyTo(Task<UnitOrderable> task) {
		return task;
	}

}
