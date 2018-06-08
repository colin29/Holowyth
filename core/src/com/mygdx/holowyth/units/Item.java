package com.mygdx.holowyth.units;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.badlogic.gdx.utils.Array;

public class Item {

	public enum ItemType {
		EQUIPMENT, CONSUMABLE, OTHER
	}

	ItemType itemType;

	public enum EquipType {
		HEADGEAR, ARMOR, WEAPON, SHIELD, ACCESSORY
	}

	EquipType equipType;

	int damage;

	int strBonus, agiBonus, fortBonus, percepBonus;
	int atkBonus, defBonus, forceBonus, stabBonus, accBonus, dodgeBonus; // conditional bonuses are handled manually in
																			// the combat simulator, for now.

	String name;

	int armorPiercingBonus; // both of these stack additively.
	int armorNegationBonus;

	boolean is2HWeapon;

	public Item(String name) {
		this.name = name;
	}

	public Item() {
		this("Untitled Item");
	}

	public void test() {
		System.out.println(itemType);
	}

	public void printInfo() {
		System.out.println(getInfo());
	}

	public String getInfo() {
		String s = "";
		s += String.format("Item [%s]  %s%n", name, getCompleteItemType());
		s += "Bonuses: " + getListOfEquipBonuses();
		return s;
	}

	public String getCompleteItemType() {
		if (itemType == Item.ItemType.EQUIPMENT) {
			return equipType.toString() + " (Equip)";
		} else {
			return itemType.toString();
		}
	}

	public String getListOfEquipBonuses() {

		Map<String, Integer> m = new HashMap<String, Integer>();
		Array<String> ordering = new Array<String>(); // order which to display stat bonuses in, if any.

		/**
		 * StatBonuslabel is how you want the bonus to appear (e.g the "str" in "str+3")
		 */
		BiConsumer<String, Integer> registerStat = (String statBonusLabel, Integer statValue) -> {
			m.put(statBonusLabel, statValue);
			ordering.add(statBonusLabel);
		};
	
		registerStat.accept("str", strBonus);
		registerStat.accept("agi", agiBonus);
		registerStat.accept("fort", fortBonus);
		registerStat.accept("percep", percepBonus);

		registerStat.accept("atk", atkBonus);
		registerStat.accept("def", defBonus);
		registerStat.accept("force", forceBonus);
		registerStat.accept("stab", stabBonus);
		registerStat.accept("acc", accBonus);
		registerStat.accept("dodge", dodgeBonus);

		registerStat.accept("AP", armorPiercingBonus);
		registerStat.accept("Armor Negate %", armorNegationBonus);

		String s = "";

		for (String statBonusLabel : ordering) {
			int statBonus = m.get(statBonusLabel);
			if (statBonus != 0) {
				String sign = statBonus > 0 ? "+" : "-";
				s += statBonusLabel + sign + Math.abs(statBonus) + ", ";
			}
		}
		s += "\n";
		return s;
	}

}
