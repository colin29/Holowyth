package com.mygdx.holowyth.unit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mygdx.holowyth.unit.item.Equip;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * Hashmap for equipped items, does not allow the null key, put() does not allow null values. Use remove() to clear a slot.
 * 
 * @author Colin Ta
 *
 */
class EquippedItemsMap {
	private final Map<WornEquips.Slot, Equip> equippedItems = new HashMap<WornEquips.Slot, Equip>();
	// Set up slots so that iteration prints through all the slots
	{
		for (WornEquips.Slot slot : WornEquips.Slot.values()) {
			equippedItems.put(slot, null);
		}
	}

	/**
	 * Does not allow putting null values or keys
	 */
	public void put(WornEquips.Slot slot, Equip equip) {
		if (slot == null)
			throw new HoloIllegalArgumentsException("Can't equip to a null slot");
		if (equip == null)
			throw new HoloIllegalArgumentsException("Can't equip a null item");
		equippedItems.put(slot, equip);
	}

	public Equip get(WornEquips.Slot slot) {
		if (slot == null)
			throw new HoloIllegalArgumentsException("Can't access a null slot");
		return equippedItems.get(slot);
	}

	public void remove(WornEquips.Slot slot) {
		if (slot == null)
			throw new HoloIllegalArgumentsException("Can't access a null slot");
		equippedItems.put(slot, null);
	}

	public boolean isNull(WornEquips.Slot slot) {
		if (slot == null)
			throw new HoloIllegalArgumentsException("Can't access a null slot");
		return equippedItems.get(slot) == null;
	}

	public boolean contains(Equip equip) {
		return equippedItems.containsValue(equip);
	}

	public Map<WornEquips.Slot, Equip> getReadOnlyMap() {
		return Collections.unmodifiableMap(equippedItems);
	}
}