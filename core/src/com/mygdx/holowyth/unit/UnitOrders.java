package com.mygdx.holowyth.unit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.skill.skill.GroundSkill;
import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.skill.skill.UnitSkill;
import com.mygdx.holowyth.unit.Unit.JobClass;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.unit.interfaces.UnitStatusInfo;
import com.mygdx.holowyth.unit.item.Equip.WeaponType;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * Handles unit ordering logic
 * 
 */
public class UnitOrders {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Unit self;
	private final UnitStats stats;
	private final UnitStatusInfo status;

	final UnitOrdersDeferring deffering;

	private Order order = Order.NONE;

	/**
	 * Units can be ordered to use melee skills while still at range. The unit follows a hard-attack
	 * command as usual, and carries out the skill when it engages
	 */
	private @Nullable NoneSkill queuedMeleeSkill;

	/**
	 * If you cast a ground skill while out of range the unit will try to walk in range and cast
	 */
	private @Nullable GroundSkill deferredGroundSkillMoveInRange;
	private float deferredGroundSkillX, deferredGroundSkillY;
	private @Nullable UnitSkill deferredUnitSkillMoveInRange;

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
		MOVE, MOVE_TO_UNIT, ATTACKUNIT_HARD, ATTACKUNIT_SOFT, NONE,
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
		
		// Default unit behaviour
		if(self.side == Side.ENEMY) {
			ifIdleAggroOntoNearbyTargets();
		}
		if(self.side == Side.PLAYER && stats.getHpRatio() > 0.3f) {
			if(self.getJobClass() != JobClass.PRIEST && self.getJobClass() != JobClass.MAGE) {
				if(!self.equip.hasWeaponTypeEquipped(WeaponType.BOW)) {
					ifIdleAggroOntoNearbyTargets();
				}
			}
		}
		
