package com.mygdx.holowyth.units;

public class Item {
	
	public enum ItemType{EQUIP, CONSUMABLE, OTHER}
	ItemType type;
	public enum EquipType{HEADGEAR, ARMOR, WEAPON, SHIELD, ACCESSORY}
	EquipType equipType;
	
	int damage;
	
	int strBonus, agiBonus, fortBonus, percepBonus;
	int atkBonus, defBonus, forceBonus, stabBonus, accBonus, dodgeBonus; //conditional bonuses are handled manually in the combat simulator, for now.
	
	String name;

	int armorPiercingBonus; // both of these stack additively.
	int armorNegationBonus;
	
	boolean is2HWeapon;
	
	public Item(String name){
		this.name = name;
	}
	public Item(){
		this("Untitled Item");
	}
	
	public void test(){
		System.out.println(type);
	}
}
