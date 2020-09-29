package com.mygdx.holowyth.unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.unit.interfaces.UnitStatusInfo;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * Handles unit ordering logic
 */
public class UnitOrders {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Unit self;
	private final UnitStats stats;
	private final UnitStatusInfo status;
	
	final UnitOrdersDeferring deffering;
	
	private Order order = Order.NONE;
	/** Target for the current command */
	private Unit orderTarget;
	private float attackMoveDestX;
	private float attackMoveDestY;
	

	/**
	 * A unit's order represents whether it has any persistent order on it that would determine its
	 * behaviour. Note that a unit without a command could still be casting or attacking.
	 * 
	 * AttackUnit can be hard or soft. Hard will chase indefinitely, a soft attack order will stop
	 * chasing if the target goes out of range
	 */
	public enum Order {
		MOVE, ATTACKUNIT_HARD, ATTACKUNIT_SOFT, NONE,
		/**
		 * If an attacking moving unit has no target it's moving towards its destination. If it does have a
		 * target it's chasing that unit.
		 */
		ATTACKMOVE, RETREAT;

		public boolean isAttackUnit() {
			return this == ATTACKUNIT_HARD || this == ATTACKUNIT_SOFT;
		}
	}
	

	UnitOrders(Unit self) {
		this.self = self;
		stats = self.stats;
		status = self.status;
		deffering = new UnitOrdersDeferring(self, this);
	}

	/** Handles the complex logic revolving around switching orders and targets */
	void tick() {

		if (status.isTaunted()) {
			if (order == Order.NONE) {

				if (isAnyOrderAllowedIgnoringTaunt())
					orderAttackUnit((Unit) status.getTauntAttackTarget(), true, true);
			}
		}

		if (orderTarget != null && orderTarget.stats.isDead()) {
			if (order.isAttackUnit()) {
				clearOrder();
			}
			if (isAttackMoveAndHasTarget()) {
				orderTarget = null;
				repathToDestinationForAttackMove();
			}
		}

		if (order.isAttackUnit()) {
			handleTargetLossAndSwitchingForAttackUnitSoft();
		} else if (order == Order.ATTACKMOVE) {
			if (orderTarget == null) {
				aggroOntoNearbyTargetsForAttackMove();
			}
			if (orderTarget != null) {
				handleTargetLossAndSwitchingForAttackMove();
			}
		}

		startAttackingIfInRangeForAttackOrders();
		stopAttackingIfEnemyIsOutOfRange();
	}

	void orderMove(float x, float y) {
		if (!isMoveOrderAllowed()) {
			if (status.isStunned()) {
				deffering.tryToDeferOrder(Order.MOVE, null, x, y);
			}
			return;
		}
		if (self.motion.pathFindTowardsPoint(x, y)) {
			clearOrder();
			order = Order.MOVE;
		}
	}

	/**
	 * @return Whether the command was valid and accepted
	 */
	boolean orderAttackUnit(UnitOrderable unitOrd) {
		return orderAttackUnit(unitOrd, true);
	}

	/**
	 * 
	 * @param unitOrd
	 * @param isHardOrder A hard attack order makes the unit chase forever, whereas with a soft order
	 *                    the unit will stop chasing if out of range
	 * @return
	 */
	boolean orderAttackUnit(UnitOrderable unitOrd, boolean isHardOrder) {
		return orderAttackUnit(unitOrd, isHardOrder, false);
	}

