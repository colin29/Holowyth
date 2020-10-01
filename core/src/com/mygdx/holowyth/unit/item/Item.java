package com.mygdx.holowyth.unit.item;

import org.eclipse.jdt.annotation.NonNull;

public class Item {

	public enum ItemType {
		EQUIP, CONSUMABLE, OTHER
	}
	protected boolean isTemplate;

	public ItemType itemType;
	public @NonNull String name;

	public Item(@NonNull String name) {
		this.name = name;
	}

	public Item(Item src) {
		name = src.name;
		itemType = src.itemType;
	}

	/**
	 * Items that are templates are meant to be copied and should not be equipped
	 */
	public @NonNull Item markAsTemplate() {
		isTemplate = true;
		return this;
	}

	public boolean isTemplate() {
		return isTemplate;
	}
	
	public @NonNull Item cloneObject() {
		return new Item(this);
	}

}