package com.mygdx.holowyth.util.debug;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class ValueLabelMapping {

	Map<DebugValue, Label> mapping = new HashMap<DebugValue, Label>();
	
	public void registerLabel(DebugValue debugValue, Label label) {
		if(mapping.containsKey(debugValue)) {
			new Exception("Warning: Duplicate DebugValue " + debugValue.getName()).printStackTrace(System.out);
			return;
		}
		mapping.put(debugValue, label);
	}
	public void forEach(BiConsumer<DebugValue, Label> updateFunction) {
		for(Map.Entry<DebugValue, Label> entry: mapping.entrySet()) {
			updateFunction.accept(entry.getKey(), entry.getValue());
		}
	}
}
