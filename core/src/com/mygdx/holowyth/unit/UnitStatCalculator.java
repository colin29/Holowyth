package com.mygdx.holowyth.unit;

import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.util.Holo;

public class UnitStatCalculator {

	final UnitStatValues my;
	private final UnitStats self;

	// After recalculateStats() is called, these will accurately reflect the combined skill and equip bonuses
	final UnitStatValues skillBonus = new UnitStatValues();
	final UnitStatValues equipBonus = new UnitStatValues();

	UnitStatCalculator(UnitStats self) {
		this.self = self;
		this.my = new UnitStatValues();
	}

	/**
	 * Call this when ever stats need to be recalculated (ie. items equipped/un-equipped, base stats changes
	 */
	void recalculateStats() {

		var equip = self.getEquip();

		var equipBonus = calculateCoreStatEquipBonuses(equip.head, equip.torso, equip.mainHand, equip.accessory1, equip.accessory2);
		var skillBonus = calculateSkillStatBonuses();

		if (equip.mainHand != equip.offHand) // don't count a 2H weapon twice
			equipBonus.add(calculateCoreStatEquipBonuses(equip.offHand));

		my.str = self.base.str + equipBonus.str;
		my.agi = self.base.agi + equipBonus.agi;
		my.fort = self.base.fort + equipBonus.fort;
		my.percep = self.base.percep + equipBonus.percep;

		my.atk = self.base.atk + equipBonus.atk + skillBonus.atk;
		my.def = self.base.def + equipBonus.def + skillBonus.def;
		my.force = self.base.force + equipBonus.force + skillBonus.force;
		my.stab = self.base.stab + equipBonus.stab + skillBonus.stab;

		my.armor = self.base.armor + equipBonus.armor;
		my.percentArmor = self.base.percentArmor + equipBonus.percentArmor;
		my.armorPiercing = self.base.armorPiercing + equipBonus.armorPiercing;
		my.armorNegate = self.base.armorNegate + equipBonus.armorNegate;

		// stats have no effect atm

		final float weaponDamage = self.isWieldingAWeapon() ? self.getEquip().mainHand.damage : 0;
		my.damage = self.base.damage + weaponDamage + skillBonus.damage;

		my.maxHp = Holo.debugHighHpUnits ? self.base.maxHp * 10 : self.base.maxHp;
		my.maxSp = self.base.maxSp;
	}

	private UnitStatValues calculateSkillStatBonuses() {
		skillBonus.zero();
		var skills = self.self.skills.getSkills();

		for (Skill skill : skills) {
			skillBonus.atk += skill.atkBonus;
			skillBonus.def += skill.defBonus;
			skillBonus.force += skill.forceBonus;
			skillBonus.stab += skill.stabBonus;

			skillBonus.damage += skill.damBonus;
		}
		return skillBonus;
	}

	private UnitStatValues calculateCoreStatEquipBonuses(Item... items) {
		equipBonus.zero();
		for (Item item : items) {
			if (item != null) {
				equipBonus.str += item.strBonus;
				equipBonus.agi += item.agiBonus;
				equipBonus.fort += item.fortBonus;
				equipBonus.percep += item.percepBonus;

				equipBonus.atk += item.atkBonus;
				equipBonus.def += item.defBonus;
				equipBonus.force += item.forceBonus;
				equipBonus.stab += item.stabBonus;
				equipBonus.dodge += item.dodgeBonus;

				equipBonus.armor += item.armorBonus;
				equipBonus.percentArmor += item.dmgReductionBonus;
				equipBonus.armorPiercing += item.armorPiercingBonus;
				equipBonus.armorNegate += item.armorNegationBonus;
			}
		}
		return equipBonus;

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

}
