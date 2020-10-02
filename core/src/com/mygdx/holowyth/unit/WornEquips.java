package com.mygdx.holowyth.unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.game.session.OwnedItems;
import com.mygdx.holowyth.unit.item.Equip;
import com.mygdx.holowyth.util.exceptions.HoloAssertException;

/**
 * Represents a set of equipments that can be worn at one time.
 */
@NonNullByDefault
public class WornEquips {

	/**
	 * If set, de-equipped items are added to this inventory
	 */
	@Nullable private OwnedItems inventory;
	
	@SuppressWarnings("null")
	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<WornEquips.@NonNull Slot, @NonNull Equip> equips = new LinkedHashMap<>();
	private final List<EquippedItemsListener> listeners = new ArrayList<>();

	public enum Slot {
		HEAD, MAIN_HAND, OFF_HAND, BODY, ACCESSORY, SHOES;

		public String getName() {
			switch (this) {
			case HEAD:
				return "Head";
			case MAIN_HAND:
				return "Main Hand";
			case OFF_HAND:
				return "Off Hand";
			case BODY:
				return "Body";
			case ACCESSORY:
				return "Accessory";
			case SHOES:
				return "Shoes";
			default:
				throw new HoloAssertException("Unhandled Equip slot");

			}
		}
	}

	public WornEquips() {
	}

	public WornEquips(WornEquips src) {
		for (var e : src.equips.entrySet()) {
			equips.put(e.getKey(), e.getValue().cloneObject());
			if (src.is2HWieldingWeapon()) { // discard the duplicate copied reference in OFF_HAND
				equips.put(Slot.OFF_HAND, equips.get(Slot.MAIN_HAND));
			}
		}
	}

	/**
	 * If you are equipping for a unit (as opposed to a unit marker), access via UnitEquip!!!
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
			equips.put(WornEquips.Slot.BODY, item);
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
		changed();
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

	/**
	 *  If you are unequipping for a unit, access via UnitEquip!!!
	 */
	public boolean unequip(Equip item) {
		if (!hasEquipped(item)) {
			logger.warn("Tried to unequip {}, but item is not equipped by this", item.name);
			return false;
		}
		boolean wasPresent = false;
		for (WornEquips.Slot slot : WornEquips.Slot.values()) { // in case of 2-handed wep both slots removed
			if (equips.get(slot) == item) {
				equips.remove(slot);
				wasPresent = true;
			}
		}
		if(wasPresent) {
			changed();
			if(inventory != null) {
				inventory.addItem(item);
			}
			logger.debug("Un-equipped '{}'", item.name);
			return true;
		}
		return false;
	}
	
	/**
	 * If you are unequipping for a unit, access via UnitEquip!!!
	 * @return true if an equip existed in that slot
	 */
	public boolean unequip(Slot slot) {
		if(equips.containsKey(slot)) {
			unequip(equips.get(slot));
			return true;
		}else {
			return false;
		}
	}
	

	public boolean hasEquipped(Equip equip) {
		for (var wornEquip : equips.values()) {
			if (equip == wornEquip)
				return true;
		}
		return false;
	}

	/**
	 * Can return null, iterate over getEquipped().entrySet() if you want all occupied slots
	 */
	public @Nullable Equip getEquip(WornEquips.Slot slot) {
		return equips.get(slot);
	}

	/**
	 * @return Read-only collection of the equip slots
	 */
	@SuppressWarnings("null")
	public Map<WornEquips.Slot, Equip> getEquipped() {
		return Collections.unmodifiableMap(equips);
	}

	public boolean is2HWieldingWeapon() {
		return !equips.containsKey(WornEquips.Slot.MAIN_HAND)
				&& equips.get(WornEquips.Slot.MAIN_HAND) == equips.get(WornEquips.Slot.OFF_HAND);
	}

	public WornEquips cloneObject() {
		return new WornEquips(this);
	}

	public void addListener(EquippedItemsListener o) {
		listeners.add(o);
	}

	public boolean removeListener(EquippedItemsListener o) {
		return listeners.remove(o);
	}

	public void changed() {
		for (var o : listeners)
			o.changed();
	}

	public static interface EquippedItemsListener {
		public abstract void changed();
	}

	public void setInventory(OwnedItems inventory) {
		this.inventory = inventory;
	}
}
