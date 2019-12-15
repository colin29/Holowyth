package com.mygdx.holowyth.unit.item;

public class Item {

	public enum ItemType {
		EQUIP, CONSUMABLE, OTHER
	}

	public ItemType itemType;
	protected boolean isTemplate;
	public String name;

	public Item() {
		super();
	}

	/**
	 * Items that are templates are meant to be copied and should not be equipped
	 */
	public void markAsTemplate() {
		isTemplate = true;
	}

	public boolean isTemplate() {
		return isTemplate;
	}

}