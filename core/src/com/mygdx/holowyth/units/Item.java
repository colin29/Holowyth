package com.mygdx.holowyth.units;

public class Item {
	public enum ItemType{EQUIP, CONSUMABLE, OTHER}
	private ItemType type;
	public enum EquipType{HEADGEAR, ARMOR, WEAPON, SHIELD, ACCESSORY}
	private EquipType equipType;
	
	private int strBonus, agiBonus, fortBonus, percepBonus;
	private int atkBonus, defBonus, forceBonus, stabBonus, accBonus, dodgeBonus; //conditional bonuses are handled manually in the combat simulator, for now.
	
	private int armorPiercingBonus; // both of these stack additively.
	private int armorNegationBonus;
	
	public void test(){
		System.out.println(type);
	}
}
