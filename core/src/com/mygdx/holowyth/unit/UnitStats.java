package com.mygdx.holowyth.unit;

import static com.mygdx.holowyth.util.DataUtil.round;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.graphics.effects.EffectsHandler.DamageEffectParams;
import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.unit.UnitStats.DamageInstance;
import com.mygdx.holowyth.unit.interfaces.UnitStatsInfo;
import com.mygdx.holowyth.unit.item.Equip;
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
	public static boolean logBasicAttackInfo = false;

	// Combat parameters
	private static float atkChanceFloor = 0.01f;
	private static float atkChanceCeiling = 1f;

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
	private float baseMoveSpeed = Holo.defaultUnitMoveSpeed;  // Is this actually unused?

	
	// Map-lifetime components
	private EffectsHandler gfx;
	
	
	
	public UnitStats(Unit unit) {
		this.self = unit;
		this.gfx = unit.getMapInstance().getGfx();

		calc = new UnitStatCalculator(this);
	}

	public UnitStats(String name, Unit unit) {
		this(unit);
		if (name != null) {
			this.name = name;
		}
	}
	public void reinitializeForWorld() {
		gfx = self.getMapInstance().getGfx();
	}
	
	public void clearMapLifetimeData() {
		gfx = null;
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
		enemy.self.status.applyBasicAttackSlow(0.4f, 90);
		enemy.self.status.applyBasicAttackSlow(0.2f, 150);

		// 2. Simulate block chance

		if (!isAttackRollSuccessful(enemy, atkBonus, false)) {
			gfx.makeBlockEffect(this.self, enemy.self);
			// System.out.printf("%s's attack was blocked by %s %n", this.name, enemy.name);
			onAttack(enemy);
			return;
		}

		// 3. Calculate damage and reduction from armor

		float damage = enemy.calculatePostArmorDamage(getDamage(), getArmorPiercing(), getArmorNegation());

		// 5. Apply damage
		logger.trace("{}'s attack hit and did {} damage to {}", this.name, DataUtil.round(damage), enemy.name);

		enemy.applyExactDamage(damage, null);
		if (damage > 0) {
			enemy.self.interruptSoft();
		}
		onAttack(enemy);

	}

	//** Notify skills, etc. that trigger on attack
	private void onAttack(UnitStats enemy) {
		for(Skill s : self.skills.getSkills()) {
			s.onUnitAttack(self, enemy.self);
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
		
		final float randomRoll = (float) Math.random(); 
		
		if (isSkill) {
			logger.debug("Skill atk roll: {}->{} {} {} ({} relative attack)", this.name, enemy.name,
					randomRoll <= chanceToHit ? "SUCCESS" : "FAILURE",
					round(chanceToHit), atk - defEnemy);
		} else {
			if (logBasicAttackInfo) {
				logger.debug("{} -> {} {}  ({} relative attack) (+{} from multi-teaming)", this.name, enemy.name,
						round(chanceToHit), atk - defEnemy,
						getMultiTeamingAtkBonus(enemy));
			}
		}

		return randomRoll <= chanceToHit;
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
		return self.status.isStunned() ? 20 : 0;
	}

	private int getReelAtkPenalty() {
		return self.status.isReeled() ? 10 : 0;
	}

	private int getReelDefPenalty() {
		return self.status.isReeled() ? 10 : 0;
	}

	/**
	 * Note: uses the armor values of this unit
	 * 
	 * @return
	 */
	public float calculatePostArmorDamage(float damage, float atkerArmorPiercing, float atkerArmorNegation) {
		float damageMitgated = Math.max(getArmor() - atkerArmorPiercing, damage * getPercentageArmor());
		damageMitgated *= (1 - atkerArmorNegation);

		float postArmorDamage = damage - damageMitgated;

		if (postArmorDamage < 0) {
			postArmorDamage = 0;
		}

		// logger.debug("Damage {} --> {}", damage, postArmorDamage);
		return postArmorDamage;
	}

	public void applyHeal(float amount) {
		if(!isDead()) {
			hp += amount;
		}
	}
	
	private final DamageInstance temp = new DamageInstance();
	public void applyDamage(float damage) {
		temp.clear();
		temp.damage = damage;
		applyDamage(temp);
	}
	public void applyDamage(float damage, DamageEffectParams effectParams) {
		temp.clear();
		temp.damage = damage;
		applyDamage(temp, effectParams);
	}
	public void applyDamage(DamageInstance d) {
		processDamage(d, null);
	}
	public void applyDamage(DamageInstance d, DamageEffectParams effectParams) {
		processDamage(d, effectParams);
	}
	
	private void processDamage(DamageInstance d, DamageEffectParams effectParams) {
		applyExactDamage(calculatePostArmorDamage(d.damage, d.armorPiercing, d.armorNegation), effectParams);
	}

	/**
	 * Ignores all damage reduction, internal method
	 */
	private void applyExactDamage(float damage,  DamageEffectParams effectParams) {
		hp -= damage;
		if (hp <= 0) {
			hp = 0;
			self.unitDies();
		}
		if(effectParams == null) {
			gfx.makeDamageEffect(damage, self);
		}else {
			gfx.makeDamageEffect(damage, self, effectParams);
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
		doStunRollAgainst(force, stunDuration, stunDuration + UnitStatus.defaultStunProration, false, null, 0);
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
		doStunRollAgainst(force, stunDuration, stunDuration + UnitStatus.defaultStunProration, true, knockbackVel, maxKnockbackVel);
	}

	public void doKnockBackRollAgainst(int force, float stunDuration, Vector2 knockbackVel) {
		doStunRollAgainst(force, stunDuration, stunDuration + UnitStatus.defaultStunProration, true, knockbackVel, knockbackVel.len() + 1);
	}

	/**
	 * Even if the stun is partially resisted, the max stun duration remains original -- thus successive stuns can add up
	 */
	private void doStunRollAgainst(int force, float stunDuration, float maxStunDuration, boolean isKnockback, Vector2 knockbackVel,
			float maxKnockbackVel) {

		int attackerRoll = RandomUtils.nextInt(1, 7); // 1d6

		int attackerResult = force + attackerRoll - getStab();

		logger.debug("{} roll made against {}. Attacker Result: {} + ({}) - {} = {}", isKnockback ? "knockback" : "stun",
				getName(), force, attackerRoll, this.getStab(), attackerResult);

		if (attackerResult >= 10) { // apply full effect
			if (isKnockback) {
			self.status.applyKnockbackStun(stunDuration, maxStunDuration, knockbackVel, maxKnockbackVel);
			} else {
			self.status.applyStun(stunDuration, maxStunDuration);
			}
		} else if (attackerResult > 0) { // reduced stun and/or knockback
			float factor = attackerResult * 0.1f;
			if (isKnockback) {
			self.status.applyKnockbackStun(stunDuration * factor, maxStunDuration, knockbackVel.scl(factor), maxKnockbackVel);
			} else {
			self.status.applyStun(stunDuration * factor, maxStunDuration);
			}
		} else if (attackerResult > -10) {
		self.status.applyReel(stunDuration * (attackerResult + 10) * 0.1f, stunDuration + UnitStatus.defaultReelProration);
		}

	}

	public void doReelRollAgainst(int force, float reelDuration) {
		doReelRollAgainst(force, reelDuration, reelDuration + UnitStatus.defaultReelProration);
	}

	public void doReelRollAgainst(int force, float reelDuration, float maxReelDuration) {

		int attackerRoll = RandomUtils.nextInt(1, 7); // 1d6

		int attackerResult = force + attackerRoll - getStab();

		logger.debug("Reel roll made against {}. Attacker Result: {} ({} + ({}) - {})",
				getName(), attackerResult, force, attackerRoll, getStab());

		if (attackerResult >= 10) { // apply full effect
			self.status.applyReel(reelDuration, maxReelDuration);
		} else if (attackerResult > 0) { // reduced stun and/or knockback
			float factor = (attackerResult + 10) * 0.1f;
			self.status.applyReel(reelDuration * factor, maxReelDuration);
		} else {
			// resisted
		}

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
		s += String.format("Unit [%s]  hp: %s/%d  sp: %s/%d  <level %d>%n", name, round(hp), getMaxHp(), round(sp), getMaxSp(),
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

		for (WornEquips.Slot slotType : WornEquips.Slot.values()) {
			Equip item = equip.getEquip(slotType);
			s += " -" + slotType.getName() + ": " + item.name + "\n";
		}
		return s;
	}

	private String getInfoForAllEquippedItems() {
		// Get a list of all distinct items

		List<Equip> distinctItems = new ArrayList<Equip>();

		final var equip = self.equip;
		for (Equip item : equip.getEquipped().values()) {
			if (distinctItems.stream().noneMatch(i -> i==item)) {
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
	
	public enum DamageType{
		NORMAL, MAGIC
	}
	@NonNullByDefault
	public static class DamageInstance {
		float armorPiercing = 0;
		float armorNegation = 0;
		float damage = 0;
		DamageType type = DamageType.NORMAL;
		public DamageInstance() {
		}
		public DamageInstance(float damage) {
			this.damage = damage;
		}
		public void clear() {
			armorPiercing = 0;
			armorNegation = 0;
			damage = 0;
		}
	}
}
