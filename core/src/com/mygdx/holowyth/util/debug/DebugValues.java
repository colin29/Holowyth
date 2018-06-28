package com.mygdx.holowyth.util.debug;

import java.util.ArrayList;
import java.util.function.IntSupplier;

import com.mygdx.holowyth.util.debug.DebugValue.FloatSupplier;
import com.mygdx.holowyth.util.debug.DebugValue.StringSupplier;

public class DebugValues extends ArrayList<DebugValue> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void add(String name, FloatSupplier getter){
		this.add(new DebugValue(name, getter));
	}
	public void add(String name, IntSupplier getter){
		this.add(new DebugValue(name, getter));
	}
	public void add(String name, StringSupplier getter){
		this.add(new DebugValue(name, getter));
	}
}