	/**
	 * 
	 * @param unitOrd
	 * @param isHardOrder
	 * @param isATauntedOrder Whether this order is a special internal order that should ignore taunt
	 *                        restriction
	 * @return
	 */
	boolean orderAttackUnit(UnitOrderable unitOrd, boolean isHardOrder, boolean isATauntedOrder) {
		Unit unit = (Unit) unitOrd; // underlying objects must hold the same type

		if (unit == self) {
			logger.warn("Unit can't be ordered to attack itself");
			return false;
		}
		if (isATauntedOrder ? isAnyOrderAllowedIgnoringTaunt() : isAttackOrderAllowed(unit)) {
			if (status.isStunned()) {
				deffering.tryToDeferOrder(isHardOrder ? Order.ATTACKUNIT_HARD : Order.ATTACKUNIT_SOFT,
						(Unit) unitOrd, 0, 0);
			}
			if (self.combat.isAttacking())
				return orderAttackUnitWhileAlreadyAttacking(unitOrd, isHardOrder);

			clearOrder();
			this.order = isHardOrder ? Order.ATTACKUNIT_HARD : Order.ATTACKUNIT_SOFT;
			this.orderTarget = unit;

			self.motion.pathFindTowardsTarget();

			return true;
		}
		return false;
	}

	/**
	 * Precondition: Unit is currently attacking a unit
	 *
	 * Is a helper method, does not check order validity, should call orderAttackUnit instead.
	 * 
	 * Units can switch attacking targets if the new target is within melee range. If the unit is in
	 * range, switches targets immediately <br>
	 * If out of range, does nothing (unit needs to manually retreat first)
	 */
	private boolean orderAttackUnitWhileAlreadyAttacking(UnitOrderable unitOrd, boolean isHardOrder) {
		Unit target = (Unit) unitOrd;

		if (self.getAttacking() == null) {
			logger.warn("order orderAttackUnitWhileAlreadyAttacking called but unit is not attacking");
			return false;
		}
		if (target == self) {
			logger.warn("Unit can't be ordered to attack itself)");
			return false;
		}

		if (Point.dist(self.getPos(), target.getPos()) <= self.getRadius() + target.getRadius()
				+ Holo.defaultUnitSwitchEngageRange) {
			clearOrder();
			order = isHardOrder ? Order.ATTACKUNIT_HARD : Order.ATTACKUNIT_SOFT;
			orderTarget = target;
			self.combat.setAttacking(target);
			return true;
		}
		return false;
	}

	void orderAttackMove(float x, float y) {
		if (isAttackMoveOrderAllowed()) {
			if (self.motion.pathFindTowardsPoint(x, y)) {
				clearOrder();
				attackMoveDestX = x;
				attackMoveDestY = y;
				this.order = Order.ATTACKMOVE;
			}
		} else if (status.isStunned()) {
			deffering.tryToDeferOrder(Order.ATTACKMOVE, null, x, y);
		}

	}

	void orderRetreat(float x, float y) {
		if (isRetreatOrderAllowed()) {
			self.combat.retreat(x, y);
		} else if (status.isStunned()) {
			deffering.tryToDeferOrder(Order.RETREAT, null, x, y);
		}
	}

	/**
	 * A stop order stops a unit's motion and current order. You cannot use stop to cancel your own
	 * casting atm.
	 */
	void orderStop() {
		if (!isStopOrderAllowed()) {
			if (status.isStunned())
				deffering.clearDeferredOrder();
			return;
		}
		stopUnit();
	}

	/**
	 * @param skill The skill must have effects set already
	 */
	void orderUseSkill(ActiveSkill skill) {

		if (status.isStunned()) {
			deffering.clearDeferredOrder(); // deferring a skill order is not supported but it will still clear an
													// existing deferred order
		}

		if (!isUseSkillAllowed()) {
			return;
		}
		if (!skill.hasEnoughSp(self)) {
			return;
		}

		self.setActiveSkill(skill);
		skill.begin(self);
	}

	/**
	 * Stops a unit's motion and halts any current orders. It does not disengage a unit that is
	 * attacking, though.. Unlike orderStop, is not affected by order restrictions. As such, it should
	 * be for backend and not player use.
	 */
	void stopUnit() {
		clearOrder();
		self.motion.stopCurrentMovement();
	}

	// @formatter:off
	public boolean isAttackOrderAllowed(Unit target) {
		return isGeneralOrderAllowed() && self.isEnemy(target);
	}

	public boolean isAttackOrderAllowed() {
		return isGeneralOrderAllowed();
	}

