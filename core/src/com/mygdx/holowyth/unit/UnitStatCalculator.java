package com.mygdx.holowyth.unit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.unit.WornEquips.Slot;
import com.mygdx.holowyth.unit.item.Equip;
import com.mygdx.holowyth.util.Holo;

public class UnitStatCalculator {
	private final UnitStats self;
	private final UnitStatValues my;

	// After recalculateStats() is called, these will accurately reflect the combined skill and equip bonuses
	final UnitStatValues skillBonus = new UnitStatValues();
	final UnitStatValues equipBonus = new UnitStatValues();

	UnitStatCalculator(UnitStats self) {
		this.self = self;
		my = new UnitStatValues();
	}

	/**
	 * Call this when ever stats need to be recalculated (ie. items equipped/un-equipped, base stats changes
	 */
	void recalculateStats() {

		var equip = self.getEquip();

		calculateSkillStatBonuses(skillBonus);
		calculateEquipStatBonuses(equipBonus, WornEquips.Slot.HEAD, WornEquips.Slot.BODY, WornEquips.Slot.MAIN_HAND, WornEquips.Slot.ACCESSORY);

		final @Nullable Equip mainHand = equip.getEquip(WornEquips.Slot.MAIN_HAND);
		final @Nullable Equip offHand = equip.getEquip(WornEquips.Slot.OFF_HAND);
		 
		if (mainHand != offHand) // don't count a 2H weapon twice
			addEquipStatBonuses(equipBonus, Slot.OFF_HAND);

		my.set(self.base);
		my.add(skillBonus);
		my.add(equipBonus);

		// By default bonuses are simply added, overwrite the value if desired

		if(mainHand != null) {
			my.atkspd = mainHand.baseAtkSpd; 
		}else if(offHand != null) {
			my.atkspd = offHand.baseAtkSpd;
		}else {
			my.atkspd = 1;	
		}
		
		my.maxHp = Holo.debugHighHpUnits ? self.base.maxHp * 10 : self.base.maxHp;
		my.maxSp = self.base.maxSp;
	}

	private void calculateSkillStatBonuses(UnitStatValues values) {
		values.zero();
		var skills = self.self.skills.getSkills();

		for (Skill skill : skills) {
			values.damage += skill.damBonus;

			values.atk += skill.atkBonus;
			values.def += skill.defBonus;
			values.force += skill.forceBonus;
			values.stab += skill.stabBonus;
		}
	}

	private void calculateEquipStatBonuses(UnitStatValues values, WornEquips.Slot... slots) {
		values.zero();
		addEquipStatBonuses(values, slots);
	}

	private void addEquipStatBonuses(UnitStatValues values, WornEquips.Slot... slots) {
		for (WornEquips.Slot slot : slots) {
			var item = self.getEquip().getEquip(slot);
			if (item != null) {
				values.add(item.bonus);
			}
		}
	}

	public int getStr() {
		return my.str;
	}

	public int getAgi() {
		return my.agi;
	}

	public int getFort() {
		return my.fort;
	}

	public int getPercep() {
		return my.percep;
	}

	public int getMaxHp() {
		return my.maxHp;
	}

	public int getMaxSp() {
		return my.maxSp;
	}

	public int getAtk() {
		return my.atk;
	}

	public int getDef() {
		return my.def;
	}

	public int getForce() {
		return my.force;
	}

	public int getStab() {
		return my.stab;
	}

	public int getDodge() {
		return my.dodge;
	}

	public int getArmor() {
		return my.armor;
	}

	public int getArmorPiercing() {
		return my.armorPiercing;
	}

	public float getPercentageArmor() {
		return my.percentArmor;
	}

	public float getArmorNegation() {
		return my.armorNegate;
	}

	public void setMaxHp(int maxHp) {
		my.maxHp = maxHp;
	}

	public void setMaxSp(int maxSp) {
		my.maxSp = maxSp;
	}

	public float getDamage() {
		return my.damage;
	}
	public float getRangedDamage() {
		return my.rangedDamage;
	}
	public float getAtkspd() {
		return my.atkspd;
	}

	public int getRangedAtk() {
		return my.rangedAtk;
	}

	public int getRangedForce() {
		return my.rangedForce;
	}


}
