package com.mygdx.holowyth.unit;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.unit.item.Equip;
import com.mygdx.holowyth.util.exceptions.HoloAssertException;

/**
 * Represents a set of equipments that can be worn at one time.
 */
@NonNullByDefault
public class WornEquips {

	@SuppressWarnings("null")
	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<WornEquips.@NonNull Slot, @NonNull Equip> equips = new LinkedHashMap<>();

	public enum Slot {
		HEAD, MAIN_HAND, OFF_HAND, TORSO, ACCESSORY, FOOTWEAR;

		public String getName() {
			switch (this) {
			case HEAD:
				return "head";
			case MAIN_HAND:
				return "Main Hand";
			case OFF_HAND:
				return "Off Hand";
			case TORSO:
				return "Torso";
			case ACCESSORY:
				return "Accessory";
			case FOOTWEAR:
				return "Footwear";
			default:
				throw new HoloAssertException("Unhandled Equip slot");
			
			}
		}
	}

	public WornEquips() {
	}

	public WornEquips(WornEquips src) {
		for (Slot slot : Slot.values()) {
			equips.put(slot, src.getEquip(slot));
			if (src.is2HWieldingWeapon()) { // discard the duplicate copied reference in OFF_HAND
				equips.put(Slot.OFF_HAND, equips.get(Slot.MAIN_HAND));
			}
		}
	}

	/**
	 * 
	 * @return true if the equip was successful
	 */
	public boolean equip(Equip item) {
		if (this.hasEquipped(item)) {
			logger.warn("Tried to equip item {} which was already equipped by this.", item.name);
			return false;
		}

		switch (item.equipType) {
		case HEADGEAR:
			equips.put(WornEquips.Slot.HEAD, item);
			break;
		case ARMOR:
			equips.put(WornEquips.Slot.TORSO, item);
			break;
		case WEAPON:
			clearMainHandSlot();
			if (item.is2HWeapon) {
				equips.put(WornEquips.Slot.MAIN_HAND, item);
				equips.put(WornEquips.Slot.OFF_HAND, item);
			} else {
				equips.put(WornEquips.Slot.MAIN_HAND, item);
			}
			break;
		case SHIELD:
			clearOffHandSlot();
			equips.put(WornEquips.Slot.OFF_HAND, item);
			break;
		case ACCESSORY:
			equips.put(WornEquips.Slot.ACCESSORY, item);
			break;
		default:
			throw new HoloAssertException("Unhandled equipment type");
		}
		return true;
	}

	private void clearMainHandSlot() {
		if (is2HWieldingWeapon()) {
			equips.remove(WornEquips.Slot.MAIN_HAND);
			equips.remove(WornEquips.Slot.OFF_HAND);
		} else {
			equips.remove(WornEquips.Slot.MAIN_HAND);
		}
	}

	private void clearOffHandSlot() {
		if (is2HWieldingWeapon()) {
			equips.remove(WornEquips.Slot.MAIN_HAND);
			equips.remove(WornEquips.Slot.OFF_HAND);
		} else {
			equips.remove(WornEquips.Slot.OFF_HAND);
		}
	}

	public boolean unequip(Equip item) {
		if (!hasEquipped(item)) {
			logger.info("Tried to unequip {}, but item is not equipped by this", item.name);
			return false;
		}
		for (WornEquips.Slot slot : WornEquips.Slot.values()) {
			if (equips.get(slot) == item) {
				equips.remove(slot);
			}
		}
		return true;
	}

	public boolean hasEquipped(Equip equip) {
		for (var wornEquip : equips.values()) {
			if (equip == wornEquip)
				return true;
		}
		return false;
	}

	public Equip getEquip(WornEquips.Slot slot) {
		return equips.get(slot);
	}

	/**
	 * @return Read-only collection of the equip slots
	 */
	@SuppressWarnings("null")
	public Map<WornEquips.Slot, Equip> getEquipSlots() {
		return Collections.unmodifiableMap(equips);
	}

	public boolean is2HWieldingWeapon() {
		return !equips.containsKey(WornEquips.Slot.MAIN_HAND)
				&& equips.get(WornEquips.Slot.MAIN_HAND) == equips.get(WornEquips.Slot.OFF_HAND);
	}

	public WornEquips cloneObject() {
		return new WornEquips(this);
	}

}