	public boolean isMoveOrderAllowed() {
		return isGeneralOrderAllowed() && !self.isAttacking();
	}

	public boolean isAttackMoveOrderAllowed() {
		return isGeneralOrderAllowed() && !self.isAttacking();
	}

	public boolean isRetreatOrderAllowed() {
		return isAnyOrderAllowed() && self.isAttacking() && this.order != Order.RETREAT
				&& self.getRetreatCooldownRemaining() <= 0 && !(self.isCasting() || self.isChannelling());
	}

	public boolean isStopOrderAllowed() {
		return isGeneralOrderAllowed();
	}

	public boolean isUseSkillAllowed() {
		return isGeneralOrderAllowed() && !status.isBlinded();
	}

	/**
	 * Returns the conditions which are shared by most ordinary orders
	 * 
	 * @return
	 */
	public boolean isGeneralOrderAllowed() {
		return isAnyOrderAllowed() && !isBusyRetreating() && !(self.isCasting() || self.isChannelling());
	}

	/**
	 * Returns the conditions which are shared by all orders
	 * 
	 * @return
	 */
	public boolean isAnyOrderAllowed() {
		return isAnyOrderAllowedIgnoringTaunt() && !status.isTaunted();
	}

	public boolean isAnyOrderAllowedIgnoringTaunt() {
		return !stats.isDead() && !self.motion.isBeingKnockedBack() && !status.isStunned();
	}

	// @formatter:on

	public boolean isBusyRetreating() {
		return order == Order.RETREAT && self.combat.getRetreatDurationRemaining() > 0;
	}

	/**
	 * Clears any current order on this unit. For internal use.
	 */
	void clearOrder() {
		order = Order.NONE;
		orderTarget = null;

		attackMoveDestX = 0;
		attackMoveDestY = 0;
	}

	/**
	 * Probably will be used later by ai or player
	 */
	@SuppressWarnings("unused")
	private void ifIdleAggroOntoNearbyTargets() {
		if (self.isCompletelyIdle()) {
			var closestTargets = UnitUtil.getTargetsSortedByDistance(self, self.getMapInstance());
			if (!closestTargets.isEmpty()) {
				UnitOrderable closestEnemy = closestTargets.remove();

				float aggroRange = self.getSide() == Side.PLAYER ? Holo.alliedUnitsAggroRange : Holo.defaultAggroRange;

				if (Point.dist(self.getPos(), closestEnemy.getPos()) <= aggroRange) {
					orderAttackUnit(closestEnemy, false);
				}
			}
		}
	}

	private void aggroOntoNearbyTargetsForAttackMove() {
		if (order == Order.ATTACKMOVE) {
			var closestTargets = UnitUtil.getTargetsSortedByDistance(self, self.getMapInstance());
			if (!closestTargets.isEmpty()) {
				UnitOrderable closestEnemy = closestTargets.remove();

				float aggroRange = self.getSide() == Side.PLAYER ? Holo.alliedUnitsAggroRange : Holo.defaultAggroRange;

				if (Point.dist( self.getPos(), closestEnemy.getPos()) <= aggroRange) {
					orderTarget = (Unit) closestEnemy; // manually set target/path, since we want to keep the ATTACKMOVE
														// order
					if (!self.motion.pathFindTowardsTarget()) {
						orderTarget = null; // keep walking normally if path to target not found
					}
				}
			}
		}
	}

	private void startAttackingIfInRangeForAttackOrders() {
		if (!self.isAttacking() && (order.isAttackUnit() || isAttackMoveAndHasTarget())) {
			float distToTarget = Point.dist(self.getPos(), orderTarget.getPos());
			if (distToTarget <= getEngageRange(orderTarget)) {
				self.combat.startAttacking(orderTarget);
			}
		}
	}

	private void stopAttackingIfEnemyIsOutOfRange() {
		if (self.isAttacking()) {
			float distToEnemy = Point.dist(self.getPos(), self.getAttacking().getPos());
			if (distToEnemy >= getDisengageRange(self.getAttacking())) {
				self.combat.stopAttacking();
			}
		}
	}

