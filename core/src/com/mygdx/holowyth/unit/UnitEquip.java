package com.mygdx.holowyth.unit;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.unit.item.Equip;
import com.mygdx.holowyth.util.exceptions.HoloAssertException;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * @author Colin Ta
 */
public class UnitEquip {

	private final Unit self;

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


	public UnitEquip(Unit self) {
		this.self = self;
	}
	

	public void clearMapLifetimeData() {
		// None
	}

	/**
	 * 
	 * @return true if the equip was successful
	 */
	public boolean equip(Equip item) {
		// Don't report null equip, let the underlying collection throw an exception
		if (this.hasEquipped(item)) {
			logger.info("Tried to equip {} which is already equipped by {}", item.name, self.getName());
			return false;
		}

		switch (item.equipType) {
		case HEADGEAR:
			equips.put(Slot.HEAD, item);
			break;
		case ARMOR:
			equips.put(Slot.TORSO, item);
			break;
		case WEAPON:
			clearMainHandSlot();
			if (item.is2HWeapon) {
				equips.put(Slot.MAIN_HAND, item);
				equips.put(Slot.OFF_HAND, item);
			} else {
				equips.put(Slot.MAIN_HAND, item);
			}
			break;
		case SHIELD:
			clearOffHandSlot();
			equips.put(Slot.OFF_HAND, item);
			break;
		case ACCESSORY:
			if (equips.isNull(Slot.ACCESSORY1) && equips.isNull(Slot.ACCESSORY2)) {
				equips.put(Slot.ACCESSORY2, item);
			} else {
				equips.put(Slot.ACCESSORY1, item);
			}
			break;
		default:
			throw new HoloAssertException("Unhandled equipment type");
		}
		self.stats.recalculateStats();
		return true;
	}

	private void clearMainHandSlot() {
		if (is2HWieldingWeapon()) {
			equips.remove(Slot.MAIN_HAND);
			equips.remove(Slot.OFF_HAND);
		} else {
			equips.remove(Slot.MAIN_HAND);
		}
	}

	private void clearOffHandSlot() {
		if (is2HWieldingWeapon()) {
			equips.remove(Slot.MAIN_HAND);
			equips.remove(Slot.OFF_HAND);
		} else {
			equips.remove(Slot.OFF_HAND);
		}
	}

	public boolean unequip(Equip item) {
		if (item == null)
			throw new HoloIllegalArgumentsException("Can't unequip a null item");
		if (!hasEquipped(item)) {
			logger.info("Tried to unequip {}, but item is not equipped by {}", item.name, self.getName());
			return false;
		}
		for (Slot slot : Slot.values()) {
			if (equips.get(slot) == item) {
				equips.remove(slot);
			}
		}
		return true;
	}

	public boolean hasEquipped(Equip equip) {
		return equips.contains(equip);
	}

	public Equip getEquip(Slot slot) {
		return equips.get(slot);
	}

	/**
	 * @return Read-only collection of the equip slots
	 */
	public Map<Slot, Equip> getEquipSlots() {
		return equips.getReadOnlyMap();
	}

	public boolean is2HWieldingWeapon() {
		return !equips.isNull(Slot.MAIN_HAND) &&
				equips.get(Slot.MAIN_HAND) == equips.get(Slot.OFF_HAND);
	}

}