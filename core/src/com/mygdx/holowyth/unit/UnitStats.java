package com.mygdx.holowyth.unit;

import static com.mygdx.holowyth.util.DataUtil.getRoundedString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.utils.Array;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.unit.interfaces.UnitStatsInfo;
import com.mygdx.holowyth.unit.statuseffect.SlowEffect;
import com.mygdx.holowyth.util.DataUtil;

/**
 * Simple stat fields are exposed public, while those which may trigger extra handling will be exposed through getters and setters
 * 
 * @author Colin Ta
 *
 */
public class UnitStats implements UnitStatsInfo {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public static boolean printDetailedCombatInfo = true;

	public final Unit self;

	// App fields
	EffectsHandler effects;

	// Info stats

	private String name;

	// Base stats
	public int maxHpBase, maxSpBase;
	/**
	 * Unused atm, UnitMotion just uses the default movespeed
	 */
	public float baseMoveSpeed;

	public int strBase, agiBase, fortBase, perceptBase;

	// test stats
	public static boolean useTestDamage = true;
	public int testDamage; // If set, the unit will simply do this much base damage instead of using stat and armor calculations
	public int testAtk;
	public int testDef;

	int expGives;

	// Progress stats
	int exp;

	float hp;
	float sp;

	// Calculated stats
	private int str, agi, fort, percep; // core stats
	private int maxHp, maxSp;
	private int atk, def, force, stab, acc, dodge;

	private int armor, armorPiercing;
	private float dmgReduction, armorNegation;

	// Helper Stats (for summing up stat contributions). 'i' stands for interim
	private int iStr, iAgi, iFort, iPercep; // core stats
	private int iAtk, iDef, iForce, iStab, iAcc, iDodge;
	private int iArmor, iArmorPiercing;
	private float iDmgReduction, iArmorNegation;

	private EquippedItems equip = new EquippedItems();

	public int level;

	// Status Effects

	private final List<SlowEffect> slowEffects = new LinkedList<SlowEffect>();

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

	public void tick() {
		tickEffects();
	}

	public void tickEffects() {

		slowEffects.forEach((effect) -> effect.tickDuration());
		slowEffects.removeIf((effect) -> effect.isExpired());

	}

	/**
	 * 
	 * Calculates correct calculated stats as well as movement speed, movementSpeed. The base stats should be modified and status effects set before
	 * calling this
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

		str = strBase + iStr;
		agi = agiBase + iAgi;
		fort = fortBase + iFort;
		percep = perceptBase + iPercep;

		// 2: calculate hp stats
		maxHp = maxHpBase; // Math.round(baseMaxHp * (1 + 0.1f * (fort - 5)));
		maxSp = maxSpBase;

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

		int levelBonus = (level) * 2;
		int mid = 0;

		atk = testAtk;
		def = testDef;

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
	 * Preps the unit for use in the game world by setting the transient fields to an appropriate starting value. Idempotent (can call >=1 times)
	 */
	public void prepareUnit() {
		recalculateStats();

		hp = maxHp;
		sp = maxSp;

		slowEffects.clear();
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
			logger.warn("Error, cannot attack self");
			return;
		}
		if (this.isDead()) {
			logger.warn("Error, self is dead");
			return;
		}
		if (enemy.isDead()) {
			logger.warn("Cannot attack a dead target");
			return;
		}

		// System.out.printf("%s attacks %s%n", this.name, enemy.name);

		float chanceToHit = 0;

		// // 1. Simulate dodge chance
		//
		// int acc = this.acc;
		// int dodge = enemy.dodge;
		//
		// chanceToHit = (float) (Math.pow(2, (acc - dodge) / 10f) * 0.5);
		// chanceToHit = Math.min(accChanceCeiling, chanceToHit);
		// chanceToHit = Math.max(accChanceFloor, chanceToHit);
		// if (printDetailedCombatInfo)
		// System.out.printf(" -Chance to be accurate: %f %d relative acc %n", chanceToHit, acc - dodge);
		//
		// if (Math.random() > chanceToHit) {
		// // System.out.printf("%s's attack missed %s %n", this.name, enemy.name);
		// effects.makeMissEffect(this.self);
		// return;
		// }

		// Add a slow effect, regardless of block or hit. This is for balancing fleeing enemies without engaging.
		enemy.applySlow(0.4f, 90);
		enemy.applySlow(0.2f, 150);

