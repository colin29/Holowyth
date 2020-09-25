package com.mygdx.holowyth.unit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.ai.UnitAI;
import com.mygdx.holowyth.gameScreen.World;
import com.mygdx.holowyth.gameScreen.WorldInfo;
import com.mygdx.holowyth.gameScreen.basescreens.GameMapLoadingScreen;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.map.UnitMarker;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.UnitPF;
import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.skill.ActiveSkill.Status;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.unit.interfaces.UnitStatsInfo;
import com.mygdx.holowyth.unit.sprite.UnitGraphics;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.exceptions.HoloException;

/**
 * Responsibilities:<br>
 * <ol>
 * <li>Be the main class, delegate functionality to subcomponents such as UnitMotion, UnitStats</li>
 * <li>Be the implementer for interfaces, such as UnitInfo or UnitOrderable</li>
 * <li>Hold some basic unit information that isn't delegated to other subcomponents (hp, position, side)</li>
 * <li>Handle Order Logic (both at the moment of ordering and onFrame logic)</li>
 * </ol>
 *
 * @author Colin Ta
 * 
 */

/**
 * There are four aspects that largely determine a unit's state. They can be independent, but often
 * check each other for legality. <br>
 * For example, some skills might castable while attacking, and a unit could retain certain orders
 * while attacking.
 * 
 * <b>currentOrder</b> -- a unit with an order will continue trying to do something <br>
 * <b>attacking</b> -- a unit attacking is locked in combat and will regularly attack their target
 * <br>
 * <b>activeSkill</b> -- a unit with an active ability is either casting or channelling that ability
 * <br>
 * 
 * <b>motion.isBeingKnockedBack()</b> -- a unit being knocked back cannot be given new orders or
 * perform any action (it may retain its old order, depending on the type)
 */
public class Unit implements UnitPF, UnitInfo, UnitOrderable {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public float x, y;

	// World Fields
	private WorldInfo world;
	List<Unit> units;

	// Map life-time Components
	public final UnitMotion motion;

	// Unit life-time Components (persist at least partially through levels)
	public final UnitStats stats;
	public final UnitSkills skills;
	public final UnitGraphics graphics;
	public final UnitEquip equip;
	public final UnitAI ai;

	// Collision Detection
	private float radius = Holo.UNIT_RADIUS;

	// Id (for debug)
	private static int curId = 0;
	private final int id;

	/**
	 * A unit's friendly/foe status.
	 */
	Side side;

	// Ordering state
	public final UnitOrderDeferring orderDeferring;
	
	Order order = Order.NONE;
	/** Target for the current command */
	Unit orderTarget;
	float attackMoveDestX;
	float attackMoveDestY;

	// Combat state
	/** The unit this unit is attacking. Attacking a unit <--> being engaged. */
	private Unit attacking;


	// Skills
	/**
	 * Time in frames before the unit can use skills again
	 */
	private float skillCooldownRemaining;

	/**
	 * Time in frames. When a unit engages it cannot retreat for a certain amount of time.
	 */
	private float retreatCooldown = 0; // 480; // 8 seconds until can retreat
	private float retreatCooldownRemaining = 0;

	private float attackCooldown = 60;
	private float attackCooldownRemaining = 0;

	private float attackOfOpportunityCooldown = 120;
	private float attackOfOpportunityCooldownRemaining = 0;

	public enum Side { // For now, can assume that not being on the same side means that two units are enemies. Neutrals
						// and alliances, are a
						// non-trivial task
		PLAYER, ENEMY;

		public boolean isEnemy() {
			return this != PLAYER;
		}

		public boolean isPlayer() {
			return this == PLAYER;
		}
	}

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

	/**
	 * The skill the character is actively casting or channelling, else null. The Skill class will reset
	 * this when the active portion has finished.
	 */
	private ActiveSkill activeSkill;

	public Unit(float x, float y, WorldInfo world, Side side, String name) {
		this(x, y, side, world);
		setName(name);
	}

