package com.mygdx.holowyth.unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.combatDemo.World;
import com.mygdx.holowyth.combatDemo.WorldInfo;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.UnitInterPF;
import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.skill.Skill.Status;
import com.mygdx.holowyth.unit.behaviours.AggroIfIsEnemyUnit;
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

public class Unit implements UnitInterPF, UnitInfo, UnitOrderable {

	public float x, y;

	// Components
	public final UnitMotion motion;
	public final UnitStats stats;

	// World Fields
	ArrayList<Unit> units;

	// Collision Detection
	private float radius = Holo.UNIT_RADIUS;

	// Debug
	private static int curId = 0;
	private final int ID;

	/**
	 * todo
	 */
	public boolean debugIsOnIllegalLocation = false;
	public int debugNumOfUnitsCollidingWith = 0;

	// Orders
	Order currentOrder = Order.IDLE;
	Unit target; // target for the current command.

	// Combat
	/** The unit this unit is attacking. Attacking a unit <--> being engaged. */
	Unit attacking;
	/**
	 * Maps from a unit to a list of units attacking that one unit. Other classes should use {@link #getAttackers()}
	 */
	private static Map<Unit, Set<Unit>> unitsAttacking = new HashMap<Unit, Set<Unit>>();
	Mode mode = Mode.PASSIVE;

	/**
	 * A unit's friendly/foe status.
	 */
	Side side;

	// Skills

	int skillCooldown;

	private WorldInfo world;

	public enum Side { // for now, simple, two forces
		PLAYER, ENEMY
	}

	public enum Order {
		MOVE, ATTACKUNIT, IDLE, ATTACKMOVE, RETREAT
	}

	public enum Mode {
		ENGAGE, PASSIVE
	}

	/**
	 * The skill the character is actively casting or channelling, else null. The Skill class will reset this when the
	 * active portion has finished.
	 */
	private Skill activeSkill;

