package com.mygdx.holowyth.unit;

public class UnitStatValues implements Cloneable {
	public int str, agi, fort, percep; // core stats
	public int maxHp, maxSp;
	public int atk, def, force, stab, acc, dodge;

	public int armor, armorPiercing;
	public float percentArmor, armorNegate;

	public float damage;

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
		acc = 0;
		dodge = 0;

		armor = 0;
		armorPiercing = 0;
		percentArmor = 0;
		armorNegate = 0;

		damage = 0;
	}

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

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}