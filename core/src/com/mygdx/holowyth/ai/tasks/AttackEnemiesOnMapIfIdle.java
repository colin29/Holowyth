package com.mygdx.holowyth.ai.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;

public class AttackEnemiesOnMapIfIdle extends LeafTask<UnitOrderable> {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public Status execute() {
		logger.debug("attacking enemies");
		return Status.SUCCEEDED;
	}

	@Override
	protected Task<UnitOrderable> copyTo(Task<UnitOrderable> task) {
		return task;
	}

}
