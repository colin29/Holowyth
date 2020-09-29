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
	public Map<WornEquips.Slot, Equip> getEquipSlots() {
		return wornEquips.getEquipSlots();
	}

	public boolean is2HWieldingWeapon() {
		return wornEquips.is2HWieldingWeapon();
	}

}