	public Unit(float x, float y, Side side, WorldInfo world) {
		this.id = Unit.getNextId();
		idToUnit.put(id, this);
		logger.debug("Placed unit id [{}]: ", id);

		this.x = x;
		this.y = y;
		this.side = side;
		this.world = world;

		// get neccesary references
		units = world.getUnits();

		motion = new UnitMotion(this, world);
		stats = new UnitStats(this);
		skills = new UnitSkills(this);
		equip = new UnitEquip(this);
		orderDeferring = new UnitOrderDeferring(this);
		ai = new UnitAI(this);

		graphics = new UnitGraphics(this);
	}

	/**
	 * Create unit from marker
	 */
	public Unit(UnitMarker m, WorldInfo world) {
		this(m.pos.x, m.pos.y, world, m.side, m.name);

		stats.base.set(m.baseStats);

		skills.addSkills(m.activeSkills);
		skills.addSkills(m.passiveSkills);
		skills.slotSkills(m.activeSkills);

		graphics.setAnimatedSprite(world.getAnimations().get(m.animatedSpriteName));

	}

	// Orders

	/**
	 * This method clears data that shouldn't persist from while changing maps. It is used when moving a
	 * unit to a new map. Note: This just clears a unit's data. It doesn't remove the unit from the
	 * world's collection (see: {@link World#removeAndDetachUnitFromWorld})
	 */
	public void clearMapLifeTimeData() {

	}

	public void clearOrderStateData() {

	}

	@Override
	public void orderMove(float x, float y) {
		if (!isMoveOrderAllowed()) {
			if (stats.isStunned()) {
				orderDeferring.tryToDeferOrder(Order.MOVE, null, x, y);
			}
			return;
		}
		if (motion.pathFindTowardsPoint(x, y)) {
			clearOrder();
			order = Order.MOVE;
		}
	}

	/**
	 * @param unit
	 * @return Whether the command was valid and accepted
	 */
	@Override
	public boolean orderAttackUnit(UnitOrderable unitOrd) {
		return orderAttackUnit(unitOrd, true);
	}

