package com.mygdx.holowyth.unit;

import static com.mygdx.holowyth.util.DataUtil.getAsPercentage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.utils.Array;

public class Item {

	public enum ItemType {
		EQUIPMENT, CONSUMABLE, OTHER
	}

	public enum EquipType {
		HEADGEAR, ARMOR, WEAPON, SHIELD, ACCESSORY
	}

	public String name;

	public ItemType itemType;
	public EquipType equipType;

	public final UnitStatValues bonus = new UnitStatValues();
	public boolean is2HWeapon;

	private boolean isTemplate;

	public Item copy() {
		Item item = new Item(this.name);

		item.itemType = itemType;
		item.equipType = equipType;

		item.bonus.set(bonus);
		item.is2HWeapon = is2HWeapon;
		return item;
	}

	public Item(String name, EquipType equipType) {
		this(name, ItemType.EQUIPMENT);
		this.equipType = equipType;
	}

	public Item(String name, ItemType itemType) {
		this(name);
		this.itemType = itemType;
	}

	public Item() {
		this("Untitled Item");
	}

	public Item(String name) {
		this(name, false);
	}

	public Item(String name, boolean isTemplate) {
		this.name = name;
		this.isTemplate = isTemplate;
	}

	public void printInfo() {
		System.out.println(getInfo());
	}

	public String getInfo() {
		String s = "";
		s += String.format("[%s]  %s%n", name, getCompleteItemType());
		if (bonus.damage > 0) {
			s += String.format(" Damage: %d%n", bonus.damage);
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

		registerStat.accept("str", bonus.str);
		registerStat.accept("agi", bonus.agi);
		registerStat.accept("fort", bonus.fort);
		registerStat.accept("percep", bonus.percep);

		registerStat.accept("atk", bonus.atk);
		registerStat.accept("def", bonus.def);
		registerStat.accept("force", bonus.force);
		registerStat.accept("stab", bonus.stab);
		registerStat.accept("dodge", bonus.dodge);

		registerStat.accept("Armor", bonus.armor);
		registerStat.accept("DR", bonus.percentArmor);

		registerStat.accept("AP", bonus.armorPiercing);
		registerStat.accept("Armor Negate", bonus.armorNegate);

		String s = "";

		for (String statBonusLabel : ordering) {
			Object statBonus = m.get(statBonusLabel);
			if (statBonus instanceof Integer) {
				int sbInt = (Integer) statBonus;
				if (sbInt != 0) {
					String sign = sbInt > 0 ? "+" : "-";
					s += statBonusLabel + sign + Math.abs(sbInt) + ", ";
				}
			} else if (statBonus instanceof Float) {
				float sbFloat = (Float) statBonus;
				if (sbFloat != 0) {
					String sign = sbFloat > 0 ? "+" : "-";
					s += statBonusLabel + " " + sign + getAsPercentage(Math.abs(sbFloat)) + ", ";
				}
			} else {
				System.out.printf("Invalid data type %s for label %s%n", statBonus.getClass().getSimpleName(),
						statBonusLabel);
			}
		}
		s = StringUtils.removeEnd(s, ", ");
		s += "\n";
		return s;
	}

	/**
	 * Items that are templates are meant to be copied and should not be equipped
	 */
	public void markAsTemplate() {
		isTemplate = true;
	}

	public boolean isTemplate() {
		return isTemplate;
	}
}