		// 2. Simulate block chance

		int atk = this.atk + getMultiTeamingAtkBonus(enemy);
		int def = enemy.def;

		chanceToHit = (float) (0.4 * (1 + (atk - def) * 0.05));

		chanceToHit = Math.min(atkChanceCeiling, chanceToHit);
		chanceToHit = Math.max(atkChanceFloor, chanceToHit);
		if (printDetailedCombatInfo) {
			logger.debug("{} attacks {}: relative attack is {} (+{} from multi-teaming)", this.name, enemy.name, atk - def,
					getMultiTeamingAtkBonus(enemy));
			System.out.printf(" -Chance to land hit: %s %d relative acc %n", getRoundedString(chanceToHit), atk - def);
		}

		if (Math.random() > chanceToHit) {
			// System.out.printf("%s's attack was blocked by %s %n", this.name, enemy.name);
			effects.makeBlockEffect(this.self, enemy.self);
			return;
		}

		float origDamage;

		if (useTestDamage && testDamage != 0) {
			origDamage = testDamage;
		} else {
			origDamage = getUnitDamage();
		}

		// 3. Calculate damage and reduction from armor
		float damageReduced = Math.max(enemy.armor - armorPiercing, origDamage * enemy.dmgReduction);
		damageReduced *= (1 - armorNegation);

		float damage = origDamage - damageReduced;

		if (damage < 0) {
			damage = 0;
		}

		// 5. Apply damage
		System.out.printf("%s's attack hit and did %s damage to %s%n", this.name, DataUtil.getRoundedString(damage), enemy.name);

