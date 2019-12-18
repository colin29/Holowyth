package com.mygdx.holowyth.test.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;

public class LogTask extends LeafTask<Dog> {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public Status execute() {
		logger.debug("Doing my thang");
		return Status.SUCCEEDED;
	}

	@Override
	protected Task<Dog> copyTo(Task<Dog> task) {
		return null;
	}

}
