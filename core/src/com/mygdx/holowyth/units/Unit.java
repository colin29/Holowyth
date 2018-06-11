package com.mygdx.holowyth.units;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.utils.Array;

public class Unit implements UnitInfo {

	// Info stats

	private String name;
	private int id;

	// Base stats
	int baseMaxHp, baseMaxSp;
	float baseMoveSpeed;

	int baseStr, baseAgi, baseFort, basePercep;

	int expGives;

	// Progress stats
	int exp;

	float hp;
	int sp;

	// Calculated stats
	private int str, agi, fort, percep; // core stats
	private int maxHp, maxSp;
	private int atk, def, force, stab, acc, dodge;
	
	private int armor, armorPiercing;
	private float dmgReduction, armorNegation;

	// Helper Stats (summed up item bonuses)
	private int iStr, iAgi, iFort, iPercep; // core stats
	private int iAtk, iDef, iForce, iStab, iAcc, iDodge; 
	private int iArmor, iArmorPiercing;
	private float iDmgReduction, iArmorNegation;

	public enum Stance {
		DEFENSIVE, NORMAL, ALL_OUT_AGGRESSIVE
	};

	private Stance stance;

	public enum StunState {
		NORMAL, STUNNED, REELING
	};

	private StunState stunState;

	private int stunDurationRemainng;

	public enum Status {
		PRONE, FATIGUED, MANA_EXHAUSTION, WOUNDED, FEARFUL
	}

	Array<Status> statuses = new Array<Status>();

	private Unit lookingAt;
	private Unit occupiedBy;

	private EquippedItems equip = new EquippedItems();

	int level;

	public enum UnitType { // Player-like characters have their derived stats calculated like players. Monsters do not.
		PLAYER, MONSTER
	}

	UnitType unitType;



	public Unit() {
		this("Default Name");
	}

	public Unit(String name) {
		this.name = name;
	}
	

	/**
	 * 
	 * Calculates correct calculated stats as well as movement speed, movementSpeed. The base stats should be modified
	 * and status effects set before calling this
	 * 
	 * Call before anytime you have to read stats from Unit e.g combat calculations or a debug print or displaying on UI
	 */
	public void recalculateStats() {
		// private int maxHp, maxSp;
		// private int str, agi, fort, percep;
		// int atk, def, force, stab, acc, dodge;

		// 1: calculate new stats from equipment bonuses

		iStr = 0;
		iAgi = 0;
		iFort = 0;
		iPercep = 0;

		addCoreStatBonuses(equip.head, equip.torso, equip.mainHand, equip.accessory1, equip.accessory2);
		if (equip.mainHand != equip.offHand) { // avoid adding bonuses from a 2H-wielded weapon twice
			addCoreStatBonuses(equip.offHand);
		}

		str = baseStr + iStr;
		agi = baseAgi + iAgi;
		fort = baseFort + iFort;
		percep = basePercep + iPercep;

		// 2: calculate hp stats
		maxHp = Math.round(baseMaxHp * (1 + 0.1f * (fort - 5)));
		maxSp = baseMaxSp;

		// 3: calculate derived stats from core stats;

		iAtk = 0;
		iDef = 0;
		iForce = 0;
		iStab = 0;
		iAcc = 0;
		iDodge = 0;
		
		iArmor = 0;
		iDmgReduction = 0;
		iArmorPiercing = 0;
		iArmorNegation = 0;

		addDerivedStatBonuses(equip.head, equip.torso, equip.mainHand, equip.accessory1, equip.accessory2);
		if (equip.mainHand != equip.offHand) {
			addDerivedStatBonuses(equip.offHand);
		}

		int levelBonus = (level - 1) * 2;
		int mid = 5;

		atk = levelBonus + iAtk + 1 * (percep - mid);
		def = levelBonus + iDef + 1 * (agi - mid);
		force = levelBonus + iForce + 2 * (str - mid);
		stab = levelBonus + iStab + 2 * (fort - mid) + 1 * (str - mid);
		acc = levelBonus + iAcc + 2 * (percep - mid);
		dodge = levelBonus + iDodge + 2 * (agi - mid);
		
		armor = iArmor;
		dmgReduction = iDmgReduction;
		armorPiercing = iArmorPiercing;
		armorNegation = iArmorNegation;
		
		

	}

