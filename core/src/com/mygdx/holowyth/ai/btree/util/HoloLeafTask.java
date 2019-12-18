package com.mygdx.holowyth.ai.btree.util;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;

public abstract class HoloLeafTask extends LeafTask<UnitOrderable> {

	/**
	 * Sets a string on the unit for debug purposes
	 */
	protected void markLastTaskRan() {
		getObject().getAI().lastRanOrder = getClass().getSimpleName();
	}

	/**
	 * Default implementation, you'd only need to override if you use deep structures, or you use a parameterized constructor (includes the annotation
	 * parameters)
	 */
	@Override
	protected Task<UnitOrderable> copyTo(Task<UnitOrderable> task) {
		return task;
	}

}
