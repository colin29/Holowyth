package com.mygdx.holowyth.unit;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.utils.Array;
import com.mygdx.holowyth.unit.Item.ItemType;

/**
 * 2H weapons not fully supported yet
 * 
 * @author Colin Ta
 *
 */
public class UnitEquip {

	private final Unit self;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public Item head;
	public Item mainHand;
	public Item offHand;
	public Item torso;
	public Item accessory1;
	public Item accessory2;

	public UnitEquip(Unit self) {
		this.self = self;
	}

	public boolean isWielding2HWeapon() {
		// TODO:
		return false;
	}

	public static final Array<String> slotLabels = new Array<String>();
	static {
		slotLabels.addAll("Head", "Main Hand", "Off Hand", "Torso", "Accessory 1", "Accessory 2");
	}

	/**
	 * Note that the returned value will become outdated if any items are equipped/un-equipped
	 * 
	 * Like the fields, null means no item equipped
	 * 
	 * @return A list of the items in each field, in order. Each index corresponds to an equip slot, so null is a valid value
	 */
	Array<Item> getArrayOfEquipSlots() {
		Array<Item> a = new Array<Item>();
		a.addAll(head, mainHand, offHand, torso, accessory1, accessory2);
		assert (a.size == slotLabels.size);
		return a;
	}

	/**
	 * Allows other classes to consistently get all the equip slots and their content in order <br>
	 * Note that the returned map will become outdated if any items are equipped/un-equipped. <br>
	 * Like the fields, null means no item equipped. Some items, namely 2h weapons will appear in both hand slots <br>
	 * 
	 * @return A map of the equip slots, slotName -> Item
	 */
	public Map<String, Item> getIteratableMap() {
		Map<String, Item> map = new HashMap<String, Item>();

		Array<Item> curItems = getArrayOfEquipSlots();

		for (int i = 0; i < slotLabels.size; i++) {
			map.put(slotLabels.get(i), curItems.get(i));
		}
		return map;
	}

	/**
	 * 
	 * @return true if the equip was successful
	 */
	public boolean equip(Item equip) {
		if (equip.itemType != ItemType.EQUIPMENT) {
			logger.info("Trying to equip a non-equipment item");
			return false;
		}

		switch (equip.equipType) {
		case ACCESSORY:
			accessory1 = equip;
			// TODO: If slot 1 occupied but slot 2 free, put in slot 2 instead.
			break;
		case ARMOR:
			torso = equip;
			break;
		case HEADGEAR:
			head = equip;
			break;
		case SHIELD:
			offHand = equip;
			// TODO: if wielding a 2 handed item, then must de-equip
			break;
		case WEAPON:
			mainHand = equip;
			// TODO: if is 2 handed weapon, needs to assign both slots
			break;
		default:
			return false;
		}
		self.stats.recalculateStats();
		return true;
	}

}