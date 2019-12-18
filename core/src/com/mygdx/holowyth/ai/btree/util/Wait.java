package com.mygdx.holowyth.ai.btree.util;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;

public class Wait extends HoloLeafTask {

	private int framesElapsed = 0;

	@TaskAttribute(required = true)
	public int frames = 1;

	@Override
	public Status execute() {
		markLastTaskRan();
		framesElapsed += 1;
		if (framesElapsed > frames) {
			return Status.SUCCEEDED;
		} else {
			return Status.RUNNING;
		}

	}

	@Override
	protected Task<UnitOrderable> copyTo(Task<UnitOrderable> task) {
		((Wait) task).frames = frames;
		return task;
	}

}
