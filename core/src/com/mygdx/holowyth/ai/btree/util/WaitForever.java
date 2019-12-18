package com.mygdx.holowyth.ai.btree.util;

import com.badlogic.gdx.ai.btree.Task;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;

public class WaitForever extends HoloLeafTask {

	@Override
	public Status execute() {
		markLastTaskRan();
		return Status.RUNNING;
	}

	@Override
	protected Task<UnitOrderable> copyTo(Task<UnitOrderable> task) {
		return task;
	}

}