	float getEngageRange(Unit unit) {
		return self.getRadius() + unit.getRadius() + Holo.defaultUnitEngageRange;
	}

	float getDisengageRange(Unit unit) {
		return self.getRadius() + unit.getRadius() + Holo.defaultUnitDisengageRange;
	}

	private boolean isAttackMoveAndHasTarget() {
		return order == Order.ATTACKMOVE && orderTarget != null;
	}

	/**
	 * Preconditions: Target must be defined
	 */
	private void handleTargetLossAndSwitchingForAttackUnitSoft() {
		if (order == Order.ATTACKUNIT_SOFT) {
			var otherTargetsWithinAggroRange = UnitUtil.getTargetsSortedByDistance(self, self.getMapInstance());
			otherTargetsWithinAggroRange
					.removeIf((t) -> Point.dist(self.getPos(), t.getPos()) >= Holo.defaultAggroRange);
			otherTargetsWithinAggroRange.remove(orderTarget);
			float distToTarget = Point.dist(self.getPos(), orderTarget.getPos());

			float aggroRange = self.getSide() == Side.PLAYER ? Holo.alliedUnitsAggroRange : Holo.defaultAggroRange;
			float chaseRange = self.getSide() == Side.PLAYER ? Holo.alliedUnitsAttackChaseRange
					: Holo.defaultUnitAttackChaseRange;

			if (distToTarget > aggroRange && !otherTargetsWithinAggroRange.isEmpty()) {
				orderAttackUnit(otherTargetsWithinAggroRange.peek(), false);
			} else if (distToTarget > chaseRange) {
				clearOrder();
			}
		}
	}

	/**
	 * Preconditions: Target must be defined
	 */
	private void handleTargetLossAndSwitchingForAttackMove() {
		if (order == Order.ATTACKMOVE) {
			var otherTargetsWithinAggroRange = UnitUtil.getTargetsSortedByDistance(self, self.getMapInstance());
			otherTargetsWithinAggroRange
					.removeIf((t) -> Point.dist(self.getPos(), t.getPos()) >= Holo.defaultAggroRange);
			otherTargetsWithinAggroRange.remove(orderTarget);
			float distToTarget = Point.dist(self.getPos(), orderTarget.getPos());

			float aggroRange = self.getSide() == Side.PLAYER ? Holo.alliedUnitsAggroRange : Holo.defaultAggroRange;
			float chaseRange = self.getSide() == Side.PLAYER ? Holo.alliedUnitsAttackChaseRange
					: Holo.defaultUnitAttackChaseRange;

			if (distToTarget > aggroRange && !otherTargetsWithinAggroRange.isEmpty()) {
				// Switch targets

				Unit oldTarget = orderTarget;

				orderTarget = (Unit) otherTargetsWithinAggroRange.peek();
				if (self.motion.pathFindTowardsTarget()) {
					orderTarget = oldTarget;
					logger.debug("Was attack moving but no path could be found to the nearest unit, ignoring");
				}

			} else if (distToTarget > chaseRange) {
				// Instead of clearing order, repath and resume moving towards the original destination
				repathToDestinationForAttackMove();
			}
		}
	}

	private void repathToDestinationForAttackMove() {
		orderTarget = null;
		if (!self.motion.pathFindTowardsPoint(attackMoveDestX, attackMoveDestY)) {
			logger.debug("Was attack moving but no path could be found to destination.");
			clearOrder();
		}
	}
	
	Order getOrder() {
		return order;
	}

	Unit getOrderTarget() {
		return orderTarget;
	}

	/**
	 * Clears the previous order first, if existed.
	 */
	void setOrder(Order order) {
		if(order!=null) {
			clearOrder();
		}
		this.order = order;
	}

	float getAttackMoveDestX() {
		return attackMoveDestX;
	}

	float getAttackMoveDestY() {
		return attackMoveDestY;
	}

}
