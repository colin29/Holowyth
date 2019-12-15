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

		calculateSkillStatBonuses(skillBonus);
		calculateEquipStatBonuses(equipBonus, equip.head, equip.torso, equip.mainHand, equip.accessory1, equip.accessory2);

		if (equip.mainHand != equip.offHand) // don't count a 2H weapon twice
			addEquipStatBonuses(equipBonus, equip.offHand);

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

		final float weaponDamage = self.isWieldingAWeapon() ? self.getEquip().mainHand.bonus.damage : 0;
		my.damage = self.base.damage + weaponDamage + skillBonus.damage;

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

	private void calculateEquipStatBonuses(UnitStatValues values, Item... items) {
		values.zero();
		addEquipStatBonuses(values, items);
	}

	private void addEquipStatBonuses(UnitStatValues values, Item... items) {
		for (Item item : items) {
			if (item != null) {
				values.str += item.bonus.str;
				values.agi += item.bonus.agi;
				values.fort += item.bonus.fort;
				values.percep += item.bonus.percep;

				values.damage += item.bonus.damage;

				values.atk += item.bonus.atk;
				values.def += item.bonus.def;
				values.force += item.bonus.force;
				values.stab += item.bonus.stab;
				values.dodge += item.bonus.dodge;

				values.armor += item.bonus.armor;
				values.percentArmor += item.bonus.percentArmor;
				values.armorPiercing += item.bonus.armorPiercing;
				values.armorNegate += item.bonus.armorNegate;
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

}
