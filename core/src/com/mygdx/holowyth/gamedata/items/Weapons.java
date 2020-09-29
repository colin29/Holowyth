package com.mygdx.holowyth.gamedata.items;

import com.mygdx.holowyth.unit.item.Equip;
import com.mygdx.holowyth.unit.item.Equip.TemplateEquip;
import com.mygdx.holowyth.unit.item.Equip.EquipType;

/**
 * Contains equipment templates which can be copied and given to units
 *
 * Generally weaker equips are listed first
 *
 */
public class Weapons {

	
	public final static Equip club = new TemplateEquip("Club", EquipType.WEAPON) {
		{
			setBasicBonuses(2, 1, 2, 0, 0);
		};
	};
	public final static Equip dagger = new TemplateEquip("Dagger", EquipType.WEAPON) {
		{
			setBasicBonuses(2, 1, 0, 0, 0);
			baseAtkSpd = 1.3f;
		};
	};
	
	public final static Equip shortSword = new TemplateEquip("Short Sword", EquipType.WEAPON) {
		{
			setBasicBonuses(3, 2, 0, 0, 0);
			baseAtkSpd = 1.2f;
		}
	};
	public final static Equip staff = new TemplateEquip("Staff", EquipType.WEAPON) {
		{
			setBasicBonuses(3, 2, 3, 1, 2);
			baseAtkSpd = 0.8f;
		}
	};
	public final static Equip spear = new TemplateEquip("Spear", EquipType.WEAPON) {
		{
			setBasicBonuses(4, 5, 1, 0, 1);
			baseAtkSpd = 0.9f;
		};
	};
	
	public final static Equip machete = new TemplateEquip("Machete", EquipType.WEAPON) {
		{
			setBasicBonuses(5, 3, 2, 0, 1);
		}
	};
	
	public final static Equip longSword = new TemplateEquip("Long Sword", EquipType.WEAPON) {
		{
			setBasicBonuses(5, 4, 2, 1, 1);
		}
	};
	
	public final static Equip mace = new TemplateEquip("Long Sword", EquipType.WEAPON) {
		{
			setBasicBonuses(6, 3, 5, 0, 1);
		}
	};

}