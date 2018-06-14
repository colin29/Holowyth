package com.mygdx.holowyth.util.debug;

import java.util.function.IntSupplier;

/**
 * The user should run a switch statement on the valueType, and call the appropriate value.
 * @author Colin Ta
 *
 */
public class DebugValue {
	
	String name;
	FloatSupplier floatGetter;
	IntSupplier intGetter;
	
	private boolean formatPercentage = false;
	
	public enum ValueType {FLOAT, INT};
	ValueType valueType;
	
	DebugValue(String name, FloatSupplier getter){
		this.name = name;
		this.floatGetter = getter;
		valueType = ValueType.FLOAT;
	}
	DebugValue(String name, IntSupplier getter){
		this.name = name;
		this.intGetter = getter;
		valueType = ValueType.INT;
	}
	
	public ValueType getValueType() {
		return valueType;
	}
	
	public float getFloatValue() {
		return floatGetter.getAsFloat();
	}
	public int getIntValue() {
		return intGetter.getAsInt();
	}
	
	public void displayAsPercentage() {
		formatPercentage = true;
	}
	public boolean shouldDisplayAsPercentage() {
		return formatPercentage;
	}
	
	boolean printAsPercentage = false;

	@FunctionalInterface
	public abstract interface FloatSupplier{
		abstract float getAsFloat();
	}

}