		if (status.isTaunted()) {
			if (order == Order.NONE) {
				if (isAnyOrderAllowedIgnoringTaunt())
					orderAttackUnit((UnitOrderable) status.getTauntAttackTarget(), true, true);
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

		ifInRangeCastDeferredGroundSkill();
		ifInRangeCastDeferredUnitSkill();

		startAttackingIfInRangeForAtkAndAtkMove();
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

	void orderMoveInRangeToUseSkill(float x, float y, @NonNull GroundSkill skill) {
		if (!skill.usingMaxRange()) {
			logger.warn("orderMoveInRangeToUseSkill: skill doesn't use a max range");
			return;
		}
		if (!isMoveOrderAllowed()) {
			return;
		}
		if (self.motion.pathFindTowardsPoint(x, y)) {
			clearOrder();
			order = Order.MOVE;
			deferredGroundSkillMoveInRange = skill;
			deferredGroundSkillX = x; // save the cast point because the pathing dest can differ due to goal
										// substitution
			deferredGroundSkillY = y;
		}
	}

	/**
	 * This actually makes the unit follow the unit until ordered otherwise
	 * 
	 * @param unit
	 */
	void orderMoveToUnit(@NonNull UnitOrderable unit) {
		clearOrder();
		if (!isMoveOrderAllowed()) {
			return;
		}
		orderTarget = (Unit) unit;
		if (self.motion.pathFindTowardsTarget()) {
			order = Order.MOVE_TO_UNIT;
		} else {
			return;
		}
	}

	void orderMoveInRangeToUseSkill(@NonNull UnitOrderable target, @NonNull UnitSkill skill) {
		if (!skill.usingMaxRange()) {
			logger.warn("orderMoveInRangeToUseSkill: skill doesn't use a max range");
			return;
		}
		clearOrder();
		if (!isMoveOrderAllowed()) {
			return;
		}
		orderTarget = (Unit) target;
		if (self.motion.pathFindTowardsTarget()) {
			order = Order.MOVE_TO_UNIT;
			deferredUnitSkillMoveInRange = skill;
		} else {
			return;
		}
	}

	private void ifInRangeCastDeferredGroundSkill() {
		if (order == Order.MOVE && deferredGroundSkillMoveInRange != null) {
			@NonNull
			GroundSkill skill = deferredGroundSkillMoveInRange;
			Point ground = new Point(deferredGroundSkillX, deferredGroundSkillY);
			if (Point.dist(ground, self.getPos()) <= skill.getMaxRange()) {
				skill.pluginTargeting(self, ground.x, ground.y);
				orderUseSkill(skill);
			}
		}
	}

	private void ifInRangeCastDeferredUnitSkill() {
		if (order == Order.MOVE_TO_UNIT && deferredUnitSkillMoveInRange != null) {
			@NonNull
			UnitSkill skill = deferredUnitSkillMoveInRange;
			if (Point.dist(orderTarget.getPos(), self.getPos()) <= skill.getMaxRange()) {
				skill.setTargeting(self, orderTarget);
				orderUseSkill(skill);
			}
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

	boolean orderAttackUnitQueueMeleeSkill(UnitOrderable unitOrd, @NonNull NoneSkill skill) {
		if (self.combat.isAttacking()) {
			logger.warn("orderAttackUnitQueueMeleeSkill shouldn't be called while unit is already attacking");
			return false;
		}
		boolean result = orderAttackUnit(unitOrd, true);
		if (result) {
			queuedMeleeSkill = skill;
		}
		return result;
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
						(UnitOrderable) unitOrd, 0, 0);
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
	public boolean isAttackOrderAllowed(UnitOrderable target) {
		return isGeneralOrderAllowed() && self.isEnemy(target);
	}

	public boolean isAttackOrderAllowed() {
		return isGeneralOrderAllowed();
	}

	public boolean isMoveOrderAllowed() {
		return (isGeneralOrderAllowed()
				|| (isAnyOrderAllowed() && !isBusyRetreating() && self.skills.isMobileChannelling())
						&& !self.isAttacking());
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
		queuedMeleeSkill = null;
		deferredGroundSkillMoveInRange = null;
		deferredGroundSkillX = 0;
		deferredGroundSkillY = 0;
		deferredUnitSkillMoveInRange = null;

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

				if (Unit.dist(self, closestEnemy) <= aggroRange) {
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

				if (Unit.dist(self, closestEnemy) <= aggroRange) {
					orderTarget = (Unit) closestEnemy; // manually set target/path, since we want to keep the ATTACKMOVE
														// order
					if (!self.motion.pathFindTowardsTarget()) {
						orderTarget = null; // keep walking normally if path to target not found
					}
				}
			}
		}
	}

	private void startAttackingIfInRangeForAtkAndAtkMove() {
		if (!self.isAttacking() && (order.isAttackUnit() || isAttackMoveAndHasTarget())) {
			float distToTarget = Unit.dist(self, orderTarget);
			if (distToTarget <= getEngageRange(orderTarget)) {
				self.combat.startAttacking(orderTarget);
				if (queuedMeleeSkill != null) {
					queuedMeleeSkill.pluginTargeting(self);
					self.orderUseSkill(queuedMeleeSkill);
				}
			}
		}
	}

	private void stopAttackingIfEnemyIsOutOfRange() {
		if (self.isAttacking()) {
			float distToEnemy = Unit.dist(self, self.getAttacking());
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
			var otherTargets = UnitUtil.getTargetsSortedByDistance(self, self.getMapInstance());
			otherTargets.remove(orderTarget);
			otherTargets
					.removeIf((t) -> Unit.dist(self, t) >= Holo.defaultAggroRange);
			
			final float distTarget = Unit.dist(self, orderTarget);
			
			float aggroRange = self.getSide() == Side.PLAYER ? Holo.alliedUnitsAggroRange : Holo.defaultAggroRange;
			float chaseRange = self.getSide() == Side.PLAYER ? Holo.alliedUnitsAttackChaseRange
					: Holo.defaultUnitAttackChaseRange;
			
			if(otherTargets.isEmpty()) {
				if(distTarget > chaseRange)
					clearOrder();
				return;
			}
			
			UnitOrderable closestOther = otherTargets.peek(); 
			final float distClosestOther = Unit.dist(self, closestOther);
			
			// Let's say switch if the closest enemy target is closer than 75% the distance

			if(distTarget > chaseRange) {
				if(distClosestOther<=aggroRange) {
					orderAttackUnit(closestOther, false);
				}else {
					clearOrder();
				}
			}else {
				if(distClosestOther < distTarget * 0.75) {
					orderAttackUnit(closestOther, false);// switch targets
				}
			}
		}
	}

	/**
	 * Preconditions: Target must be defined
	 */
	private void handleTargetLossAndSwitchingForAttackMove() {
		if (order == Order.ATTACKMOVE) {
			var otherTargets = UnitUtil.getTargetsSortedByDistance(self, self.getMapInstance());
			otherTargets.remove(orderTarget);
			otherTargets
					.removeIf((t) -> Unit.dist(self, t) >= Holo.defaultAggroRange);
			
			final float distTarget = Unit.dist(self, orderTarget);
			
			float aggroRange = self.getSide() == Side.PLAYER ? Holo.alliedUnitsAggroRange : Holo.defaultAggroRange;
			float chaseRange = self.getSide() == Side.PLAYER ? Holo.alliedUnitsAttackChaseRange
					: Holo.defaultUnitAttackChaseRange;
			
			if(otherTargets.isEmpty()) {
				if(distTarget > chaseRange)
					repathToDestinationForAttackMove();
				return;
			}
			
			UnitOrderable closestOther = otherTargets.peek(); 
			final float distClosestOther = Unit.dist(self, closestOther);
			
			// Switch if the closest enemy target is closer than 75% the distance

			if(distTarget > chaseRange) {
				if(distClosestOther<=aggroRange) {
					orderTarget = (Unit) closestOther;
				}else {
					repathToDestinationForAttackMove();
				}
			}else {
				if(distClosestOther < distTarget * 0.75) {
					orderTarget = (Unit) closestOther;
				}
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
		if (order != null) {
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
