package com.mygdx.holowyth.ai.btree.guards;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.mygdx.holowyth.ai.btree.util.HoloLeafTask;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;

public class IsHealthAbove extends HoloLeafTask {

	@TaskAttribute(required = true)
	public float value;

	@Override
	public Status execute() {
		markLastTaskRan();
		self = getObject();
		return (self.getStats().getHpRatio() > value) ? Status.SUCCEEDED : Status.FAILED;
	}

	@Override
	protected Task<UnitOrderable> copyTo(Task<UnitOrderable> task) {
		((IsHealthAbove) task).value = value;
		return super.copyTo(task);
	}

}
