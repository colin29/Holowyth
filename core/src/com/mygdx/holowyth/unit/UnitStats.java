package com.mygdx.holowyth.unit;

import static com.mygdx.holowyth.util.DataUtil.getRoundedString;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.unit.Unit.Order;
import com.mygdx.holowyth.unit.UnitEquip.Slot;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.interfaces.UnitStatsInfo;
import com.mygdx.holowyth.unit.item.Equip;
import com.mygdx.holowyth.unit.statuseffect.BasicAttackSlowEffect;
import com.mygdx.holowyth.unit.statuseffect.SlowEffect;
import com.mygdx.holowyth.util.DataUtil;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * Simple stat fields are exposed public, while those which may trigger extra handling will be exposed through getters and setters
 * 
 * @author Colin Ta
 *
 */
public class UnitStats implements UnitStatsInfo {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	public static boolean logBasicAttackInfo = false;

	// Combat parameters
	private static float atkChanceFloor = 0.01f;
	private static float atkChanceCeiling = 1f;

	private static float defaultStunProration = 60; // means a duration of x gets prorated around a max of x+60 frames
	private static float defaultReelProration = 120;
	
	// Unit-lifetime components
	public final Unit self;
	private final UnitStatCalculator calc;

	// General
	private String name = "DefaultName";
	
	// Stats
	float hp;
	float sp;
	public int level;
	/** A character's base stat values, before equipment and skill bonuses */
	public final UnitStatValues base = new UnitStatValues();
	/** Unused atm, UnitMotion just uses the default movespeed */
	public float baseMoveSpeed = Holo.defaultUnitMoveSpeed;  // Is this actually unused?

	
	// Map-lifetime components
	private EffectsHandler gfx;
	private UnitStun stun; 
	
	// Status effects
	private final List<SlowEffect> slowEffects = new LinkedList<SlowEffect>();
	private float blindDurationRemaining;

	private float tauntDurationRemaining = 0;
	private Unit tauntedTowards = null;
	
	public UnitStats(Unit unit) {
		this.self = unit;
		this.gfx = unit.getWorld().getGfx();

		stun = new UnitStun(unit);
		calc = new UnitStatCalculator(this);
	}

	public UnitStats(String name, Unit unit) {
		this(unit);
		if (name != null) {
			this.name = name;
		}
	}
	public void reinitializeForWorld() {
		gfx = self.getWorld().getGfx();
		stun = new UnitStun(self);
	}
	
	public void clearMapLifetimeData() {
		clearMapLifetimeComponents();
		clearStatusEffectsData();
	}
	private void clearMapLifetimeComponents() {
		gfx = null;
		stun = null;
	}
	private void clearStatusEffectsData() {
		slowEffects.clear();
		blindDurationRemaining = 0;

		tauntDurationRemaining = 0;
		tauntedTowards = null;
	}

	public void tick() {
		tickStatusEffects();
		stun.tick();
	}

	public void tickStatusEffects() {
		slowEffects.forEach((effect) -> effect.tickDuration());
		slowEffects.removeIf((effect) -> effect.isExpired());

		blindDurationRemaining = Math.max(0, blindDurationRemaining - 1);

		boolean wasTaunted = isTaunted();
		tauntDurationRemaining = Math.max(0, tauntDurationRemaining - 1);
		if (tauntDurationRemaining == 0) {
			tauntedTowards = null;
			if (wasTaunted)
				onTauntEnd();
		}

	}

	private void onTauntEnd() {
		self.clearOrder();
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
	}

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

		// Add a slow effect, regardless of block or hit. This is for balancing fleeing enemies without engaging.
		enemy.applyBasicAttackSlow(0.4f, 90);
		enemy.applyBasicAttackSlow(0.2f, 150);

		// 2. Simulate block chance

		if (!isAttackRollSuccessful(enemy, atkBonus, false)) {
			// System.out.printf("%s's attack was blocked by %s %n", this.name, enemy.name);
			gfx.makeBlockEffect(this.self, enemy.self);
			return;
		}

		// 3. Calculate damage and reduction from armor

		float damage = enemy.calculateDamageThroughArmor(getDamage(), getArmorPiercing(), getArmorNegation());

		// 5. Apply damage
		logger.trace("{}'s attack hit and did {} damage to {}", this.name, DataUtil.getRoundedString(damage), enemy.name);

