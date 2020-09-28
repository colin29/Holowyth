package com.mygdx.holowyth.util.tools;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Input.Keys;

/**
 * Small class that lets you easily add hotkeys
 * @author Colin Ta
 *
 */
public class FunctionBindings {
	
	private Map<Integer, Runnable> functionBindings = new HashMap<Integer, Runnable>();
	
	
	public void bindFunctionToKey(Runnable funct, int keyCode) {
		if(functionBindings.containsKey(keyCode)) {
			System.out.printf("Key %s already has a function bound %n", Keys.toString(keyCode));
			return;
		}
		if(funct == null) {
			System.out.printf("Null function provided for Key %s %n", Keys.toString(keyCode));
			return;
		}
		functionBindings.put(keyCode, funct);
	}
	/**
	 * @param keyCode
	 * @return true if a function binding existed and was called, otherwise false
	 */
	public boolean runBoundFunction(int keyCode) {
		if(functionBindings.containsKey(keyCode)) {
			functionBindings.get(keyCode).run();
//			System.out.printf("Function for key %s called %n", Keys.toString(keyCode));
			return true;
		}
		return false;
	}
	
}
