package com.mygdx.holowyth.unit;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.unit.item.Equip;
import com.mygdx.holowyth.util.exceptions.HoloAssertException;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * Represents a set of equipments that can be worn at one time.
 */
public class WornEquips {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private EquippedItemsMap equips = new EquippedItemsMap();
	
	public enum Slot {
		HEAD, MAIN_HAND, OFF_HAND, TORSO, ACCESSORY1, ACCESSORY2;
	
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
			case ACCESSORY1:
				return "Accessory 1";
			case ACCESSORY2:
				return "Accessory 2";
			default:
				throw new HoloAssertException("Unhandled Equip slot");
			}
		}
	}
	
	public WornEquips() {
	}

	public WornEquips(WornEquips src) {
		for (Slot slot : Slot.values()) {
			
			if(src.getEquip(slot) == null)
				continue;
			
			if(slot == Slot.OFF_HAND && !src.is2HWieldingWeapon()) {
				equipOrNull(src.getEquip(Slot.OFF_HAND).cloneObject());
			}else {
				equipOrNull(src.getEquip(slot).cloneObject());
			}
		}
	}
	
	private boolean equipOrNull(Equip item) {
		if(item == null) {
			return false;
		}else {
			return equip(item);
		}
	}

	/**
	 * 
	 * @return true if the equip was successful
	 */
	public boolean equip(Equip item) {
		// Don't report null equip, let the underlying collection throw an exception (??)
		if (this.hasEquipped(item)) {
			logger.info("Tried to equip item {} which was already equipped by this.", item.name);
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
			if (equips.isNull(WornEquips.Slot.ACCESSORY1) && equips.isNull(WornEquips.Slot.ACCESSORY2)) {
				equips.put(WornEquips.Slot.ACCESSORY2, item);
			} else {
				equips.put(WornEquips.Slot.ACCESSORY1, item);
			}
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
		if (item == null)
			throw new HoloIllegalArgumentsException("Can't unequip a null item");
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
		return equips.contains(equip);
	}

	public Equip getEquip(WornEquips.Slot slot) {
		return equips.get(slot);
	}

	/**
	 * @return Read-only collection of the equip slots
	 */
	public Map<WornEquips.Slot, Equip> getEquipSlots() {
		return equips.getReadOnlyMap();
	}

	public boolean is2HWieldingWeapon() {
		return !equips.isNull(WornEquips.Slot.MAIN_HAND) &&
				equips.get(WornEquips.Slot.MAIN_HAND) == equips.get(WornEquips.Slot.OFF_HAND);
	}
	
	public WornEquips cloneObject() {
		return new WornEquips(this);
	}

}
