package com.mygdx.holowyth.unit;

import static com.mygdx.holowyth.util.DataUtil.*;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.utils.Array;
import com.mygdx.holowyth.combatDemo.effects.EffectsHandler;
import com.mygdx.holowyth.util.DataUtil;

/**
 * Simple stat fields are exposed public, while those which may trigger extra handling will be exposed through getters
 * and setters
 * 
 * @author Colin Ta
 *
 */
public class UnitStats implements UnitStatsInfo {

	public static boolean printDetailedCombatInfo = false;

	Unit self;

	// App fields
	EffectsHandler effects;

	// Info stats

	private String name;

	// Base stats
	public int baseMaxHp, baseMaxSp;
	public float baseMoveSpeed;

	public int baseStr, baseAgi, baseFort, basePercep;

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

	/**
	 * A unit that is distracted by a target is easier to surpise
	 */
	private UnitStats lookingAt;
	private UnitStats occupiedBy;

	private EquippedItems equip = new EquippedItems();

	public int level;

	public enum UnitType { // Player-like characters have their derived stats calculated like players. Monsters do not.
		PLAYER, MONSTER
	}

	public UnitType unitType;

	public UnitStats(Unit unit) {
		this.self = unit;
		this.effects = unit.getWorld().getEffectsHandler();
	}

	public UnitStats(String name, Unit unit) {
		this(unit);
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
		recalculateStats();

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
		s += String.format(" -Damage %s, AP %d, Armor Negation %s %n", getRoundedString(getUnitAttackDamage()),
				armorPiercing, getAsPercentage(armorNegation));
		s += String.format(" -Armor %d, DR %s %n", armor, getAsPercentage(dmgReduction));

		s += "Equipped Items:\n";
		s += getEquipped();

		s += "Item details:\n";

		s += getInfoForAllEquippedItems();

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

	public String getInfoForAllEquippedItems() {
		// Get a list of all distinct items (different names)

		List<Item> distinctItems = new ArrayList<Item>();

		for (Item item : equip.getArrayOfEquipSlots()) {
			if (item == null)
				continue;
			if (distinctItems.stream().noneMatch(i -> i.name == item.name)) {
				distinctItems.add(item);
			}
		}

		String s = "";
		for (Item item : distinctItems) {
			s += item.getInfo();
		}
		return s;
	}

	public void printInfo() {
		System.out.println(getInfo());
	}

	public String getRoundedHp() {
		return getRoundedString(hp);
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
	 * 
	 * @param enemy
	 */
	public void attack(UnitStats enemy) {
		if (this == enemy) {
			System.out.println("Error, cannot attack self");
			return;
		}
		if (this.isDead()) {
			System.out.println("Error, self is dead");
			return;
		}
		if (enemy.isDead()) {
			System.out.println("Cannot attack a dead target");
			return;
		}
		
		// System.out.printf("%s attacks %s%n", this.name, enemy.name);

		float chanceToHit = 0;

		// 1. Simulate dodge chance

		int acc = this.acc;
		int dodge = enemy.dodge;

		chanceToHit = (float) (Math.pow(2, (acc - dodge) / 10f) * 0.5);
		chanceToHit = Math.min(accChanceCeiling, chanceToHit);
		chanceToHit = Math.max(accChanceFloor, chanceToHit);
		if(printDetailedCombatInfo)
			System.out.printf(" -Chance to acc: %f %d relative acc %n", chanceToHit, acc - dodge);

		if (Math.random() > chanceToHit) {
			System.out.printf("%s's attack missed %s %n", this.name, enemy.name);
			effects.makeMissEffect(enemy.self);
			return;
		}

		// 2. Simulate block chance

		int atk = this.atk;
		int def = enemy.def;

		chanceToHit = (float) (Math.pow(2, (atk - def) / 10f) * 0.25);
		chanceToHit = Math.min(atkChanceCeiling, chanceToHit);
		chanceToHit = Math.max(atkChanceFloor, chanceToHit);
		if(printDetailedCombatInfo)
			System.out.printf(" -Chance to land strike: %f %d relative acc %n", chanceToHit, atk - def);

		if (Math.random() > chanceToHit) {
			System.out.printf("%s's attack was blocked by %s %n", this.name, enemy.name);
			return;
		}

		// 3. Calculate damage and reduction from armor

		float origDamage;

		origDamage = getUnitAttackDamage();

		float damageReduced = Math.max(enemy.armor - armorPiercing, origDamage * enemy.dmgReduction);
		damageReduced *= (1 - armorNegation);

		float damage = origDamage - damageReduced;

		if (damage < 0) {
			damage = 0;
		}

		// 5. Apply damage
		System.out.printf("%s's attack hit and did %s damage to %s%n", this.name, DataUtil.getRoundedString(damage), enemy.name);

		effects.makeDamageEffect(damage, enemy.self);
		enemy.applyDamage(damage);
	}

	public float getUnitAttackDamage() {
		float weaponDamage = isWieldingAWeapon() ? equip.mainHand.damage : 1;
		float strengthBonus = (this.str - 5) * 0.1f;

		return weaponDamage * (1 + strengthBonus);
	}

	/**
	 * @param damage
	 * @return The amount of damage actually done
	 */
	public float applyDamage(float damage) {
		hp -= damage;

		if (hp < 0) {
			hp = 0;
		}

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
		public Item head;
		public Item mainHand;
		public Item offHand;
		public Item torso;
		public Item accessory1;
		public Item accessory2;

		public boolean isWielding2HWeapon() {
			// TODO:
			return false;
		}

		public static final Array<String> slotLabels = new Array<String>();
		static {
			slotLabels.addAll("Head", "Main Hand", "Off Hand", "Torso", "Accessory 1", "Accessory 2");
		}

		/**
		 * Note that the returned value will become outdated if any items are equipped/un-equipped
		 * 
		 * Like the fields, null means no item equipped
		 * 
		 * @return A list of the items in each field, in order. Each index corresponds to an equip slot, so null is a
		 *         valid value
		 */
		private Array<Item> getArrayOfEquipSlots() {
			Array<Item> a = new Array<Item>();
			a.addAll(head, mainHand, offHand, torso, accessory1, accessory2);
			assert (a.size == slotLabels.size);
			return a;
		}

		/**
		 * Allows other classes to consistently get all the equip slots and their content in order <br>
		 * Note that the returned map will become outdated if any items are equipped/un-equipped. <br>
		 * Like the fields, null means no item equipped. Some items, namely 2h weapons will appear in both hand slots
		 * <br>
		 * 
		 * @return A map of the equip slots, slotName -> Item
		 */
		public Map<String, Item> getIteratableMap() {
			Map<String, Item> map = new HashMap<String, Item>();

			Array<Item> curItems = getArrayOfEquipSlots();

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

	public float getHpRatio() {
		return (float) hp / maxHp;
	}

	public float getSpRatio() {
		return (float) sp / maxSp;
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

	public UnitStats getLookingAt() {
		return lookingAt;
	}

	public UnitStats getOccupiedBy() {
		return occupiedBy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
