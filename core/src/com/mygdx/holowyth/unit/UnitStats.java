package com.mygdx.holowyth.unit;

import static com.mygdx.holowyth.util.DataUtil.getRoundedString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.unit.Unit.Order;
import com.mygdx.holowyth.unit.interfaces.UnitStatsInfo;
import com.mygdx.holowyth.unit.statuseffect.BasicAttackSlowEffect;
import com.mygdx.holowyth.unit.statuseffect.SlowEffect;
import com.mygdx.holowyth.util.DataUtil;
import com.mygdx.holowyth.util.Holo;

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
	EffectsHandler gfx;

	// Sub-components:
	private final UnitStun stun;
	private final UnitStatCalculator calc;

	// Info stats

	private String name;

	// Base stats
	public int maxHpBase, maxSpBase;
	public int strBase, agiBase, fortBase, perceptBase;

	/**
	 * Unused atm, UnitMotion just uses the default movespeed
	 */
	public float baseMoveSpeed = Holo.defaultUnitMoveSpeed;

	// test stats
	public static boolean useTestDamage = true;
	public static boolean useTestAtkDef = true;
	public int testDamage; // If set, the unit will simply do this much base damage instead of using stat and armor calculations
	public int testAtk;
	public int testDef;

	int expGives;

	int exp;

	float hp;
	float sp;

	// Calculated stats
	// private int str, agi, fort, percep; // core stats
	// private int maxHp, maxSp;
	// private int atk, def, force, stab, acc, dodge;

	private int armor, armorPiercing;
	private float dmgReduction, armorNegation;

	private final EquippedItems equip = new EquippedItems();

	public int level;

	// Status Effects

	private final List<SlowEffect> slowEffects = new LinkedList<SlowEffect>();

	public enum UnitType { // Player-like characters have their derived stats calculated like players. Monsters do not.
		PLAYER, MONSTER
	}

	public UnitType unitType;

	public UnitStats(Unit unit) {
		this.self = unit;
		this.gfx = unit.getWorld().getEffectsHandler();

		stun = new UnitStun(unit);
		calc = new UnitStatCalculator(this);
	}

	public UnitStats(String name, Unit unit) {
		this(unit);
		this.name = name;
	}

	public void tick() {
		tickStatusEffects();
		stun.tick();
	}

	public void tickStatusEffects() {
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
		calc.recalculateStats();
	}

	/**
	 * Preps the unit for use in the game world by setting the transient fields to an appropriate starting value. Idempotent (can call >=1 times)
	 */
	public void prepareUnit() {
		recalculateStats();

		hp = getMaxHp();
		sp = getMaxSp();

		slowEffects.clear();

		printInfo();
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
		attack(enemy, 0);
	}

	private void attack(UnitStats enemy, int atkBonus) {
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

		// Add a slow effect, regardless of block or hit. This is for balancing fleeing enemies without engaging.
		enemy.applyBasicAttackSlow(0.4f, 90);
		enemy.applyBasicAttackSlow(0.2f, 150);

		// 2. Simulate block chance

		int atk = getAtk() + getMultiTeamingAtkBonus(enemy) + atkBonus - getReelAtkPenalty();
		int defEnemy = enemy.getDef() - enemy.getStunDefPenalty() - enemy.getReelDefPenalty();

		chanceToHit = (float) (0.4 * (1 + (atk - defEnemy) * 0.05));

		chanceToHit = Math.min(atkChanceCeiling, chanceToHit);
		chanceToHit = Math.max(atkChanceFloor, chanceToHit);

		if (printDetailedCombatInfo) {
			logger.debug("{} attacks {}", this.name, enemy.name);
			logger.debug("Hit chance: {} - {} relative attack (+{} from multi-teaming)", getRoundedString(chanceToHit), atk - defEnemy,
					getMultiTeamingAtkBonus(enemy));
			// System.out.printf(" -Chance to land hit: %s %n", getRoundedString(chanceToHit), atk - def);
		}

		if (Math.random() > chanceToHit) {
			// System.out.printf("%s's attack was blocked by %s %n", this.name, enemy.name);
			gfx.makeBlockEffect(this.self, enemy.self);
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
		logger.debug("{}'s attack hit and did {} damage to {}", this.name, DataUtil.getRoundedString(damage), enemy.name);

		if (enemy.applyDamage(damage) > 0) {
			enemy.self.interruptCastingAndChannelling();
		}

	}

	void attackOfOpportunity(UnitStats target) {
		logger.debug("{} did an attack of opportunity on {}", this.getName(), target.getName());
		attack(target, 10);
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
			return numAttackers >= 5 ? 12 : 0;
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

	private int getStunDefPenalty() {
		return stun.isStunned() ? 20 : 0;
	}

	private int getReelAtkPenalty() {
		return stun.isReeled() ? 10 : 0;
	}

	private int getReelDefPenalty() {
		return stun.isReeled() ? 10 : 0;
	}

	public float getUnitDamage() {
		float weaponDamage = isWieldingAWeapon() ? equip.mainHand.damage : 1;
		float strengthBonus = (getStr() - 5) * 0.1f;

		return weaponDamage * (1 + strengthBonus);
	}

	/**
	 * Will always apply a damage effect, refactor if you want it different.
	 * 
	 * @param damage
	 * @return The amount of damage actually done
	 */
	public float applyDamage(float damage) {

		gfx.makeDamageEffect(damage, self);

		hp -= damage;

		if (hp <= 0) {
			hp = 0;
			self.unitDies();
		}

		return damage;
	}

	/**
	 * Makes a stun roll against this unit, the result depends on the unit's stability plus a random roll
	 * 
	 * @param force
	 * @param stunDuration
	 * @return
	 */
	public void doStunRollAgainst(int force, float stunDuration) {
		doStunRollAgainst(force, stunDuration, false, null);
	}

	public void doKnockBackRollAgainst(int force, float stunDuration, Vector2 knockbackVel) {
		doStunRollAgainst(force, stunDuration, true, knockbackVel);
	}

	private void doStunRollAgainst(int force, float stunDuration, boolean isKnockback, Vector2 knockbackVel) {

		int stab = 10; // Set for 10 for testing purposes
		int attackerRoll = RandomUtils.nextInt(1, 7); // 1d6

		int attackerResult = force + attackerRoll - stab;

		logger.debug("{} roll made against {}. Attacker Result: {} ({} + ({}) - {})", isKnockback ? "knockback" : "stun",
				getName(), attackerResult, force, attackerRoll, stab);

		if (attackerResult >= 10) { // apply full effect
			if (isKnockback) {
				applyKnockbackStun(stunDuration, knockbackVel);
			} else {
				applyStun(stunDuration);
			}
		} else if (attackerResult > 0) { // reduced stun and/or knockback
			float factor = (attackerResult + 10) * 0.1f;
			if (isKnockback) {
				applyKnockbackStun(stunDuration * factor, knockbackVel.scl(factor));
			} else {
				applyStun(stunDuration * factor);
			}
		} else if (attackerResult > -10) {
			applyReel(stunDuration * (attackerResult + 20) * 0.1f);
		}

	}

	@Override
	public boolean isStunned() {
		return stun.isStunned();
	}

	public void applyStun(float duration) {
		stun.applyStun(duration);
	}

	public void applyKnockbackStun(float duration, Vector2 dv) {
		stun.applyKnockbackStun(duration, dv);
	}

	@Override
	public float getStunDurationRemaining() {
		return stun.getStunDurationRemaining();
	}

	public void applyReel(float duration) {
		stun.applyReel(duration);
	}

	@Override
	public boolean isReeled() {
		return stun.isReeled();
	}

	@Override
	public float getReeledDurationRemaining() {
		return stun.getReelDurationRemaining();
	}

	public void addSp(float amount) {
		sp = Math.min(sp + amount, getMaxSp());
	}

	public void subtractSp(float amount) {
		sp = Math.max(sp - amount, 0);
	}

	public void setSp(float value) {
		sp = Math.min(Math.max(value, 0), getMaxSp());
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Should only use this for debugging purposes. In-game wise you should use applyDamage() instead
	 */
	public void setHp(float value) {
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

	/**
	 * Same as applySlow but uses a marker sub-class so that certain maneuvers can nullify this slow.
	 */
	public void applyBasicAttackSlow(float slowAmount, int duration) {
		if (self.currentOrder != Order.RETREAT) { // units are unaffected by basic attack slow while retreating
			slowEffects.add(new BasicAttackSlowEffect(duration, slowAmount));
		}
	}

	public void removeAllBasicAttackSlows() {
		slowEffects.removeIf((effect) -> effect instanceof BasicAttackSlowEffect);
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
		s += String.format("Unit [%s]  hp: %s/%d  sp: %s/%d  <level %d>%n", name, getRoundedString(hp), getMaxHp(), getRoundedString(sp), getMaxSp(),
				level);
		s += String.format("Core stats: STR %d, AGI %d, FORT %d, PERCEP %d%n", getStr(), getAgi(), getFort(), getPercep());
		s += String.format("Derived stats: Atk %d, Def %d%n", getAtk(), getDef());
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
		return equip;
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
		return calc.getMaxHp();
	}

	@Override
	public int getMaxSp() {
		return calc.getMaxSp();
	}

	@Override
	public float getHpRatio() {
		return hp / getMaxHp();
	}

	@Override
	public float getSpRatio() {
		if (getMaxSp() == 0) {
			return 0;
		}
		return sp / getMaxSp();
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

	public final float REEL_SLOW_AMOUNT = 0.4f;

	@Override
	public float getMoveSpeed() {

		float largestSlow = 0;
		for (var effect : slowEffects) {
			largestSlow = Math.max(largestSlow, effect.getSlowAmount());
		}

		float reelingMoveSlow = isReeled() ? REEL_SLOW_AMOUNT : 0;

		return baseMoveSpeed * (1 - largestSlow) * (1 - reelingMoveSlow);
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
		return calc.getAtk();
	}

	@Override
	public int getDef() {
		return calc.getDef();
	}

	@Override
	public int getForce() {
		return calc.getForce();
	}

	@Override
	public int getStab() {
		return calc.getStab();
	}

	@Override
	public int getAcc() {
		return calc.getAcc();
	}

	@Override
	public int getDodge() {
		return calc.getDodge();
	}

	@Override
	public int getStr() {
		return calc.getStr();
	}

	@Override
	public int getAgi() {
		return calc.getAgi();
	}

	@Override
	public int getFort() {
		return calc.getFort();
	}

	@Override
	public int getPercep() {
		return calc.getPercep();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getArmor() {
		return calc.getArmor();
	}

	@Override
	public int getArmorPiercing() {
		return calc.getArmorPiercing();
	}

	@Override
	public float getDmgReduction() {
		return calc.getDmgReduction();
	}

	@Override
	public float getArmorNegation() {
		return calc.getArmorNegation();
	}

}
