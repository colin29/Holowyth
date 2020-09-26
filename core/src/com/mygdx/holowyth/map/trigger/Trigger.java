package com.mygdx.holowyth.map.trigger;

import java.util.function.Consumer;

import com.mygdx.holowyth.gameScreen.MapInstance;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * Note: triggered actions should not contain any meaningful state.
 *
 */
public class Trigger {

	private TriggerEvent triggerEvent;
	private Consumer<MapInstance> triggeredAction;

	public Trigger() {
	}

	Trigger(Trigger src) {
		triggerEvent = src.triggerEvent.cloneObject();
		triggeredAction = src.triggeredAction; // This should be fine... A Lamda is just an object with a single method.
												// There's no state so its constant
	}

	/**
	 * Executes the triggered Action if the triggeredEvent is detected.
	 * 
	 * @return
	 */
	public boolean check(MapInstance world) {
		if (triggerEvent.check(world)) {
			triggeredAction.accept(world);
			return true;
		}
		return false;
	}

	public Trigger cloneObject() {
		return new Trigger(this);
	}

	public TriggerEvent getTriggerEvent() {
		return triggerEvent;
	}

	public Consumer<MapInstance> getTriggeredAction() {
		return triggeredAction;
	}

	public void setTriggerEvent(TriggerEvent triggerEvent) {
		if (triggerEvent == null)
			throw new HoloIllegalArgumentsException("trigger event can't be null");
		this.triggerEvent = triggerEvent;
	}

	public void setTriggeredAction(Consumer<MapInstance> triggeredAction) {
		if (triggeredAction == null)
			throw new HoloIllegalArgumentsException("triggered action can't be null");
		this.triggeredAction = triggeredAction;
	}

}