	public Unit(float x, float y, WorldInfo world, Side side) {
		this.ID = Unit.getNextId();
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
			debugValues.add(new DebugValue("skillCooldown", () -> skillCooldown));

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
	public void orderMove(float dx, float dy) {
		if (!isMoveOrderAllowed()) {
			return;
		}
		if (motion.orderMove(dx, dy)) {
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
		Unit unit = (Unit) unitOrd; // underlying objects must hold the same type

		if (!isAttackOrderAllowed(unit)) {
			return false;
		}
		if (unit == this) {
			System.out.println("Warning: invalid attack command (unit can't attack itself)");
			return false;
		}
		clearOrder();
		this.currentOrder = Order.ATTACKUNIT;
		this.target = unit;

		this.motion.pathForAttackingUnit();
		return true;
	}

	@Override
	public void orderAttackMove(float x, float y) {
		if (!isAttackMoveOrderAllowed()) {
			return;
		}
		// TODO:

	}

	@Override
	public void orderRetreat(float x, float y) {
		if (!isRetreatOrderAllowed()) {
			return;
		}

		retreatDurationRemaining = retreatDuration;
		if (motion.orderMove(x, y)) {
			stopAttacking();
			clearOrder();
			this.currentOrder = Order.RETREAT;
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
		motion.stopCurrentMovement();
		clearOrder();
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
	 * Stops a unit's motion and halts any current orders. Unlike orderStop, is not affected by order restrictions.
	 */
	public void stopUnit() {
		clearOrder();
		motion.stopCurrentMovement();
	}

	// @formatter:off
		private boolean isAttackOrderAllowed(Unit target) {
			return !stats.isDead()
					&& attacking == null
					&& target.side != this.side
					&& !isRetreatCooldownActive()
					&& !(isCasting() || isChannelling());
		}

		private boolean isMoveOrderAllowed() {
			return !stats.isDead()
					&& attacking == null
					&& !isRetreatCooldownActive()
					&& !(isCasting() || isChannelling());
		}

		private boolean isAttackMoveOrderAllowed() {
			return isMoveOrderAllowed();
		}
		private boolean isRetreatOrderAllowed() {
			return !stats.isDead()
					&& this.attacking != null
					&& this.currentOrder != Order.RETREAT
					&& !(isCasting() || isChannelling());
		}
		private boolean isStopOrderAllowed() {
			return !stats.isDead()
					&& !isRetreatCooldownActive()
					&& !(isCasting() || isChannelling());
		}
		private boolean isUseSkillAllowed() {
			return !stats.isDead()
					&& !isRetreatCooldownActive()
					&& !(isCasting() || isChannelling())
					&& !areSkillsOnCooldown();
		}
		
		// @formatter:on

	/**
	 * Clears any current order on this unit. For internal use.
	 */
	void clearOrder() {
		currentOrder = Order.IDLE;
		target = null;
	}

	public boolean isCasting() {
		return this.activeSkill != null && activeSkill.getStatus() == Status.CASTING;
	}

	public boolean isChannelling() {
		return this.activeSkill != null && activeSkill.getStatus() == Status.CHANNELING;
	}

	@Override
	public boolean isRetreatCooldownActive() {
		return currentOrder == Order.RETREAT && retreatDurationRemaining > 0;
	}
	// @formatter:on

	/**
	 * Caused by normal attacking or stun effects. Interrupts any casting or channeling spell
	 */
	public void interrupt() {
		if (isCasting() || isChannelling()) {
			activeSkill.interrupt();
		}
	}

	// Combat

	/**
	 * Main function, also is where the unit determines its movement
	 */
	public void handleLogic() {
		motion.tick();
		AggroIfIsEnemyUnit.runOn(this, world);
		if (currentOrder == Order.RETREAT)
			retreatDurationRemaining -= 1;

		if (activeSkill != null)
			activeSkill.tick();

		tickSkillCooldown();
	}

	private void tickSkillCooldown() {
		if (skillCooldown > 0) {
			skillCooldown -= 1;
		}
	}

	public boolean areSkillsOnCooldown() {
		return (skillCooldown > 0);
	}

	private int attackCooldown = 60;
	private int attackCooldownLeft = 0;

	/** Handles the combat logic for a unit for one frame */
	public void handleCombatLogic() {

		if (attacking != null && attacking.stats.isDead()) {
			stopAttacking();
		}

		if (currentOrder == Order.ATTACKUNIT) {
			// If the unit is on an attackUnit command and its in engage range, make it start attacking the target
			// If the unit falls out of engage range, stop it from attacking

			if (target.stats.isDead()) {
				clearOrder();
				return;
			}

			Point a, b;
			a = this.getPos();
			b = target.getPos();
			float dist = Point.calcDistance(a, b);
			if ((attacking != target) && dist <= this.radius + target.radius + Holo.defaultUnitEngageRange) {
				startAttacking(target);
			} else if ((attacking == target) && dist >= this.radius + target.radius + Holo.defaultUnitDisengageRange) {
				stopAttacking();
			}

			if (isAttacking()) {
				if (attackCooldownLeft <= 0) {
					this.attack(attacking);
					attackCooldownLeft = Math.round(attackCooldown / getAttackingSameTargetAtkspdPenalty(attacking));
					// System.out.println("attack cooldown reset: " + attackCooldownLeft + " Penalty: " +
					// getAttackingSameTargetAtkspdPenalty(attacking));
				} else {
					attackCooldownLeft -= 1;
				}
			}
		}

		if (this.mode == Mode.ENGAGE) {
			Set<Unit> attackingMe = unitsAttacking.get(this);
			if (!attackingMe.isEmpty() && attacking == null) {
				orderAttackUnit(attackingMe.iterator().next());
			}
		}

	}

	private float getAttackingSameTargetAtkspdPenalty(Unit target) {
		int n = unitsAttacking.get(target).size();

		if (n <= 1) {
			return 1;
		} else if (n == 2) {
			return 0.8f;
		} else if (n == 3) {
			return 0.6f;
		} else { // 4 or more
			return 0.5f;
		}
	}

	private void attack(Unit enemy) {
		this.stats.attack(enemy.stats);
	}

	private void startAttacking(Unit target) {
		motion.stopCurrentMovement();
		attacking = target;
		unitsAttacking.get(attacking).add(this);

		attackCooldownLeft = attackCooldown / 2;
	}

	/**
	 * Disengages a unit.
	 */
	private void stopAttacking() {
		if (this.attacking != null) {
			unitsAttacking.get(this.attacking).remove(this);
			attacking = null;
		}
	}

	private boolean isAttacking() {
		return attacking != null;
	}

	// Debug
	private static int getNextId() {
		return curId++;
	}

	// Debug Rendering
	public void renderAttackingArrow() {
		if (this.attacking != null) {
			HoloGL.renderArrow(this, this.attacking, Color.RED);
		}
	}

	void unitDies() {
		motion.stopCurrentMovement();
		this.clearOrder();

		// Stop this (now-dead) unit from attacking
		if (attacking != null) {
			stopAttacking();
		}
	}

	// For now we allow multiple player characters
	public boolean isPlayerCharacter() {
		return side == Side.PLAYER;
	}

	@Override
	public String toString() {
		return String.format("Unit[ID: %s]", this.ID);

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
	public UnitInfo getAttacking() {
		return attacking;
	}

	public WorldInfo getWorld() {
		return world;
	}

	/**
	 * Some classes only get a reference to WorldInfo because they are not intended to modify the world. This method can
	 * be used to explicitly get a mutable World instance.
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

	public void setSkillCooldown(int value) {
		skillCooldown = value;
	}

}
