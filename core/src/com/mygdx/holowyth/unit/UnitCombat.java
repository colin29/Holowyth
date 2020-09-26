package com.mygdx.holowyth.unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.gameScreen.MapInstance;
import com.mygdx.holowyth.unit.UnitOrders.Order;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * Handles the unit's attacking and retreating logic
 * Map life-time
 * @author Colin
 *
 */
class UnitCombat {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Unit self;
	private final UnitStats stats;
	
	
	
	// Attacking
	/** The unit this unit is attacking. Attacking a unit <--> being engaged. */
	private Unit attacking;

	private float attackCooldown = 60;
	private float attackCooldownRemaining = 0;

	/** Time in frames. When a unit engages it cannot retreat for a certain amount of time. */
	private float retreatCooldown = 0;
	private float retreatCooldownRemaining = 0;
	
	/**
	 * For a short time when a unit starts retreating they can't be given any other commands.
	 */
	static final int retreatDuration = 50;
	private int retreatDurationRemaining;
	
	
	private float attackOfOpportunityCooldown = 120;
	private float attackOfOpportunityCooldownRemaining = 0;
	
	public UnitCombat(Unit self) {
		this.self = self;
		stats = self.stats;
	}


	/**
	 * Updates attacking units
	 */
	void tick() {
		if (self.isDead())
			return;

		if (!self.isCastingOrChanneling()) {
			attackCooldownRemaining = Math.max(0, attackCooldownRemaining - 1); 
		}

		if (isAttacking()) {
			if (attacking.stats.isDead()) {
				stopAttacking();
				return;
			}
			if (attackCooldownRemaining <= 0) {
				stats.attack(attacking.stats);

				// Units automatically retaliate if they are idle
				if (attacking.getOrder() == Order.NONE && !attacking.isAttacking()
						&& attacking.isAttackOrderAllowed()) {
					attacking.orderAttackUnit(self, false);
				}
				attackCooldownRemaining = attackCooldown / stats.getMultiTeamingAtkspdPenalty(attacking);
			}
		}
		
		tickRetreatDurationIfRetreating();
		tickRetreatCooldown();
		tickAttackOfOpportunityCooldown();
	}
	
	void retreat(float x, float y) {
		retreatDurationRemaining = retreatDuration;
		if (self.getMotion().pathFindTowardsPoint(x, y)) {
			stopAttacking();
			self.orders.setOrder(Order.RETREAT);
			self.stats.removeAllBasicAttackSlows();

			var attackers = self.getUnitsAttackingThis();
			for (Unit attacker : attackers) {
				attacker.stats.attackOfOpportunity(self.stats);
				attacker.combat.attackOfOpportunityCooldownRemaining = attacker.combat.attackOfOpportunityCooldown;
			}
		}
	}
	
	private void tickRetreatDurationIfRetreating() {
		if (self.orders.getOrder() == Order.RETREAT)
			retreatDurationRemaining -= 1;
	}
	private void tickRetreatCooldown() {
		if (retreatCooldownRemaining > 0) {
			retreatCooldownRemaining -= 1;
		}
	}

	private void tickAttackOfOpportunityCooldown() {
		if (attackOfOpportunityCooldownRemaining > 0) {
			attackOfOpportunityCooldownRemaining -= 1;
		}
	}
	
	/**
	 * Lowest level method for attacking. Fine to call while attacking, though will warn you if you are
	 * already attacking the same target.
	 */
	void startAttacking(Unit target) {
		logger.debug("Unit {} started attacking {}", self.getName(), target.getName());
		if (isAttacking(target)) {
			logger.warn("Unit is already attacking the target");
			return;
		}
		if (isAttacking()) {
			stopAttacking();
		}

		self.getMotion().stopCurrentMovement();
		attacking = target;
		self.getMapInstanceMutable().onUnitStartsAttacking(self, attacking);

		// Attack cooldown may be artificially higher because of a recent stun/reel
		attackCooldownRemaining = Math.max(attackCooldownRemaining, attackCooldown / 4);
		retreatCooldownRemaining = retreatCooldown;
	}

	/**
	 * Disengages a unit.
	 */
	void stopAttacking() {
		if (isAttacking()) {
			self.getMapInstanceMutable().onUnitStopsAttacking(self, attacking);
			attacking = null;
		}
	}

	void addAttackCooldownRemaining(float value) {
		attackCooldownRemaining += value;
	}

	float getAttackCooldown() {
		return attackCooldown;
	}

	float getAttackCooldownRemaining() {
		return attackCooldownRemaining;
	}

	void setAttackCooldownRemaining(float attackCooldownRemaining) {
		this.attackCooldownRemaining = attackCooldownRemaining;
	}

	float getRetreatCooldownRemaining() {
		return retreatCooldownRemaining;
	}

	int getRetreatDurationRemaining() {
		return retreatDurationRemaining;
	}

	float getAttackOfOpportunityCooldownRemaining() {
		return attackOfOpportunityCooldownRemaining;
	}

	Unit getAttacking() {
		return attacking;
	}

	boolean isAttacking() {
		return attacking != null;
	}
	boolean isAttacking(UnitInfo target) {
		return attacking == target;
	}
	/**
	 * Will do nothing if the two units are outside of engage range. 
	 */
	void setAttacking(Unit unit) {
		float distToEnemy = Point.calcDistance(self.getPos(), unit.getPos());
		if (distToEnemy >= self.orders.getDisengageRange(unit)) {
			logger.info("Tried to set attacking, but unit out of range");
			return;
		}
		if (attacking != unit)
			startAttacking(unit);
	}


}
