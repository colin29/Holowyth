package com.mygdx.holowyth.unit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.combatDemo.World;
import com.mygdx.holowyth.combatDemo.WorldInfo;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.UnitInterPF;
import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.skill.Skill.Status;
import com.mygdx.holowyth.unit.behaviours.UnitUtil;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.unit.interfaces.UnitStatsInfo;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.tools.debugstore.DebugValue;
import com.mygdx.holowyth.util.tools.debugstore.DebugValues;

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
 * There are four aspects that largely determine a unit's state. They can be independent, but often check each other for legality. <br>
 * For example, some skills might castable while attacking, and a unit could retain certain orders while attacking.
 * 
 * currentOrder -- a unit with an order will continue trying to do something <br>
 * attacking -- a unit attacking is locked in combat and will regularly attack their target <br>
 * activeSkill -- a unit with an active ability is either casting or channelling that ability
 * 
 * motion.isBeingKnockedBack() -- a unit being knocked back cannot be given new orders or perform any action (it may retain its old order, depending
 * on the type)
 */
public class Unit implements UnitInterPF, UnitInfo, UnitOrderable {

	public float x, y;

	// Components
	public final UnitMotion motion;
	public final UnitStats stats;

	// World Fields
	List<Unit> units;

	// Collision Detection
	private float radius = Holo.UNIT_RADIUS;

	// Debug
	private static int curId = 0;
	private final int id;

	/**
	 * todo
	 */
	public boolean debugIsOnIllegalLocation = false;
	public int debugNumOfUnitsCollidingWith = 0;

	// Orders

	Order currentOrder = Order.NONE;
	Unit target; // target for the current command.

	// Combat
	/** The unit this unit is attacking. Attacking a unit <--> being engaged. */
	private Unit attacking;
	/**
	 * Maps from a unit to a list of units attacking that one unit. Other classes should use {@link #getAttackers()}
	 */
	private static Map<Unit, Set<Unit>> unitsAttacking = new HashMap<Unit, Set<Unit>>();

	/**
	 * A unit's friendly/foe status.
	 */
	Side side;

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

	private WorldInfo world;

	public enum Side { // For now, can assume that not being on the same side means that two units are enemies. Neutrals and alliances, are a
						// non-trivial task
		PLAYER, ENEMY
	}

	// Order-specific
	private float attackMoveDestX;
	private float attackMoveDestY;

	/**
	 * A unit's order represents whether it has any persistent order on it that would determine its behaviour. Note that a unit without a command
	 * could still be casting or attacking.
	 * 
	 * AttackUnit can be hard or soft. Hard will chase indefinitely, a soft attack order will stop chasing if the target goes out of range
	 */
	public enum Order {
		MOVE, ATTACKUNIT_HARD, ATTACKUNIT_SOFT, NONE, ATTACKMOVE, RETREAT;

		public boolean isAttackUnit() {
			return this == ATTACKUNIT_HARD || this == ATTACKUNIT_SOFT;
		}
	}

	/**
	 * The skill the character is actively casting or channelling, else null. The Skill class will reset this when the active portion has finished.
	 */
	private Skill activeSkill;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public Unit(float x, float y, WorldInfo world, Side side) {
		this.id = Unit.getNextId();
		idToUnit.put(id, this);
		logger.debug("Placed id [{}]: ", id);

		this.x = x;
		this.y = y;
		this.side = side;

		// get neccesary references
		this.units = world.getUnits();

		Unit.unitsAttacking.put(this, new HashSet<Unit>());

		this.world = world;
		this.motion = new UnitMotion(this, world);

		this.stats = new UnitStats(this);

		if (this.side == Side.PLAYER) {
			DebugValues debugValues = this.getWorldMutable().getDebugStore().registerComponent("Player unit");
			debugValues.add(new DebugValue("sp", () -> stats.getSp() + "/" + stats.getMaxSp()));
			debugValues.add(new DebugValue("skillCooldown", () -> skillCooldownRemaining));

			debugValues.add("speed", () -> motion.getCurPlannedSpeed());
		}

	}

	public Unit(float x, float y, WorldInfo world, Side side, String name) {
		this(x, y, world, side);
		setName(name);
	}

	public void setName(String name) {
		this.stats.setName(name);
	}

	// Orders

