package com.mygdx.holowyth.ai.btree.util;

import com.badlogic.gdx.ai.btree.Task;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;

public class Stop extends HoloLeafTask {
	@Override
	public Status execute() {
		markLastTaskRan();
		getObject().orderStop();
		return Status.SUCCEEDED;
	}

	@Override
	protected Task<UnitOrderable> copyTo(Task<UnitOrderable> task) {
		return task;
	}

}
