package com.mygdx.holowyth.units;

public class Unit {
	
	private float hp;
	private int sp;
	private int maxHp, maxSp;
	
	private int baseMaxHp, baseMaxSp;
	private float baseMoveSpeed;
	
	private int exp;
	private int expGives;

	private int atk, def, force, stab, acc, dodge; //calculated stats
	
	private int str, agi, fort, percep; // Base stats
	
	public enum Stance {DEFENSIVE, NORMAL, ALL_OUT_AGGRESSIVE};
	private Stance stance;
	
	public enum StunState {NORMAL, STUNNED, REELING};
	private StunState stunState;
	
	private int stunDurationRemainng;
	
	public enum Status {PRONE, FATIGUED, MANA_EXHAUSTION, WOUNDED, FEARFUL}
	
	private Unit lookingAt;
	private Unit occupiedBy;
	
	private EquippedItems equip;
	
	
	
	
	/**
	 * 
	 * Calculates correct calculated stats as well as movement speed, movementSpeed. 
	 * The base stats should be modified and status effects set before calling this
	 * 
	 * Call before anytime you have to read stats from Unit e.g combat calculations or a debug print or displaying on UI
	 */
	public void recalculateStats(){
		//TODO:
	}
	
	
	public class EquippedItems{
		Item head;
		Item mainHand;
		Item offHand;
		Item torso;
		Item accessory1;
		Item accessory2;
		public boolean isWielding2HWeapon(){
			//TODO:
			return false;
		}
		
	}
	
}
