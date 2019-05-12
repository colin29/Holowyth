package com.mygdx.holowyth.util.tools.debugstore;

import java.util.function.IntSupplier;

/**
 * The user should run a switch statement on the valueType, and call the
 * appropriate value.
 * 
 * @author Colin Ta
 *
 */
public class DebugValue {

	private String name;
	FloatSupplier floatGetter;
	IntSupplier intGetter;
	StringSupplier stringGetter;
	private boolean formatPercentage = false;

	public enum ValueType {
		FLOAT, INT, STRING
	};

	ValueType valueType;

	public DebugValue(String name, FloatSupplier getter) {
		this.name = name;
		this.floatGetter = getter;
		valueType = ValueType.FLOAT;
	}

	public DebugValue(String name, IntSupplier getter) {
		this.name = name;
		this.intGetter = getter;
		valueType = ValueType.INT;
	}

	public DebugValue(String name, StringSupplier getter) {
		this.name = name;
		this.stringGetter = getter;
		valueType = ValueType.STRING;
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

	public String getStringValue() {
		return stringGetter.getAsString();
	}

	public boolean shouldDisplayAsPercentage() {
		return formatPercentage;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@FunctionalInterface
	public abstract interface FloatSupplier {
		abstract float getAsFloat();
	}

	@FunctionalInterface
	public abstract interface StringSupplier {
		abstract String getAsString();
	}

}
