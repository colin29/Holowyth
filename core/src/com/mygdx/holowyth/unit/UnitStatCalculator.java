package com.mygdx.holowyth.unit;

import com.mygdx.holowyth.util.Holo;

public class UnitStatCalculator {

	// Calculated stats
	private int str, agi, fort, percep; // core stats
	private int maxHp, maxSp;
	private int atk, def, force, stab, acc, dodge;

	private int armor, armorPiercing;
	private float dmgReduction, armorNegation;

	// Helper Stats (for summing up stat contributions). 'i' stands for interim
	private int strBonus, agiBonus, fortBonus, perceptBonus;
	private int iAtk, iDef, iForce, iStab, iAcc, iDodge;
	private int iArmor, iArmorPiercing;
	private float iDmgReduction, iArmorNegation;

	private final UnitStats self;

	UnitStatCalculator(UnitStats self) {
		this.self = self;
	}

	/**
	 * Call this when ever stats need to be recalculated (ie. items equipped/un-equipped, base stats changes
	 */
	void recalculateStats() {
		// private int maxHp, maxSp;
		// private int str, agi, fort, percep;
		// int atk, def, force, stab, acc, dodge;

		// 1: calculate new stats from equipment bonuses

		strBonus = 0;
		agiBonus = 0;
		fortBonus = 0;
		perceptBonus = 0;

		var equip = self.getEquip();

		addCoreStatBonusesFor(equip.head, equip.torso, equip.mainHand, equip.accessory1, equip.accessory2);
		if (equip.mainHand != equip.offHand) { // avoid adding bonuses from a 2H-wielded weapon twice
			addCoreStatBonusesFor(equip.offHand);
		}

		str = self.strBase + strBonus;
		agi = self.agiBase + agiBonus;
		fort = self.fortBase + fortBonus;
		percep = self.perceptBase + perceptBonus;

		// 2: calculate hp stats

		// Base stats

		maxHp = Holo.debugHighHpUnits ? self.maxHpBase * 10 : self.maxHpBase; // Math.round(baseMaxHp * (1 + 0.1f * (fort - 5)));
		maxSp = self.maxSpBase;

		// 3: calculate derived stats from core stats;

		iAtk = 0;
		iDef = 0;
		iForce = 0;
		iStab = 0;
		iAcc = 0;
		iDodge = 0;

		iArmor = 0;
		iDmgReduction = 0;
		iArmorPiercing = 0;
		iArmorNegation = 0;

		addDerivedStatBonusesFor(equip.head, equip.torso, equip.mainHand, equip.accessory1, equip.accessory2);
		if (equip.mainHand != equip.offHand) {
			addDerivedStatBonusesFor(equip.offHand);
		}

		int levelBonus = (self.level) * 2;
		int mid = 0;

		if (UnitStats.useTestAtkDef) {
			atk = self.testAtk;
			def = self.testDef;
		} else {
			atk = iAtk;
			def = iDef;
		}

		force = levelBonus + iForce + 2 * (str - mid);
		stab = levelBonus + iStab + 2 * (fort - mid) + 1 * (str - mid);
		acc = levelBonus + iAcc + 2 * (percep - mid);
		dodge = levelBonus + iDodge + 2 * (agi - mid);

		armor = iArmor;
		dmgReduction = iDmgReduction;
		armorPiercing = iArmorPiercing;
		armorNegation = iArmorNegation;
	}

	private void addCoreStatBonusesFor(Item... items) {
		for (Item item : items) {
			if (item != null) {
				strBonus += item.strBonus;
				agiBonus += item.agiBonus;
				fortBonus += item.fortBonus;
				perceptBonus += item.percepBonus;
			}
		}
	}

	private void addDerivedStatBonusesFor(Item... items) {

		for (Item item : items) {
			if (item != null) {
				iAtk += item.atkBonus;
				iDef += item.defBonus;
				iForce += item.forceBonus;
				iStab += item.stabBonus;
				iAcc += item.accBonus;
				iDodge += item.dodgeBonus;

				iArmor += item.armorBonus;
				iDmgReduction += item.dmgReductionBonus;
				iArmorPiercing += item.armorPiercingBonus;
				iArmorNegation += item.armorNegationBonus;
			}
		}
	}

	public int getStr() {
		return str;
	}

	public int getAgi() {
		return agi;
	}

	public int getFort() {
		return fort;
	}

	public int getPercep() {
		return percep;
	}

	public int getMaxHp() {
		return maxHp;
	}

	public int getMaxSp() {
		return maxSp;
	}

	public int getAtk() {
		return atk;
	}

	public int getDef() {
		return def;
	}

	public int getForce() {
		return force;
	}

	public int getStab() {
		return stab;
	}

	public int getAcc() {
		return acc;
	}

	public int getDodge() {
		return dodge;
	}

	public int getArmor() {
		return armor;
	}

	public int getArmorPiercing() {
		return armorPiercing;
	}

	public float getDmgReduction() {
		return dmgReduction;
	}

	public float getArmorNegation() {
		return armorNegation;
	}

	public void setMaxHp(int maxHp) {
		this.maxHp = maxHp;
	}

	public void setMaxSp(int maxSp) {
		this.maxSp = maxSp;
	}

}