		if (enemy.applyDamage(damage) > 0) {
			enemy.self.interrupt();
		}

	}

	private int getMultiTeamingAtkBonus(UnitStats target) {
		int numAttackers = target.self.getAttackers().size();

		switch (numAttackers) {
		case 1:
			return 0;
		case 2:
			return 5;
		case 3:
			return 8;
		case 4:
			return 10;
		default:
			if (numAttackers >= 5) {
				return 12;
			} else {
				return 0;
			}
		}

	}

	// private int getFlankingAtkBonus(UnitStats target) {
	// if (target.isBeingFlanked()) {
	// return 4;
	// } else {
	// return 0;
	// }
	// }
	//
	// private static final float flankingRequiredAngle = 130;
	//
	// private boolean isBeingFlanked() {
	//
	// if (self.getAttackers().size() < 2) {
	// return false;
	// }
	//
	// ArrayList<Pair<Unit, Float>> angles = new ArrayList<Pair<Unit, Float>>();
	// for (Unit u : self.getAttackers()) {
	// angles.add(new Pair<Unit, Float>(u, Point.getAngleInDegrees(self.getPos(), u.getPos())));
	// }
	//
	// // Brute force through values looking for two angles that differ by flanking
	//
	// // Size of array is at least 2 because of condition above
	// for (int i = 0; i < angles.size() - 1; i++) {
	// for (int j = i + 1; j < angles.size(); j++) {
	// float angleUnit1 = angles.get(i).second();
	// float angleUnit2 = angles.get(j).second();
	// if (Math.abs(angleUnit1 - angleUnit2) >= flankingRequiredAngle) {
	// // String name1 = angles.get(i).first().stats.name;
	// // String name2 = angles.get(j).first().stats.name;
	// // System.out.printf("Unit %s is being flanked by %s and %s%n", this.name, name1, name2);
	// return true;
	// }
	// }
	// }
	//
	// return false;
	// }

	public float getUnitDamage() {
		float weaponDamage = isWieldingAWeapon() ? equip.mainHand.damage : 1;
		float strengthBonus = (this.str - 5) * 0.1f;

		return weaponDamage * (1 + strengthBonus);
	}

	/**
	 * Will always apply a damage effect, refactor if you want it different.
	 * 
	 * @param damage
	 * @return The amount of damage actually done
	 */
	public float applyDamage(float damage) {
		effects.makeDamageEffect(damage, this.self);

		hp -= damage;

		if (hp <= 0) {
			hp = 0;
			self.unitDies();
		}

		return damage;
	}

	public void addSp(float amount) {
		sp = Math.min(sp + amount, maxSp);
	}

	public void subtractSp(float amount) {
		sp = Math.max(sp - amount, 0);
	}

	public void setSp(float value) {
		sp = Math.min(Math.max(value, 0), maxSp);
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Should only use this for debugging purposes. In-game wise you should use deal applyDamage
	 * 
	 * @param value
	 */
	public void setHpDebug(float value) {
		hp = value;
	}

	/**
	 * @param slowAmount
	 *            from 0 to 1, 1 being a total slow
	 * @param duration
	 *            in frames
	 */
	public void applySlow(float slowAmount, int duration) {
		slowEffects.add(new SlowEffect(duration, slowAmount));
	}

	// Printing Info Methods

	public void printInfo() {
		System.out.println(getInfo());
	}

	public String getInfo() {
		return getInfo(false);
	}

	public String getInfo(boolean includeEquipmentInfo) {
		recalculateStats();

		String s = "";
		s += String.format("Unit [%s]  hp: %s/%d  sp: %s/%d  <level %d>%n", name, getRoundedString(hp), maxHp, getRoundedString(sp), maxSp,
				level);
		// s += String.format("Core stats: STR %d, AGI %d, FORT %d, PERCEP %d%n", str, agi, fort, percep);
		s += String.format("Derived stats: Atk %d, Def %d%n", atk, def);
		// s += String.format("Derived stats: Atk %d, Def %d, Force %d, Stab %d, Acc %d, Dodge %d%n", atk, def, force,
		// stab, acc, dodge);
		/*
		 * s += "Other stats: \n"; s += String.format(" -Damage %s, AP %d, Armor Negation %s %n", getRoundedString(getUnitAttackDamage()),
		 * armorPiercing, getAsPercentage(armorNegation)); s += String.format(" -Armor %d, DR %s %n", armor, getAsPercentage(dmgReduction));
		 */

		if (includeEquipmentInfo) {
			s += "Equipped Items:\n";
			s += getEquippedItemsAsString();

			s += "Item details:\n";

			s += getInfoForAllEquippedItems();
		}
		return s;
	}

	private String getEquippedItemsAsString() {
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

	private String getInfoForAllEquippedItems() {
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
		 * @return A list of the items in each field, in order. Each index corresponds to an equip slot, so null is a valid value
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
		 * Like the fields, null means no item equipped. Some items, namely 2h weapons will appear in both hand slots <br>
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

	@Override
	public float getHp() {
		return hp;
	}

	@Override
	public float getSp() {
		return sp;
	}

	@Override
	public int getMaxHp() {
		return maxHp;
	}

	@Override
	public int getMaxSp() {
		return maxSp;
	}

	@Override
	public float getHpRatio() {
		return hp / maxHp;
	}

	@Override
	public float getSpRatio() {
		if (maxSp == 0) {
			return 0;
		}
		return sp / maxSp;
	}

	@Override
	public int getBaseMaxHp() {
		return maxHpBase;
	}

	@Override
	public int getBaseMaxSp() {
		return maxSpBase;
	}

	@Override
	public float getBaseMoveSpeed() {
		return baseMoveSpeed;
	}

	@Override
	public float getMoveSpeed() {

		float largestSlow = 0;
		for (var effect : slowEffects) {
			largestSlow = Math.max(largestSlow, effect.getSlowAmount());
		}

		return baseMoveSpeed * (1 - largestSlow);
	}

	/**
	 * @returns movespeed / baseMoveSpeed. For example if a unit was slowed by 30%, it would return 0.7
	 */
	public float getMoveSpeedRatio() {
		if (baseMoveSpeed == 0) {
			return 0;
		}

		return getMoveSpeed() / baseMoveSpeed;
	}

	@Override
	public int getExp() {
		return exp;
	}

	@Override
	public int getExpGives() {
		return expGives;
	}

	@Override
	public int getAtk() {
		return atk;
	}

	@Override
	public int getDef() {
		return def;
	}

	@Override
	public int getForce() {
		return force;
	}

	@Override
	public int getStab() {
		return stab;
	}

	@Override
	public int getAcc() {
		return acc;
	}

	@Override
	public int getDodge() {
		return dodge;
	}

	@Override
	public int getStr() {
		return strBase;
	}

	@Override
	public int getAgi() {
		return agiBase;
	}

	@Override
	public int getFort() {
		return fortBase;
	}

	@Override
	public int getPercep() {
		return perceptBase;
	}

	@Override
	public String getName() {
		return name;
	}
}