	/**
	 * 
	 * @param unitOrd
	 * @param isHardOrder A hard attack order makes the unit chase forever, whereas with a soft order
	 *                    the unit will stop chasing if out of range
	 * @return
	 */
	@Override
	public boolean orderAttackUnit(UnitOrderable unitOrd, boolean isHardOrder) {
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
	private boolean orderAttackUnit(UnitOrderable unitOrd, boolean isHardOrder, boolean isATauntedOrder) {
		Unit unit = (Unit) unitOrd; // underlying objects must hold the same type

		if (unit == this) {
			logger.warn("Unit can't be ordered to attack itself");
			return false;
		}
		if (isATauntedOrder ? isAnyOrderAllowedIgnoringTaunt() : isAttackOrderAllowed(unit)) {
			if (stats.isStunned()) {
				orderDeferring.tryToDeferOrder(isHardOrder ? Order.ATTACKUNIT_HARD : Order.ATTACKUNIT_SOFT,
						(Unit) unitOrd, 0, 0);
			}
			if (isAttacking())
				return orderAttackUnitWhileAlreadyAttacking(unitOrd, isHardOrder);

			clearOrder();
			this.order = isHardOrder ? Order.ATTACKUNIT_HARD : Order.ATTACKUNIT_SOFT;
			this.orderTarget = unit;

			this.motion.pathFindTowardsTarget();

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

		if (attacking == null) {
			logger.warn("order orderAttackUnitWhileAlreadyAttacking called but unit is not attacking");
			return false;
		}
		if (target == this) {
			logger.warn("Unit can't be ordered to attack itself)");
			return false;
		}

		if (Point.calcDistance(getPos(), target.getPos()) <= radius + target.radius
				+ Holo.defaultUnitSwitchEngageRange) {
			clearOrder();
			order = isHardOrder ? Order.ATTACKUNIT_HARD : Order.ATTACKUNIT_SOFT;
			orderTarget = target;
			attacking = target;
			return true;
		}
		return false;
	}

	@Override
	public void orderAttackMove(float x, float y) {
		if (isAttackMoveOrderAllowed()) {

			if (motion.pathFindTowardsPoint(x, y)) {
				clearOrder();
				attackMoveDestX = x;
				attackMoveDestY = y;
				this.order = Order.ATTACKMOVE;
			}
		} else if (stats.isStunned()) {
			orderDeferring.tryToDeferOrder(Order.ATTACKMOVE, null, x, y);
		}

	}

	@Override
	public void orderRetreat(float x, float y) {
		if (isRetreatOrderAllowed()) {
			retreat(x, y);
		} else if (stats.isStunned()) {
			orderDeferring.tryToDeferOrder(Order.RETREAT, null, x, y);
		}
	}

	private void retreat(float x, float y) {
		retreatDurationRemaining = retreatDuration;
		if (motion.pathFindTowardsPoint(x, y)) {
			stopAttacking();
			clearOrder();
			this.order = Order.RETREAT;
			stats.removeAllBasicAttackSlows();

			var attackers = getUnitsAttackingThis();
			for (Unit attacker : attackers) {
				attacker.stats.attackOfOpportunity(this.stats);
				attacker.attackOfOpportunityCooldownRemaining = attacker.attackOfOpportunityCooldown;
			}
		}
	}

	static final int retreatDuration = 50;
	/**
	 * For a short time when a unit starts retreating they can't be given any other commands
	 */
	int retreatDurationRemaining;

	/**
	 * A stop order stops a unit's motion and current order. You cannot use stop to cancel your own
	 * casting atm.
	 */
	@Override
	public void orderStop() {
		if (!isStopOrderAllowed()) {
			if (stats.isStunned())
				orderDeferring.clearDeferredOrder();
			return;
		}
		stopUnit();
	}

	/**
	 * @param skill The skill must have effects set already
	 */
	@Override
	public void orderUseSkill(ActiveSkill skill) {

		if (stats.isStunned()) {
			orderDeferring.clearDeferredOrder(); // deferring a skill order is not supported but it will still clear an
													// existing deferred order
		}

		if (!isUseSkillAllowed()) {
			return;
		}
		if (!skill.hasEnoughSp(this)) {
			return;
		}

		activeSkill = skill;
		skill.begin(this);
	}

	/**
	 * Stops a unit's motion and halts any current orders. It does not disengage a unit that is
	 * attacking, though.. Unlike orderStop, is not affected by order restrictions. As such, it should
	 * be for backend and not player use.
	 */
	public void stopUnit() {
		clearOrder();
		motion.stopCurrentMovement();
	}

	// @formatter:off
	@Override
	public boolean isAttackOrderAllowed(Unit target) {
		return isGeneralOrderAllowed() && isEnemy(target);
	}

	@Override
	public boolean isAttackOrderAllowed() {
		return isGeneralOrderAllowed();
	}

	@Override
	public boolean isMoveOrderAllowed() {
		return isGeneralOrderAllowed() && !isAttacking();
	}

	@Override
	public boolean isAttackMoveOrderAllowed() {
		return isGeneralOrderAllowed() && !isAttacking();
	}

	@Override
	public boolean isRetreatOrderAllowed() {
		return isAnyOrderAllowed() && isAttacking() && this.order != Order.RETREAT && retreatCooldownRemaining <= 0
				&& !(isCasting() || isChannelling());
	}

	@Override
	public boolean isStopOrderAllowed() {
		return isGeneralOrderAllowed();
	}

	@Override
	public boolean isUseSkillAllowed() {
		return isGeneralOrderAllowed() && !stats.isBlinded();
	}

	/**
	 * Returns the conditions which are shared by most ordinary orders
	 * 
	 * @return
	 */
	@Override
	public boolean isGeneralOrderAllowed() {
		return isAnyOrderAllowed() && !isBusyRetreating() && !(isCasting() || isChannelling());
	}

	/**
	 * Returns the conditions which are shared by all orders
	 * 
	 * @return
	 */
	@Override
	public boolean isAnyOrderAllowed() {
		return isAnyOrderAllowedIgnoringTaunt() && !stats.isTaunted();
	}

	@Override
	public boolean isAnyOrderAllowedIgnoringTaunt() {
		return !stats.isDead() && !motion.isBeingKnockedBack() && !stats.isStunned();
	}

	// @formatter:on

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
	 * Normal interrupts are caused by damage and reel. Some skills, particularly melee skills, are not
	 * interrupt by this.
	 */
	public void interruptNormal() {
		if (isCasting() || isChannelling()) {
			activeSkill.interrupt(false);
		}
	}

	public void interruptRangedSkills() {
		if (isCasting() || isChannelling()) {
			if (activeSkill != null && activeSkill.isRangedPhysicalOrMagicSkill()) {
				activeSkill.interrupt(false);
			}
		}
	}

	/**
	 * Hard interrupts are caused by stun / knockback
	 */
	public void interruptHard() {
		if (isCasting() || isChannelling()) {
			activeSkill.interrupt(true);
		}
	}

	// Tick Logic

	/**
	 * Main function: Determine movement, tick status effects, tick basic cooldowns
	 * 
	 */
	public void tickLogic() {
		if (isDead())
			return;

		ai.tick();

		motion.tick();
		stats.tick();

		tickOrderLogic();

		if (order == Order.RETREAT)
			retreatDurationRemaining -= 1;

		if (activeSkill != null)
			activeSkill.tick();

		tickSkillCooldowns();
		tickRetreatCooldown();
		tickAttackOfOpportunityCooldown();
	}

	private void tickSkillCooldowns() {
		if (skillCooldownRemaining > 0) {
			skillCooldownRemaining -= 1;
		}
		// tick individual skill Cooldowns too
		skills.tickSkillCooldowns();
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

	@Override
	public float getRetreatCooldown() {
		return retreatCooldownRemaining;
	}

	public boolean areSkillsOnCooldown() {
		return (skillCooldownRemaining > 0);
	}

	/**
	 * Does not even require units be alive or in units. For debugging purposes
	 */
	private static Map<Integer, Unit> idToUnit = new HashMap<Integer, Unit>();

	/** Handles the complex logic revolving around switching orders and targets */
	public void tickOrderLogic() {

		if (stats.isTaunted()) {
			if (order == Order.NONE) {

				if (isAnyOrderAllowedIgnoringTaunt())
					orderAttackUnit((Unit) stats.getTauntAttackTarget(), true, true);
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

	private void ifIdleAggroOntoNearbyTargets() {
		if (isCompletelyIdle()) {
			var closestTargets = UnitUtil.getTargetsSortedByDistance(this, world);
			if (!closestTargets.isEmpty()) {
				UnitOrderable closestEnemy = closestTargets.remove();

				float aggroRange = getSide() == Side.PLAYER ? Holo.alliedUnitsAggroRange : Holo.defaultAggroRange;

				if (Point.calcDistance(getPos(), closestEnemy.getPos()) <= aggroRange) {
					orderAttackUnit(closestEnemy, false);
				}
			}
		}
	}

	private void aggroOntoNearbyTargetsForAttackMove() {
		if (order == Order.ATTACKMOVE) {
			var closestTargets = UnitUtil.getTargetsSortedByDistance(this, world);
			if (!closestTargets.isEmpty()) {
				UnitOrderable closestEnemy = closestTargets.remove();

				float aggroRange = getSide() == Side.PLAYER ? Holo.alliedUnitsAggroRange : Holo.defaultAggroRange;

				if (Point.calcDistance(getPos(), closestEnemy.getPos()) <= aggroRange) {
					orderTarget = (Unit) closestEnemy; // manually set target/path, since we want to keep the ATTACKMOVE
														// order
					if (!motion.pathFindTowardsTarget()) {
						orderTarget = null; // keep walking normally if path to target not found
					}
				}
			}
		}
	}

	private void startAttackingIfInRangeForAttackOrders() {
		if (!isAttacking() && (order.isAttackUnit() || isAttackMoveAndHasTarget())) {
			float distToTarget = Point.calcDistance(this.getPos(), orderTarget.getPos());
			if (distToTarget <= getEngageRange(orderTarget)) {
				startAttacking(orderTarget);
			}
		}
	}

	private void stopAttackingIfEnemyIsOutOfRange() {
		if (isAttacking()) {
			float distToEnemy = Point.calcDistance(this.getPos(), attacking.getPos());
			if (distToEnemy >= getDisengageRange(attacking)) {
				stopAttacking();
			}
		}
	}

	private float getEngageRange(Unit unit) {
		return this.radius + unit.radius + Holo.defaultUnitEngageRange;
	}

	private float getDisengageRange(Unit unit) {
		return this.radius + unit.radius + Holo.defaultUnitDisengageRange;
	}

	private boolean isAttackMoveAndHasTarget() {
		return order == Order.ATTACKMOVE && orderTarget != null;
	}

	/**
	 * Preconditions: Target must be defined
	 */
	private void handleTargetLossAndSwitchingForAttackUnitSoft() {
		if (order == Order.ATTACKUNIT_SOFT) {
			var otherTargetsWithinAggroRange = UnitUtil.getTargetsSortedByDistance(this, world);
			otherTargetsWithinAggroRange
					.removeIf((t) -> Point.calcDistance(getPos(), t.getPos()) >= Holo.defaultAggroRange);
			otherTargetsWithinAggroRange.remove(orderTarget);
			float distToTarget = Point.calcDistance(this.getPos(), orderTarget.getPos());

			float aggroRange = getSide() == Side.PLAYER ? Holo.alliedUnitsAggroRange : Holo.defaultAggroRange;
			float chaseRange = getSide() == Side.PLAYER ? Holo.alliedUnitsAttackChaseRange
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
			var otherTargetsWithinAggroRange = UnitUtil.getTargetsSortedByDistance(this, world);
			otherTargetsWithinAggroRange
					.removeIf((t) -> Point.calcDistance(getPos(), t.getPos()) >= Holo.defaultAggroRange);
			otherTargetsWithinAggroRange.remove(orderTarget);
			float distToTarget = Point.calcDistance(this.getPos(), orderTarget.getPos());

			float aggroRange = getSide() == Side.PLAYER ? Holo.alliedUnitsAggroRange : Holo.defaultAggroRange;
			float chaseRange = getSide() == Side.PLAYER ? Holo.alliedUnitsAttackChaseRange
					: Holo.defaultUnitAttackChaseRange;

			if (distToTarget > aggroRange && !otherTargetsWithinAggroRange.isEmpty()) {
				// Switch targets

				Unit oldTarget = orderTarget;

				orderTarget = (Unit) otherTargetsWithinAggroRange.peek();
				if (!motion.pathFindTowardsTarget()) {
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
		if (!motion.pathFindTowardsPoint(attackMoveDestX, attackMoveDestY)) {
			logger.debug("Was attack moving but no path could be found to destination.");
			clearOrder();
		}
	}

	/**
	 * Updates attacking units
	 */
	public void tickAttacking() {
		if (isDead())
			return;

		if (!isCastingOrChanneling()) {
			attackCooldownRemaining = Math.max(0, attackCooldownRemaining - 1); // cooldown ticks even when unit not
																				// attacking
		}

		if (isAttacking()) {
			if (attacking.stats.isDead()) {
				stopAttacking();
				return;
			}
			if (attackCooldownRemaining <= 0) {
				this.attack(attacking);

				// Units automatically retaliate if they are idle
				if (attacking.getOrder() == Order.NONE && !attacking.isAttacking()
						&& attacking.isAttackOrderAllowed()) {
					attacking.orderAttackUnit(this, false);
				}
				attackCooldownRemaining = attackCooldown / stats.getMultiTeamingAtkspdPenalty(attacking);
			}
		}
	}

	private void attack(Unit enemy) {
		this.stats.attack(enemy.stats);
	}

	/**
	 * Lowest level method for attacking. Fine to call while attacking, though will warn you if you are
	 * already attacking the same target.
	 */
	private void startAttacking(Unit target) {
		if (isAttacking(target)) {
			logger.warn("Unit is already attacking the target");
			return;
		}
		if (isAttacking()) {
			stopAttacking();
		}

		motion.stopCurrentMovement();
		attacking = target;
		((World) world).onUnitStartsAttacking(this, attacking);

		// Attack cooldown may be artificially higher because of a recent stun/reel
		attackCooldownRemaining = Math.max(attackCooldownRemaining, attackCooldown / 4);
		retreatCooldownRemaining = retreatCooldown;
	}

	/**
	 * Disengages a unit.
	 */
	void stopAttacking() {
		if (isAttacking()) {
			((World) world).onUnitStopsAttacking(this, attacking);
			attacking = null;
		}
	}

	void addAttackCooldownRemaining(float value) {
		attackCooldownRemaining += value;
	}

	/**
	 * Only use for special cases, like a taunt ability. Uses startAttacking().
	 */
	public void setAttacking(Unit unit) {
		float distToEnemy = Point.calcDistance(getPos(), unit.getPos());
		if (distToEnemy >= getDisengageRange(unit)) {
			logger.info("Tried to set attacking, but unit out of range");
			return;
		}
		if (attacking != unit)
			startAttacking(unit);
	}

	// Debug
	private static int getNextId() {
		return curId++;
	}

	// Debug Rendering
	public void renderAttackingArrow() {
		if (isAttacking()) {
			HoloGL.renderArrow(this, attacking, Color.RED);
		}
	}

	void unitDies() {
		motion.stopCurrentMovement();
		this.clearOrder();

		// Stop this (now-dead) unit from attacking
		if (isAttacking()) {
			stopAttacking();
		}
		// Don't actually remove the unit here -- world will handle that
	}

	// For now we allow multiple player characters
	@Override
	public boolean isAPlayerCharacter() {
		return side == Side.PLAYER;
	}

	@Override
	public String toString() {
		return String.format("Unit[ID: %s]", this.id);

	}

	// Convenience functions
	public static float getDist(Unit u1, Unit u2) {
		return Point.calcDistance(u1.getPos(), u2.getPos());
	}

	// Getters
	@Override
	public float getRadius() {
		return radius;
	}

	@Override
	public Point getPos() {
		return new Point(this.x, this.y);
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public Path getPath() {
		return motion.getPath();
	}

	@Override
	public Side getSide() {
		return side;
	}

	@Override
	public UnitStatsInfo getStats() {
		return stats;
	}

	@Override
	public Order getOrder() {
		return order;
	}

	@Override
	public UnitInfo getOrderTarget() {
		return orderTarget;
	}

	@Override
	public boolean isAttacking() {
		return attacking != null;
	}

	@Override
	public boolean isAttacking(UnitInfo target) {
		return attacking == target;
	}

	@Override
	public Unit getAttacking() {
		return attacking;
	}

	public boolean isCasting() {
		return this.activeSkill != null && activeSkill.getStatus() == Status.CASTING;
	}

	public boolean isChannelling() {
		return this.activeSkill != null && activeSkill.getStatus() == Status.CHANNELING;
	}

	@Override
	public boolean isBusyRetreating() {
		return order == Order.RETREAT && retreatDurationRemaining > 0;
	}

	@Override
	public WorldInfo getWorld() {
		return world;
	}

	/**
	 * Some classes only get a reference to WorldInfo because they are not intended to modify the world.
	 * This method can be used to explicitly get a mutable World instance.
	 * 
	 * @return
	 */
	public World getWorldMutable() {
		return (World) world;
	}

	public Set<Unit> getUnitsAttackingThis() {
		return world.getUnitsAttackingThis(this);
	}

	@Override
	public ActiveSkill getActiveSkill() {
		return activeSkill;
	}

	public void setActiveSkill(ActiveSkill activeSkill) {
		this.activeSkill = activeSkill;
	}

	public void setSkillCooldown(float value) {
		skillCooldownRemaining = value;
	}

	@Override
	public int getID() {
		return id;
	}

	/**
	 * For debug purposes. Gets any created unit, regardless of whether is it in the world.
	 */
	public static Unit getUnitByID(int id) {
		return idToUnit.get(id);
	}

	@Override
	public boolean isCastingOrChanneling() {
		if (getActiveSkill() == null)
			return false;
		return (getActiveSkill().getStatus() == Status.CASTING || getActiveSkill().getStatus() == Status.CHANNELING);
	}

	/**
	 * Not doing any action nor has any order assigned
	 */
	@Override
	public boolean isCompletelyIdle() {
		return order == Order.NONE && !isAttacking() && !isCastingOrChanneling();
	}

	@Override
	public UnitMotion getMotion() {
		return motion;
	}

	@Override
	public boolean isDead() {
		return stats.isDead();
	}

	@Override
	public UnitSkills getSkills() {
		return skills;
	}

	@Override
	public float getAttackCooldown() {
		return attackCooldown;
	}

	@Override
	public float getAttackCooldownRemaining() {
		return attackCooldownRemaining;
	}

	public void setName(String name) {
		this.stats.setName(name);
	}

	@Override
	public String getName() {
		return stats.getName();
	}

	@Override
	public UnitAI getAI() {
		return ai;
	}

	@Override
	public boolean isEnemy(UnitInfo unit) {
		return getSide() != unit.getSide();
	}

}
