package com.mygdx.holowyth.unit;

import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.util.Holo;

public class UnitStatCalculator {

	final UnitStatValues my;
	private final UnitStats self;

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

		my.str = self.strBase + equipBonus.str;
		my.agi = self.agiBase + equipBonus.agi;
		my.fort = self.fortBase + equipBonus.fort;
		my.percep = self.perceptBase + equipBonus.percep;

		my.atk = self.atkBase + equipBonus.atk + skillBonus.atk;
		my.def = self.defBase + equipBonus.def + skillBonus.def;
		my.force = self.forceBase + equipBonus.force + skillBonus.force;
		my.stab = self.stabBase + equipBonus.stab + skillBonus.stab;

		my.armor = self.armorBase + equipBonus.armor;
		my.percentArmor = self.percentArmorBase + equipBonus.percentArmor;
		my.armorPiercing = self.armorPiercingBase + equipBonus.armorPiercing;
		my.armorNegate = self.armorNegationBase + equipBonus.armorNegate;

		// Base stats have no effect atm

		final float weaponDamage = self.isWieldingAWeapon() ? self.getEquip().mainHand.damage : 0;
		my.damage = self.atkDamageBase + weaponDamage + skillBonus.damage;

		my.maxHp = Holo.debugHighHpUnits ? self.maxHpBase * 10 : self.maxHpBase;
		my.maxSp = self.maxSpBase;

	}

	public static class UnitStatValues {
		public int str, agi, fort, percep; // core stats
		public int maxHp, maxSp;
		public int atk, def, force, stab, acc, dodge;

		public int armor, armorPiercing;
		public float percentArmor, armorNegate;

		public float damage;

		/**
		 * adds the given values to the first set of values
		 */
		public UnitStatValues add(UnitStatValues other) {
			str += other.str;
			agi += other.agi;
			fort += other.fort;
			percep += other.percep;

			maxHp += other.maxHp;
			maxSp += other.maxSp;

			atk += other.atk;
			def += other.def;
			force += other.force;
			stab += other.stab;
			acc += other.acc;
			dodge += other.dodge;

			armor += other.armor;
			armorPiercing += other.armorPiercing;
			percentArmor += other.percentArmor;
			armorNegate += other.armorNegate;

			damage += other.damage;

			return this;
		}
	}

	private UnitStatValues calculateSkillStatBonuses() {
		var skills = self.self.skills.getSkills();

		UnitStatValues skillBonus = new UnitStatValues();
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

		UnitStatValues equipBonus = new UnitStatValues();

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
				equipBonus.acc += item.accBonus;
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

	public int getAcc() {
		return my.acc;
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
