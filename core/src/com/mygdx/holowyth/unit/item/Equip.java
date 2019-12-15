package com.mygdx.holowyth.unit.item;

import static com.mygdx.holowyth.util.DataUtil.getAsPercentage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.utils.Array;
import com.mygdx.holowyth.unit.UnitStatValues;

/**
 * Only equips have bonuses
 *
 */
public class Equip extends Item {

	public enum EquipType {
		HEADGEAR, ARMOR, WEAPON, SHIELD, ACCESSORY
	}

	public EquipType equipType;
	public final UnitStatValues bonus = new UnitStatValues();
	public boolean is2HWeapon;

	public Equip(String name, EquipType equipType) {
		this.name = name;
		this.itemType = ItemType.EQUIP;
		this.equipType = equipType;
	}

	public Equip(String name) {
		this(name, false);
	}

	public Equip(String name, boolean isTemplate) {
		this.name = name;
		this.isTemplate = isTemplate;
	}

	public Equip copy() {
		Equip item = new Equip(name, equipType);
		item.bonus.set(bonus);
		item.is2HWeapon = is2HWeapon;
		return item;
	}

	public void printInfo() {
		System.out.println(getInfo());
	}

	public String getInfo() {
		String s = "";
		s += String.format("[%s]  %s%n", name, getCompleteItemType());
		if (bonus.damage > 0) {
			s += String.format(" Damage: %f%n", bonus.damage);
		}
		s += " " + getEquipBonusesAsText();
		return s;
	}

	public String getCompleteItemType() {
		if (itemType == Equip.ItemType.EQUIP) {
			return equipType.toString() + " (Equip)";
		} else {
			return itemType.toString();
		}
	}

	public String getEquipBonusesAsText() {
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
}
