package com.mygdx.holowyth.unit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mygdx.holowyth.unit.UnitEquip.Slot;
import com.mygdx.holowyth.unit.item.Equip;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * Hashmap for equipped items, does not allow the null key, put() does not allow null values. Use remove() to clear a slot.
 * 
 * @author Colin Ta
 *
 */
class EquippedItemsMap {
	private final Map<Slot, Equip> items = new HashMap<Slot, Equip>();
	// Set up slots so that iteration prints through all the slots
	{
		for (Slot slot : Slot.values()) {
			items.put(slot, null);
		}
	}

	/**
	 * Does not allow putting null values or keys
	 */
	public void put(Slot slot, Equip equip) {
		if (slot == null)
			throw new HoloIllegalArgumentsException("Can't equip to a null slot");
		if (equip == null)
			throw new HoloIllegalArgumentsException("Can't equip a null item");
		items.put(slot, equip);
	}

	public Equip get(Slot slot) {
		if (slot == null)
			throw new HoloIllegalArgumentsException("Can't access a null slot");
		return items.get(slot);
	}

	public void remove(Slot slot) {
		if (slot == null)
			throw new HoloIllegalArgumentsException("Can't access a null slot");
		items.put(slot, null);
	}

	public boolean isNull(Slot slot) {
		if (slot == null)
			throw new HoloIllegalArgumentsException("Can't access a null slot");
		return items.get(slot) == null;
	}

	public boolean contains(Equip equip) {
		return items.containsValue(equip);
	}

	public Map<Slot, Equip> getReadOnlyMap() {
		return Collections.unmodifiableMap(items);
	}
}