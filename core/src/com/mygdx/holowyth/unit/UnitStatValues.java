package com.mygdx.holowyth.unit;

/**
 * Used to represent stat values -- also used for additive bonuses
 */
public class UnitStatValues implements Cloneable {
	public int str, agi, fort, percep; // core stats
	public int maxHp, maxSp;
	public float damage;
	public int atk, def, force, stab, dodge;
	public float atkspd;

	/** Flat armor */
	public int armor;
	/** Is between 0 and 1. Eg. 0.2 means 20% damage reduction */
	public float percentArmor;
	public int armorPiercing;
	public float armorNegate;

	public UnitStatValues() {
	}

	public void set(UnitStatValues src) {
		str = src.str;
		agi = src.agi;
		fort = src.fort;
		percep = src.percep;

		maxHp = src.maxHp;
		maxSp = src.maxSp;

		damage = src.damage;

		atk = src.atk;
		def = src.def;
		force = src.force;
		stab = src.stab;
		dodge = src.dodge;
		
		atkspd = src.atkspd;

		armor = src.armor;
		armorPiercing = src.armorPiercing;
		percentArmor = src.percentArmor;
		armorNegate = src.armorNegate;
	}

	public void zero() {
		str = 0;
		agi = 0;
		fort = 0;
		percep = 0;

		maxHp = 0;
		maxSp = 0;

		atk = 0;
		def = 0;
		force = 0;
		stab = 0;
		dodge = 0;
		
		atkspd = 0;

		armor = 0;
		armorPiercing = 0;
		percentArmor = 0;
		armorNegate = 0;

		damage = 0;
	}

	/**
	 * Adds the given values to the first set of values. One of the UnitStatValues should represent a bonus (e.g. it doesn't max sense to add 2 unit's hps together)
	 */
	public UnitStatValues add(UnitStatValues other) {
		str += other.str;
		agi += other.agi;
		fort += other.fort;
		percep += other.percep;

		maxHp += other.maxHp;
		maxSp += other.maxSp;

		damage += other.damage;
		
		atkspd += other.atkspd;
		
		atk += other.atk;
		def += other.def;
		force += other.force;
		stab += other.stab;
		dodge += other.dodge;

		armor += other.armor;
		armorPiercing += other.armorPiercing;
		percentArmor += other.percentArmor;
		armorNegate += other.armorNegate;

		return this;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}