	/**
	 * Preps the unit for use in the game world by setting the transient fields to an appropriate starting value
	 * Idempotent (can call >=1 times)
	 */
	public void prepareUnit() {
		hp = maxHp;
		sp = maxSp;

		statuses.clear();
		stance = Stance.NORMAL;
		stunState = StunState.NORMAL;
		stunDurationRemainng = 0;

		lookingAt = null;
		occupiedBy = null;
	}

	public String getInfo() {
		recalculateStats();

		String s = "";
		s += String.format("Unit [%s]  hp: %s/%d  sp: %d/%d  <level %d>%n", name, getRoundedHp(), maxHp, sp, maxSp,
				level);
		s += String.format("Core stats: STR %d, AGI %d, FORT %d, PERCEP %d%n", str, agi, fort, percep);
		s += String.format("Derived stats: Atk %d, Def %d, Force %d, Stab %d, Acc %d, Dodge %d%n", atk, def, force,
				stab, acc, dodge);
		s += "Other stats: \n";
		s += String.format (" -Damage %s, AP %d, Armor Negation %s%% %n", getRoundedString(getUnitAttackDamage()), armorPiercing, getAsPercentage(armorNegation));
		s += String.format(" -Armor %d, DR %s%% %n", armor, getAsPercentage(dmgReduction));
		

		s += "Equipped Items:\n";
		s += getEquipped();
		
		s += "Item details:";
		
		//TODO:

		return s;
	}

	public String getEquipped() {
		String s = "";

		Map<String, Item> map = equip.getIteratableMap();

		for (String slotLabel : EquippedItems.slotLabels) {
			Item item = map.get(slotLabel);
			if (item != null) {
				s += " -" + slotLabel + ": " + item.name + "\n";
			} else {
				s += " -" + slotLabel + ": [None]\n";
			}
		}

		return s;
	}

	public void printInfo() {
		System.out.println(getInfo());
	}

	public String getRoundedHp() {
		return getRoundedString(hp);
	}
	private String getAsPercentage(float value) {
		return getRoundedString(value*100);
	}
	
	private String getRoundedString(float value) {
		DecimalFormat df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df.format(value);
	}

	private void addCoreStatBonuses(Item... items) {
		for (Item item : items) {
			if (item != null) {
				iStr += item.strBonus;
				iAgi += item.agiBonus;
				iFort += item.fortBonus;
				iPercep += item.percepBonus;
			}
		}
	}

	private void addDerivedStatBonuses(Item... items) {

		for (Item item : items) {
			if (item != null) {
				iAtk += item.atkBonus;
				iDef += item.defBonus;
				iForce += item.forceBonus;
				iStab += item.stabBonus;
				iAcc += item.accBonus;
				iDodge += item.dodgeBonus;
				
				iArmor += item.armorBonus;
				iDmgReduction += item.dmgReductionBonus;
				iArmorPiercing += item.armorPiercingBonus;
				iArmorNegation += item.armorNegationBonus;
			}
		}
	}

	// Combat/Attacking methods
	

	private float accChanceFloor = 0.05f;
	private float accChanceCeiling = 1f;

