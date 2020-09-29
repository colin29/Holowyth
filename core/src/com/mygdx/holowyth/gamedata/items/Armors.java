package com.mygdx.holowyth.gamedata.items;

import com.mygdx.holowyth.unit.item.Equip;
import com.mygdx.holowyth.unit.item.Equip.TemplateEquip;
import com.mygdx.holowyth.unit.item.Equip.EquipType;

public class Armors {
	public final static Equip tatteredLeatherArmor = new TemplateEquip("Tattered Leather Armor", EquipType.ARMOR) {
		{
			bonus.armor = 1;
			bonus.stab = 1;
		};
	};
	
	public final static Equip rustyChainmail = new TemplateEquip("Rusty Chainmail", EquipType.ARMOR) {
		{
			bonus.armor = 3;
			bonus.stab = 3;
		};
	};
}
