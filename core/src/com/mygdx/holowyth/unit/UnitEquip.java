package com.mygdx.holowyth.unit;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.unit.WornEquips.Slot;
import com.mygdx.holowyth.unit.item.Equip;

/**
 * @author Colin Ta
 */
public class UnitEquip {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Unit self;

	private final WornEquips wornEquips = new WornEquips();

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
		boolean isSuccess = wornEquips.equip(item);
		self.stats.recalculateStats();
		return isSuccess;
	}

	public boolean unequip(Equip item) {
		return wornEquips.unequip(item);
	}

	public boolean hasEquipped(Equip equip) {
		return wornEquips.hasEquipped(equip);
	}

	public Equip getEquip(WornEquips.Slot slot) {
		return wornEquips.getEquip(slot);
	}

	/**
	 * @return Read-only collection of the equip slots
	 */
	public Map<WornEquips.@NonNull Slot, @NonNull Equip> getEquipSlots() {
		return wornEquips.getEquipSlots();
	}

	public boolean is2HWieldingWeapon() {
		return wornEquips.is2HWieldingWeapon();
	}

	void equipAllFromTemplate(WornEquips src) {
		for (Slot slot : Slot.values()) {
			if (slot == Slot.OFF_HAND && !src.is2HWieldingWeapon()) {
				equip(src.getEquip(Slot.OFF_HAND).cloneObject());
			} else {
				equip(src.getEquip(slot).cloneObject());
			}
		}
	}
}