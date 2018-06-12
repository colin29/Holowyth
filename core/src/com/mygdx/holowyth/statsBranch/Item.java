package com.mygdx.holowyth.statsBranch;

import static com.mygdx.holowyth.statsBranch.DataUtil.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;

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
	
	int armorBonus;
	float dmgReductionBonus; // is a percentage reduction

	String name;

	int armorPiercingBonus; // both of these stack additively.
	float armorNegationBonus;

	boolean is2HWeapon;

	public Item(String name, EquipType equipType) {
		this(name, ItemType.EQUIPMENT);
		this.equipType = equipType;
	}
	
	public Item(String name, ItemType itemType) {
		this(name);
		this.itemType = itemType;
	}
	
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
		s += String.format("[%s]  %s%n", name, getCompleteItemType());
		if(damage>0) {
			s += String.format(" Damage: %d%n", damage);
		}
		s += " " + getListOfEquipBonuses();
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
		// Need to handle both ints and floats
		Map<String, Object> m = new HashMap<String, Object>();
		Array<String> ordering = new Array<String>(); // order which to display stat bonuses in, if any.

		/**
		 * StatBonuslabel is how you want the bonus to appear (e.g the "str" in "str+3")
		 */
		BiConsumer<String, Object> registerStat = (String statBonusLabel, Object statValue) -> {
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

		registerStat.accept("Armor", armorBonus);
		registerStat.accept("DR", dmgReductionBonus);
		
		registerStat.accept("AP", armorPiercingBonus);
		registerStat.accept("Armor Negate", armorNegationBonus);

		String s = "";

		for (String statBonusLabel : ordering) {
			Object statBonus = m.get(statBonusLabel);
			if(statBonus instanceof Integer) {
				int sbInt = (Integer) statBonus;
				if (sbInt != 0) {
					String sign = sbInt > 0 ? "+" : "-";
					s += statBonusLabel + sign + Math.abs(sbInt) + ", ";
				}
			}else if(statBonus instanceof Float) {
				float sbFloat = (Float) statBonus;
				if (sbFloat != 0) {
					String sign = sbFloat > 0 ? "+" : "-";
					s += statBonusLabel + " " + sign + getAsPercentage(Math.abs(sbFloat)) + ", ";
				}
			}else {
				System.out.printf("Invalid data type %s for label %s%n", statBonus.getClass().getSimpleName(), statBonusLabel);
			}
		}
		s = StringUtils.removeEnd(s, ", ");
		s += "\n";
		return s;
	}
}