	private float atkChanceFloor = 0.01f;
	private float atkChanceCeiling = 1f;

	
/**
 * Called when an the unit makes an actual strike
 * @param enemy
 */
	public void attack(Unit enemy) {
		if(this == enemy) {
			System.out.println("Error, cannot attack self");
			return;
		}
		if(enemy.isDead()) {
			System.out.println("Cannot attack a dead target");
			return;
		}
		
//		System.out.printf("%s attacks %s%n", this.name, enemy.name);
		
		float chanceToHit = 0;
		
		// 1. Simulate dodge chance
		
		int acc = this.acc;
		int dodge = enemy.dodge;
		
		chanceToHit = (float) (Math.pow(2, (acc-dodge)/10f) * 0.5);
		chanceToHit = Math.min(accChanceCeiling, chanceToHit);
		chanceToHit = Math.max(accChanceFloor, chanceToHit);
		System.out.printf("Chance to acc: %f %d relative acc %n", chanceToHit, acc-dodge);
		
		
		if(Math.random() > chanceToHit) {
			System.out.printf("%s dodged %s's attack%n", enemy.name, this.name);
			return;
		}
		
		// 2. Simulate block chance
		
		int atk = this.atk;
		int def = enemy.def;
		
		chanceToHit = (float) (Math.pow(2, (atk - def)/10f) * 0.25);
		chanceToHit = Math.min(atkChanceCeiling, chanceToHit);
		chanceToHit = Math.max(atkChanceFloor, chanceToHit);
		System.out.printf("Chance to hit: %f %d relative acc %n", chanceToHit, atk-def);
		
		if(Math.random() > chanceToHit) {
			System.out.printf("%s blocked %s's attack%n", enemy.name, this.name);
			return;
		}
		
		// 3. Calculate damage and reduction from armor
		
		float origDamage;
		float weaponDamage = isWieldingAWeapon() ? equip.mainHand.damage : 0;
		float strengthBonus = (this.str-5)*0.1f;
		
		origDamage = getUnitAttackDamage();
		
		
		
		float damage = 999;
		
		// 5. Apply damage
		System.out.printf("%s hit and did %f damage to %s%n",this.name, damage, enemy.name);
		enemy.applyDamage(damage);
		
		
	}
	
	public float getUnitAttackDamage() {
		float weaponDamage = isWieldingAWeapon() ? equip.mainHand.damage : 0;
		float strengthBonus = (this.str-5)*0.1f;
		
		return weaponDamage * (1+strengthBonus);
	}
	
	/**
	 * @param damage
	 * @return The amount of damage actually done
	 */
	public float applyDamage(float damage) {
		hp -= damage;
		return damage;
	}
	public boolean isDead() {
		return hp <= 0;
	}
	
	public boolean isWieldingAWeapon() {
		return equip.mainHand != null;
	}
	
	
	public EquippedItems getEquip() {
		return this.equip;
	}

	public static class EquippedItems {
		Item head;
		Item mainHand;
		Item offHand;
		Item torso;
		Item accessory1;
		Item accessory2;

		public boolean isWielding2HWeapon() {
			// TODO:
			return false;
		}

		static final Array<String> slotLabels = new Array<String>();
		static {
			slotLabels.addAll("Head", "Main Hand", "Off Hand", "Torso", "Accessory 1", "Accessory 2");
		}

		/**
		 * Not that the return value will become outdated if any items are equipped/un-equipped
		 * 
		 * @return
		 */
		private Array<Item> getCurrentEquipsInOrder() {
			Array<Item> a = new Array<Item>();
			a.addAll(head, mainHand, offHand, torso, accessory1, accessory2);
			assert (a.size == slotLabels.size);
			return a;
		}

		public Map<String, Item> getIteratableMap() {
			Map<String, Item> map = new HashMap<String, Item>();

			Array<Item> curItems = getCurrentEquipsInOrder();

			for (int i = 0; i < slotLabels.size; i++) {
				map.put(slotLabels.get(i), curItems.get(i));
			}
			return map;
		}

	}

	public float getHp() {
		return hp;
	}

	public int getSp() {
		return sp;
	}

	public int getMaxHp() {
		return maxHp;
	}

	public int getMaxSp() {
		return maxSp;
	}

	public int getBaseMaxHp() {
		return baseMaxHp;
	}

	public int getBaseMaxSp() {
		return baseMaxSp;
	}

	public float getBaseMoveSpeed() {
		return baseMoveSpeed;
	}

	public int getExp() {
		return exp;
	}

	public int getExpGives() {
		return expGives;
	}

	public int getAtk() {
		return atk;
	}

	public int getDef() {
		return def;
	}

	public int getForce() {
		return force;
	}

	public int getStab() {
		return stab;
	}

	public int getAcc() {
		return acc;
	}

	public int getDodge() {
		return dodge;
	}

	public int getStr() {
		return baseStr;
	}

	public int getAgi() {
		return baseAgi;
	}

	public int getFort() {
		return baseFort;
	}

	public int getPercep() {
		return basePercep;
	}

	public Stance getStance() {
		return stance;
	}

	public StunState getStunState() {
		return stunState;
	}

	public int getStunDurationRemainng() {
		return stunDurationRemainng;
	}

	public Unit getLookingAt() {
		return lookingAt;
	}

	public Unit getOccupiedBy() {
		return occupiedBy;
	}

}
