package com.mygdx.holowyth.unit;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.unit.WornEquips.Slot;
import com.mygdx.holowyth.unit.item.Equip;

/**
 * @author Colin Ta
 */
@NonNullByDefault
public class UnitEquip {
	
	@SuppressWarnings("null")
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

	public @Nullable Equip getEquip(WornEquips.Slot slot) {
		return wornEquips.getEquip(slot);
	}

	/**
	 * @return Read-only collection of the equip slots
	 */
	public Map<WornEquips.@NonNull Slot, @NonNull Equip> getEquipped() {
		return wornEquips.getEquipped();
	}

	public boolean is2HWieldingWeapon() {
		return wornEquips.is2HWieldingWeapon();
	}

	void equipAllFromTemplate(WornEquips src) {
		for (var e : src.getEquipped().entrySet()) { // this only iterates over occupied slots, because nonNull map
			if (e.getKey() == Slot.OFF_HAND && !src.is2HWieldingWeapon()) {
				equip(e.getValue().cloneObject());
			} else {
				equip(e.getValue().cloneObject());
			}
		}
	}

	public WornEquips getWornEquips() {
		return wornEquips;
	}
}