		enemy.applyDamageIgnoringArmor(damage);
		if (damage > 0) {
			enemy.self.interruptNormal();
		}

	}

	public boolean isAttackRollSuccessful(UnitStats enemy, int atkBonus) {
		return isAttackRollSuccessful(enemy, atkBonus, true);
	}

	/**
	 * Does an attack roll on enemy and returns whether attack was successful or not
	 */
	public boolean isAttackRollSuccessful(UnitStats enemy, int atkBonus, boolean isSkill) {
		float chanceToHit;
		int atk = getAtk() + atkBonus + getMultiTeamingAtkBonus(enemy) - getReelAtkPenalty();
		int defEnemy = enemy.getDef() - enemy.getStunDefPenalty() - enemy.getReelDefPenalty();

		chanceToHit = (float) (0.4 * (1 + (atk - defEnemy) * 0.05));

		chanceToHit = Math.min(atkChanceCeiling, chanceToHit);
		chanceToHit = Math.max(atkChanceFloor, chanceToHit);

		if (isSkill) {
			logger.debug("Skill atk roll: {}->{} {} {} ({} relative attack)", this.name, enemy.name,
					Math.random() <= chanceToHit ? "SUCCESS" : "FAILURE",
					getRoundedString(chanceToHit), atk - defEnemy);
		} else {
			if (logBasicAttackInfo) {
				logger.debug("{} -> {} {}  ({} relative attack) (+{} from multi-teaming)", this.name, enemy.name,
						getRoundedString(chanceToHit), atk - defEnemy,
						getMultiTeamingAtkBonus(enemy));
			}
		}

		return Math.random() <= chanceToHit;
	}

	void attackOfOpportunity(UnitStats target) {
		logger.debug("{} did an attack of opportunity on {}", this.getName(), target.getName());
		attack(target, 10);
	}

	private int getMultiTeamingAtkBonus(UnitStats target) {
		int numAttackers = target.self.getUnitsAttackingThis().size();

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

	/**
	 * Note: uses the armor values of this unit
	 * 
	 * @return
	 */
	public float calculateDamageThroughArmor(float damage, int atkerArmorPiercing, float atkerArmorNegation) {
		float damageMitgated = Math.max(getArmor() - atkerArmorPiercing, damage * getPercentageArmor());
		damageMitgated *= (1 - atkerArmorNegation);

		float postArmorDamage = damage - damageMitgated;

		if (postArmorDamage < 0) {
			postArmorDamage = 0;
		}

		// logger.debug("Damage {} --> {}", damage, postArmorDamage);
		return postArmorDamage;
	}

	/**
	 * Applies damage with armor dmg reduction as normal
	 */
	public void applyDamage(float damage) {
		applyDamage(damage, false);
	}

	public void applyDamage(float damage, boolean useFastDamageEffect) {
		applyDamage(damage, 0, 0, useFastDamageEffect);
	}

	public void applyDamage(float damage, int atkerArmorPiercing, float atkerArmorNegation) {
		applyDamage(damage, atkerArmorPiercing, atkerArmorNegation, false);
	}

	public void applyDamage(float damage, int atkerArmorPiercing, float atkerArmorNegation, boolean useFastDamageEffect) {
		applyDamageIgnoringArmor(calculateDamageThroughArmor(damage, atkerArmorPiercing, atkerArmorNegation), useFastDamageEffect);
	}

	public void applyMagicDamage(float damage) {
		applyDamageIgnoringArmor(damage);
	}

	public void applyMagicDamage(float damage, boolean useFastDamageEffect) {
		applyDamageIgnoringArmor(damage, useFastDamageEffect);
	}

	public void applyDamageIgnoringArmor(float damage) {
		applyDamageIgnoringArmor(damage, false);
	}

	/**
	 * Applies the damage, doesn't do any damage reduction
	 * 
	 * @param damage
	 * @return
	 */
	public void applyDamageIgnoringArmor(float damage, boolean useFastDamageEffect) {
		gfx.makeDamageEffect(damage, self, useFastDamageEffect);

		hp -= damage;

		if (hp <= 0) {
			hp = 0;
			self.unitDies();
		}
	}

	/**
	 * Makes a stun roll against this unit, the result depends on the unit's stability plus a random roll
	 * 
	 * @param force
	 * @param stunDuration
	 * @return
	 */
	public void doStunRollAgainst(int force, float stunDuration) {
		doStunRollAgainst(force, stunDuration, stunDuration + defaultStunProration, false, null, 0);
	}

	/**
	 * 
	 * Use default stun proration
	 * 
	 * @param force
	 * @param stunDuration
	 * @param knockbackVel
	 * @param maxKnockbackVel
	 *            The max knockback speed the unit can be pushed, no matter how many consecutive knockbacks there are. <br>
	 *            Affects velocity proration
	 */
	public void doKnockBackRollAgainst(int force, float stunDuration, Vector2 knockbackVel, float maxKnockbackVel) {
		doStunRollAgainst(force, stunDuration, stunDuration + defaultStunProration, true, knockbackVel, maxKnockbackVel);
	}

	public void doKnockBackRollAgainst(int force, float stunDuration, Vector2 knockbackVel) {
		doStunRollAgainst(force, stunDuration, stunDuration + defaultStunProration, true, knockbackVel, knockbackVel.len() + 1);
	}

	/**
	 * Even if the stun is partially resisted, the max stun duration remains original -- thus successive stuns can add up
	 */
	private void doStunRollAgainst(int force, float stunDuration, float maxStunDuration, boolean isKnockback, Vector2 knockbackVel,
			float maxKnockbackVel) {

		int attackerRoll = RandomUtils.nextInt(1, 7); // 1d6

		int attackerResult = force + attackerRoll - getStab();

		logger.debug("{} roll made against {}. Attacker Result: {} ({} + ({}) - {})", isKnockback ? "knockback" : "stun",
				getName(), attackerResult, force, attackerRoll, getStab());

		if (attackerResult >= 10) { // apply full effect
			if (isKnockback) {
				applyKnockbackStun(stunDuration, maxStunDuration, knockbackVel, maxKnockbackVel);
			} else {
				applyStun(stunDuration, maxStunDuration);
			}
		} else if (attackerResult > 0) { // reduced stun and/or knockback
			float factor = attackerResult * 0.1f;
			if (isKnockback) {
				applyKnockbackStun(stunDuration * factor, maxStunDuration, knockbackVel.scl(factor), maxKnockbackVel);
			} else {
				applyStun(stunDuration * factor, maxStunDuration);
			}
		} else if (attackerResult > -10) {
			applyReel(stunDuration * (attackerResult + 10) * 0.1f, stunDuration + defaultReelProration);
		}

	}

	public void doReelRollAgainst(int force, float reelDuration) {
		doReelRollAgainst(force, reelDuration, reelDuration + defaultReelProration);
	}

	public void doReelRollAgainst(int force, float reelDuration, float maxReelDuration) {

		int attackerRoll = RandomUtils.nextInt(1, 7); // 1d6

		int attackerResult = force + attackerRoll - getStab();

		logger.debug("Reel roll made against {}. Attacker Result: {} ({} + ({}) - {})",
				getName(), attackerResult, force, attackerRoll, getStab());

		if (attackerResult >= 10) { // apply full effect
			applyReel(reelDuration, maxReelDuration);
		} else if (attackerResult > 0) { // reduced stun and/or knockback
			float factor = (attackerResult + 10) * 0.1f;
			applyReel(reelDuration * factor, maxReelDuration);
		} else {
			// resisted
		}

	}

	public void applyStun(float duration) {
		stun.applyStun(duration, duration + 60); // thus a 0.5 sec stun will prorate around a 1.5sec max, 3sec -> 4 sec max
	}

	public void applyStun(float duration, float maxStunDuration) {
		stun.applyStun(duration, maxStunDuration);
	}

	/**
	 * Unlike doKnockbackRoll, this method has no concept of force/stability
	 * 
	 * @param dv
	 */
	public void applyKnockbackStun(float duration, float maxStunDuration, Vector2 dv) {
		applyKnockbackStun(duration, maxStunDuration, dv, dv.len() + 1);
	}

	/**
	 * 
	 * @param duration
	 * @param maxStunDuration
	 *            stun contribution by this effect is prorated around this value
	 * @param dv
	 * @param maxKnockbackVel
	 *            velocity contribution by this effect is prorated around this value
	 */
	public void applyKnockbackStun(float duration, float maxStunDuration, Vector2 dv, float maxKnockbackVel) {
		stun.applyKnockbackStun(duration, dv, maxKnockbackVel, maxStunDuration);
	}

	/**
	 * Apply a knockback stun without adding any minimum stun time
	 * 
	 * @param dv
	 */
	public void applyKnockbackStunWithoutVelProrate(Vector2 dv) {
		applyKnockbackStunWithoutVelProrate(0, dv);
	}

	/**
	 * Use default stun proration
	 */
	public void applyKnockbackStunWithoutVelProrate(float duration, Vector2 dv) {
		applyKnockbackStunWithoutVelProrate(duration, duration + defaultStunProration, dv);
	}

	public void applyKnockbackStunWithoutVelProrate(float duration, float maxStunDuration, Vector2 dv) {
		stun.applyKnockbackStunWithoutVelProrate(duration, maxStunDuration, dv);
	}

	public void applyBlind(float duration) {

		if (duration < 0) {
			throw new HoloIllegalArgumentsException("duration must be non-negative");
		}
		if (!isBlinded()) {
			self.interruptRangedSkills();
			blindDurationRemaining = duration;
		} else {
			blindDurationRemaining += duration;
		}

	}

	@Override
	public boolean isStunned() {
		return stun.isStunned();
	}

	@Override
	public float getStunDurationRemaining() {
		return stun.getStunDurationRemaining();
	}

	public void applyReel(float duration) {
		applyReel(duration, duration + defaultReelProration);
	}

	public void applyReel(float duration, float maxReelDuration) {
		stun.applyReel(duration, maxReelDuration);
	}

	@Override
	public boolean isReeled() {
		return stun.isReeled();
	}

	@Override
	public float getReeledDurationRemaining() {
		return stun.getReelDurationRemaining();
	}

	@Override
	public boolean isSlowed() {
		return !slowEffects.isEmpty();
	}

	@Override
	public boolean isSlowedIgnoringBasicAttackSlow() {
		for (var slowEffect : slowEffects) {
			if (!(slowEffect instanceof BasicAttackSlowEffect))
				return true;
		}
		return false;
	}

	@Override
	public boolean isBlinded() {
		return blindDurationRemaining > 0;
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
		if (self.order != Order.RETREAT) { // units are unaffected by basic attack slow while retreating
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

	public void printInfo(boolean includeEquipmentInfo) {
		System.out.println(getInfo(includeEquipmentInfo));
	}

	public String getInfo() {
		return getInfo(false);
	}

	public String getInfo(boolean includeEquipmentInfo) {
		recalculateStats();

		String s = "";
		s += String.format("Unit [%s]  hp: %s/%d  sp: %s/%d  <level %d>%n", name, getRoundedString(hp), getMaxHp(), getRoundedString(sp), getMaxSp(),
				level);
		// s += String.format("Core stats: STR %d, AGI %d, FORT %d, PERCEP %d%n", getStr(), getAgi(), getFort(), getPercep());
		s += String.format("Derived stats: Atk %d | Def %d | Force %d | Stab %d%n", getAtk(), getDef(), getForce(),
				getStab());
		s += String.format(" -Damage: %s%n", getDamage());
		// s += "Other stats: \n";
		// s += String.format(" -AP %d, Armor Negation %s %n", getArmorPiercing(), DataUtil.getAsPercentage(getArmorNegation()));
		//
		// s += String.format(" -Armor %d + %s %n", getArmor(), DataUtil.getAsPercentage(getPercentageArmor()));

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

		final var equip = self.equip;

		for (Slot slot : UnitEquip.Slot.values()) {
			Equip item = equip.getEquip(slot);
			if (item != null) {
				s += " -" + slot.getName() + ": " + item.name + "\n";
			} else {
				s += " -" + slot.getName() + ": [None]\n";
			}
		}

		return s;
	}

	private String getInfoForAllEquippedItems() {
		// Get a list of all different named items

		List<Equip> distinctItems = new ArrayList<Equip>();

		final var equip = self.equip;
		for (Equip item : equip.getEquipSlots().values()) {
			if (item == null)
				continue;
			if (distinctItems.stream().noneMatch(i -> i.name == item.name)) {
				distinctItems.add(item);
			}
		}

		String s = "";
		for (Equip item : distinctItems) {
			s += item.getInfo();
		}
		return s;
	}

	public boolean isDead() {
		return hp <= 0;
	}

	public UnitEquip getEquip() {
		return self.equip;
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
		return base.maxHp;
	}

	@Override
	public int getBaseMaxSp() {
		return base.maxSp;
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
	public float getDamage() {
		return calc.getDamage();
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
	public float getPercentageArmor() {
		return calc.getPercentageArmor();
	}

	@Override
	public int getArmorPiercing() {
		return calc.getArmorPiercing();
	}

	@Override
	public float getArmorNegation() {
		return calc.getArmorNegation();
	}

	@Override
	public UnitStatValues getBaseStats() {
		try {
			return (UnitStatValues) base.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public UnitStatValues getEquipBonuses() {
		try {
			return (UnitStatValues) calc.equipBonus.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public UnitStatValues getSkillBonuses() {
		try {
			return (UnitStatValues) calc.skillBonus.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public void applyTaunt(int duration, Unit tauntSource) {
		if (tauntSource == null) {
			logger.warn("tauntSource is null");
			return;
		}
		tauntDurationRemaining = duration;
		tauntedTowards = tauntSource;
	}

	@Override
	public float getTauntDurationRemaining() {
		return tauntDurationRemaining;
	}

	@Override
	public boolean isTaunted() {
		return tauntDurationRemaining > 0;
	}

	@Override
	public UnitInfo getTauntAttackTarget() {
		return tauntedTowards;
	}

	float getMultiTeamingAtkspdPenalty(Unit target) {
		int n = self.getUnitsAttackingThis().size();

		if (n <= 1) {
			return 1;
		} else if (n == 2) {
			return 0.9f;
		} else if (n == 3) {
			return 0.85f;
		} else { // 4 or more
			return 0.82f;
		}
	}


}