	@Override
	public void orderMove(float x, float y) {
		if (!isMoveOrderAllowed()) {
			return;
		}
		if (motion.pathFindTowardsPoint(x, y)) {
			clearOrder();
			currentOrder = Order.MOVE;
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
	 * @param isHardOrder
	 *            A hard attack order makes the unit chase forever, whereas with a soft order the unit will stop chasing if out of range
	 * @return
	 */
	@Override
	public boolean orderAttackUnit(UnitOrderable unitOrd, boolean isHardOrder) {
		Unit unit = (Unit) unitOrd; // underlying objects must hold the same type

		if (!isAttackOrderAllowed(unit)) {
			return false;
		}
		if (unit == this) {
			System.out.println("Warning: invalid attack command (unit can't attack itself)");
			return false;
		}
		clearOrder();
		this.currentOrder = isHardOrder ? Order.ATTACKUNIT_HARD : Order.ATTACKUNIT_SOFT;
		this.target = unit;

		this.motion.pathFindTowardsTarget();
		return true;
	}

	@Override
	public boolean orderSwitchAttackUnit(UnitOrderable unitOrd, boolean isHardOrder) {
		Unit unit = (Unit) unitOrd; // underlying objects must hold the same type

		if (!isSwitchAttackOrderAllowed(unit)) {
			return false;
		}
		if (unit == this) {
			System.out.println("Warning: invalid switch attack command (unit can't attack itself)");
			return false;
		}

		if (Point.calcDistance(getPos(), unit.getPos()) <= radius + target.radius + Holo.defaultUnitSwitchEngageRange) {
			this.currentOrder = isHardOrder ? Order.ATTACKUNIT_HARD : Order.ATTACKUNIT_SOFT;
			this.target = unit;
			this.attacking = unit;
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
				this.currentOrder = Order.ATTACKMOVE;
			}
		}

	}

	@Override
	public void orderRetreat(float x, float y) {
		if (isRetreatOrderAllowed()) {
			retreat(x, y);
		}
	}

	private void retreat(float x, float y) {
		retreatDurationRemaining = retreatDuration;
		if (motion.pathFindTowardsPoint(x, y)) {
			stopAttacking();
			clearOrder();
			this.currentOrder = Order.RETREAT;
			stats.removeAllBasicAttackSlows();

			var attackers = getAttackers();
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
	 * A stop order stops a unit's motion and current order. You cannot use stop to cancel your own casting atm.
	 */
	@Override
	public void orderStop() {
		if (!isStopOrderAllowed()) {
			return;
		}
		stopUnit();
	}

	@Override
	public void orderUseSkill(Skill skill) {

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
	 * Stops a unit's motion and halts any current orders. It does not disengage a unit that is attacking, though.. Unlike orderStop, is not affected
	 * by order restrictions. As such, it should be for backend and not player use.
	 */
	public void stopUnit() {
		clearOrder();
		motion.stopCurrentMovement();
	}

	// @formatter:off
		private boolean isAttackOrderAllowed(Unit target) {
			return isGeneralOrderAllowed()
					&& !isAttacking()
					&& target.side != this.side;
		}
		private boolean isSwitchAttackOrderAllowed(Unit target) {
			return isGeneralOrderAllowed()
					&& target.side != this.side;
		}

		private boolean isMoveOrderAllowed() {
			return isGeneralOrderAllowed()
					&& !isAttacking();
		}

		private boolean isAttackMoveOrderAllowed() {
			return isGeneralOrderAllowed()
					&& !isAttacking();
		}
		private boolean isRetreatOrderAllowed() {
			return isAnyOrderAllowed()
					&& isAttacking()
					&& this.currentOrder != Order.RETREAT
					&& retreatCooldownRemaining <= 0
					&& !(isCasting() || isChannelling());
		}
		private boolean isStopOrderAllowed() {
			return isGeneralOrderAllowed();
		}

		
		private boolean isUseSkillAllowed() {
			return isGeneralOrderAllowed()
					&& !areSkillsOnCooldown();
		}
		
		/**
		 * Returns the conditions which are shared by most ordinary orders
		 * @return
		 */
		private boolean isGeneralOrderAllowed() {
			return isAnyOrderAllowed()
					&& !isBusyRetreating()
					&& !(isCasting() || isChannelling());
		}
		/**
		 * Returns the conditions which are shared by all orders
		 * @return
		 */
		private boolean isAnyOrderAllowed() {
			return !stats.isDead()
			&& !motion.isBeingKnockedBack();
		}
		
		
		// @formatter:on

	/**
	 * Clears any current order on this unit. For internal use.
	 */
	void clearOrder() {
		currentOrder = Order.NONE;
		target = null;

		attackMoveDestX = 0;
		attackMoveDestY = 0;
	}

	/**
	 * Caused by normal attacking or stun effects. Interrupts any casting or channeling spell
	 */
	public void interrupt() {
		if (isCasting() || isChannelling()) {
			activeSkill.interrupt();
		}
	}

	// Tick Logic

	/**
	 * Main function: Determine movement, tick status effects, tick basic cooldowns
	 * 
	 */
	public void tickLogic() {
		motion.tick();
		stats.tick();

		tickOrderLogic();

		if (currentOrder == Order.RETREAT)
			retreatDurationRemaining -= 1;

		if (activeSkill != null)
			activeSkill.tick();

		tickSkillCooldown();
		tickRetreatCooldown();
		tickAttackOfOpportunityCooldown();
	}

	private void tickSkillCooldown() {
		if (skillCooldownRemaining > 0) {
			skillCooldownRemaining -= 1;
		}
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

	/**
	 * Updates attacking units
	 */
	public void tickAttacking() {
		if (isAttacking()) {
			if (attacking.stats.isDead()) {
				stopAttacking();
				return;
			}
			if (attackCooldownRemaining <= 0) {
				this.attack(attacking);
				attackCooldownRemaining = attackCooldown / getMultiTeamingAtkspdPenalty(attacking);
			} else {
				attackCooldownRemaining -= 1;
			}
		}
	}

	/** Handles the complex logic revolving around switching orders and targets */
	public void tickOrderLogic() {

		if (this.side == Side.ENEMY) {
			ifIdleAggroOntoNearbyTargets();
		}

		if (target != null && target.stats.isDead()) {
			if (currentOrder.isAttackUnit()) {
				clearOrder();
			}
			if (isAttackMoveAndHasTarget()) {
				target = null;
				repathToDestinationForAttackMove();
			}
		}

		if (currentOrder.isAttackUnit()) {
			handleTargetLossAndSwitchingForAttackUnit();
		} else if (currentOrder == Order.ATTACKMOVE) {
			// If an attacking moving unit has no target it's moving towards its destination. If it does have a target it's chasing that unit.
			if (target == null) {
				aggroOntoNearbyTargetsForAttackMove();
			}
			if (target != null) {
				handleTargetLossAndSwitchingForAttackMove();
			}
		}

		handleEngageAndDisengage();
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
		if (currentOrder == Order.ATTACKMOVE) {
			var closestTargets = UnitUtil.getTargetsSortedByDistance(this, world);
			if (!closestTargets.isEmpty()) {
				UnitOrderable closestEnemy = closestTargets.remove();

				float aggroRange = getSide() == Side.PLAYER ? Holo.alliedUnitsAggroRange : Holo.defaultAggroRange;

				if (Point.calcDistance(getPos(), closestEnemy.getPos()) <= aggroRange) {
					target = (Unit) closestEnemy; // manually set target/path, since we want to keep the ATTACKMOVE order
					if (!motion.pathFindTowardsTarget()) {
						target = null; // keep walking normally if path to target not found
					}
				}
			}
		}
	}

	private void handleEngageAndDisengage() {
		if (currentOrder.isAttackUnit() || isAttackMoveAndHasTarget()) {
			float distToTarget = Point.calcDistance(this.getPos(), target.getPos());

			if (!isAttacking() && distToTarget <= this.radius + target.radius + Holo.defaultUnitEngageRange) {
				startAttacking(target);
			} else if (isAttacking(target) && distToTarget >= this.radius + target.radius + Holo.defaultUnitDisengageRange) {
				stopAttacking();
			}
		}
	}

	boolean isAttackMoveAndHasTarget() {
		return currentOrder == Order.ATTACKMOVE && target != null;
	}

	/**
	 * Preconditions: Target must be defined
	 */
	private void handleTargetLossAndSwitchingForAttackUnit() {
		if (currentOrder == Order.ATTACKUNIT_SOFT) {
			var otherTargetsWithinAggroRange = UnitUtil.getTargetsSortedByDistance(this, world);
			otherTargetsWithinAggroRange.removeIf((t) -> Point.calcDistance(getPos(), t.getPos()) >= Holo.defaultAggroRange);
			otherTargetsWithinAggroRange.remove(target);
			float distToTarget = Point.calcDistance(this.getPos(), target.getPos());

			float aggroRange = getSide() == Side.PLAYER ? Holo.alliedUnitsAggroRange : Holo.defaultAggroRange;
			float chaseRange = getSide() == Side.PLAYER ? Holo.alliedUnitsAttackChaseRange : Holo.defaultUnitAttackChaseRange;

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
		if (currentOrder == Order.ATTACKMOVE) {
			var otherTargetsWithinAggroRange = UnitUtil.getTargetsSortedByDistance(this, world);
			otherTargetsWithinAggroRange.removeIf((t) -> Point.calcDistance(getPos(), t.getPos()) >= Holo.defaultAggroRange);
			otherTargetsWithinAggroRange.remove(target);
			float distToTarget = Point.calcDistance(this.getPos(), target.getPos());

			float aggroRange = getSide() == Side.PLAYER ? Holo.alliedUnitsAggroRange : Holo.defaultAggroRange;
			float chaseRange = getSide() == Side.PLAYER ? Holo.alliedUnitsAttackChaseRange : Holo.defaultUnitAttackChaseRange;

			if (distToTarget > aggroRange && !otherTargetsWithinAggroRange.isEmpty()) {
				// Switch targets

				Unit oldTarget = target;

				target = (Unit) otherTargetsWithinAggroRange.peek();
				if (!motion.pathFindTowardsTarget()) {
					target = oldTarget;
					logger.debug("Was attack moving but no path could be found to the nearest unit, ignoring");
				}

			} else if (distToTarget > chaseRange) {
				// Instead of clearing order, repath and resume moving towards the original destination
				repathToDestinationForAttackMove();
			}
		}
	}

	private void repathToDestinationForAttackMove() {
		target = null;
		if (!motion.pathFindTowardsPoint(attackMoveDestX, attackMoveDestY)) {
			logger.debug("Was attack moving but no path could be found to destination.");
			clearOrder();
		}
	}

	private float getMultiTeamingAtkspdPenalty(Unit target) {
		int n = unitsAttacking.get(target).size();

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

	private void attack(Unit enemy) {
		this.stats.attack(enemy.stats);
	}

	private void startAttacking(Unit target) {
		motion.stopCurrentMovement();
		attacking = target;
		unitsAttacking.get(attacking).add(this);

		attackCooldownRemaining = attackCooldown / 4;
		retreatCooldownRemaining = retreatCooldown;
	}

	/**
	 * Disengages a unit.
	 */
	void stopAttacking() {
		if (isAttacking()) {
			unitsAttacking.get(attacking).remove(this);
			attacking = null;
		}
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
	public Order getCurrentOrder() {
		return currentOrder;
	}

	@Override
	public UnitInfo getTarget() {
		return target;
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
	public UnitInfo getAttacking() {
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
		return currentOrder == Order.RETREAT && retreatDurationRemaining > 0;
	}

	public WorldInfo getWorld() {
		return world;
	}

	/**
	 * Some classes only get a reference to WorldInfo because they are not intended to modify the world. This method can be used to explicitly get a
	 * mutable World instance.
	 * 
	 * @return
	 */
	public World getWorldMutable() {
		return (World) world;
	}

	public Set<Unit> getAttackers() {
		return unitsAttacking.get(this);
	}

	@Override
	public Skill getActiveSkill() {
		return activeSkill;
	}

	public void setActiveSkill(Skill activeSkill) {
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
		return (getActiveSkill().getStatus() == Status.CASTING
				|| getActiveSkill().getStatus() == Status.CHANNELING);
	}

	@Override
	public boolean isCompletelyIdle() {
		return currentOrder == Order.NONE && !isAttacking() && !isCastingOrChanneling();
	}

	@Override
	public UnitMotion getMotion() {
		return motion;
	}

	@Override
	public boolean isDead() {
		return stats.isDead();
	}

}
