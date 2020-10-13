package com.mygdx.holowyth.gamedata.items;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.mygdx.holowyth.unit.item.Equip;
import com.mygdx.holowyth.unit.item.Equip.TemplateEquip;
import com.mygdx.holowyth.unit.item.Equip.WeaponType;
import com.mygdx.holowyth.unit.item.Equip.EquipType;

/**
 * Contains equipment templates which can be copied and given to units
 *
 * Generally weaker equips are listed first
 *
 */
@NonNullByDefault
public class Weapons {

	public final static Equip club = new TemplateEquip("Club", WeaponType.CLUB) {
		{
			setBasicBonuses(2, 1, 2, 0, 0);
		};
	};
	public final static Equip dagger = new TemplateEquip("Dagger", WeaponType.DAGGER) {
		{
			setBasicBonuses(2, 1, 0, 0, 0);
			baseAtkSpd = 1.3f;
		};
	};

	public final static Equip shortSword = new TemplateEquip("Short Sword", WeaponType.SWORD) {
		{
			setBasicBonuses(3, 2, 0, 0, 0);
			baseAtkSpd = 1.2f;
		}
	};
	public final static Equip staff = new TemplateEquip("Staff", WeaponType.STAFF) {
		{
			setBasicBonuses(3, 2, 3, 1, 2);
			baseAtkSpd = 0.8f;
		}
	};
	public final static Equip spear = new TemplateEquip("Spear", WeaponType.POLEARM) {
		{
			setBasicBonuses(4, 5, 1, 0, 1);
			baseAtkSpd = 0.9f;
		};
	};

	public final static Equip machete = new TemplateEquip("Machete", WeaponType.SWORD) {
		{
			setBasicBonuses(5, 3, 2, 0, 1);
		}
	};

	public final static Equip longSword = new TemplateEquip("Long Sword", WeaponType.SWORD) {
		{
			setBasicBonuses(5, 4, 2, 1, 1);
		}

	};

	public final static Equip mace = new TemplateEquip("Mace", WeaponType.CLUB) {
		{
			setBasicBonuses(6, 3, 5, 0, 1);
		}
	};
	public final static Equip bow = new TemplateEquip("Bow", WeaponType.BOW) {
		{
			setBasicBonuses(6, 3, 5, 0, 1);
			is2HWeapon = true;
		}
	};

}