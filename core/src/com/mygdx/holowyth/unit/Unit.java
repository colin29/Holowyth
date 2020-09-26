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
import com.mygdx.holowyth.gameScreen.MapInstance;
import com.mygdx.holowyth.gameScreen.MapInstanceInfo;
import com.mygdx.holowyth.gameScreen.basescreens.GameMapLoadingScreen;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.map.UnitMarker;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.UnitPF;
import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.skill.ActiveSkill.Status;
import com.mygdx.holowyth.unit.UnitOrders.Order;
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
	
	/** For debugging. Does not even require units be alive or in a MapInstance. */
	private static Map<Integer, Unit> idToUnit = new HashMap<Integer, Unit>();

	// Unit life-time Components
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

	// State
	/** A unit's friendly/foe type. */
	Side side;

	/////////////// World specific state /////////////

	// Map life-time Components (are discarded when a unit moves to a new map)
	UnitMotion motion;
	UnitCombat combat;
	UnitOrders orders;

	// General
	public float x, y;
	private MapInstanceInfo mapInstance;

	// Ordering
	

	// Skills
	/**
	 * The skill the character is actively casting or channelling, else null. The Skill class will reset
	 * this when the active portion has finished.
	 */
	ActiveSkill activeSkill;
	/**
	 * Time in frames before the unit can use skills again
	 */
	private float skillCooldownRemaining;

	
	/////////////// End of Fields /////////////
	
	public enum Side { // For now, just two enemy and player. Neutrals and alliances, are a non-trivial task
		PLAYER, ENEMY;
		public boolean isEnemy() {
			return this != PLAYER;
		}

		public boolean isPlayer() {
			return this == PLAYER;
		}
	}


	public Unit(float x, float y, MapInstanceInfo world, Side side, String name) {
		this(x, y, side, world);
		setName(name);
	}

	public Unit(float x, float y, Side side, MapInstanceInfo world) {
		this.id = Unit.getNextId();
		idToUnit.put(id, this);
		logger.debug("Placed unit id [{}]: ", id);

		this.x = x;
		this.y = y;
		this.side = side;
		this.mapInstance = world;

		// Init unit lifetime components before map-lifetime components, just so that the latter can acquire reference in constructor.
		stats = new UnitStats(this);
		skills = new UnitSkills(this);
		equip = new UnitEquip(this);
		ai = new UnitAI(this);
		graphics = new UnitGraphics(this);
		
		// Init map lifetime components
		motion = new UnitMotion(this, world);
		combat = new UnitCombat(this);
		orders =  new UnitOrders(this);

		
	}

	/**
	 * Create unit from marker
	 */
	public Unit(UnitMarker m, MapInstanceInfo world) {
		this(m.pos.x, m.pos.y, world, m.side, m.name);

		stats.base.set(m.baseStats);

		skills.addSkills(m.activeSkills);
		skills.addSkills(m.passiveSkills);
		skills.slotSkills(m.activeSkills);

		graphics.setAnimatedSprite(world.getAnimations().get(m.animatedSpriteName));

	}

	/** Specifically for units that have been initialized and removed from a world once */
	public void reinitializeForWorld(MapInstance world) {
		this.mapInstance = world;
		motion = new UnitMotion(this, world);
		combat = new UnitCombat(this);
		orders = new UnitOrders(this);
		stats.reinitializeForWorld();
	}

	/**
	 * This method clears data that shouldn't persist from while changing maps. It is used when moving a
	 * unit to a new map. Note: This just clears a unit's data. It doesn't remove the unit from the
	 * world's collection (Use: {@link MapInstance#removeAndDetachUnitFromWorld})
	 */
	public void clearMapLifeTimeData() {
		notifyAppLifetimeComponentsToClearMapLifeTimeData();
		clearMapLifeTimeComponents();
		clearGeneralData();
		clearSkillsData();
	}
	private void notifyAppLifetimeComponentsToClearMapLifeTimeData() {
		stats.clearMapLifetimeData();
		skills.clearMapLifetimeData();
		graphics.clearMapLifetimeData();
		equip.clearMapLifetimeData();
		ai.clearMapLifetimeData();
	}

	private void clearMapLifeTimeComponents() {
		motion = null;
		combat = null;
		orders = null;
	}
	
	private void clearGeneralData() {
		x=0;
		y=0;
		mapInstance = null;
	}
	
	private void clearSkillsData() {
		activeSkill = null;
		skillCooldownRemaining = 0;
	}

	@Override
	public void orderMove(float x, float y) {
		orders.orderMove(x, y);
	}

	/**
	 * @param unit
	 * @return Whether the command was valid and accepted
	 */
	@Override
	public boolean orderAttackUnit(UnitOrderable unitOrd) {
		return orders.orderAttackUnit(unitOrd);
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
		return orders.orderAttackUnit(unitOrd, isHardOrder);
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
		return orders.orderAttackUnit(unitOrd, isHardOrder, isATauntedOrder);
	}

	@Override
	public void orderAttackMove(float x, float y) {
		orders.orderAttackMove(x, y);
	}

	@Override
	public void orderRetreat(float x, float y) {
		orders.orderRetreat(x, y);
	}

	/**
	 * A stop order stops a unit's motion and current order. You cannot use stop to cancel your own
	 * casting atm.
	 */
	@Override
	public void orderStop() {
		orders.orderStop();
	}

	/**
	 * @see UnitOrders#orderUseSkill(ActiveSkill)
	 */
	@Override
	public void orderUseSkill(ActiveSkill skill) {
		orders.orderUseSkill(skill);
	}

	/**
	 * @see UnitOrders#stopUnit()
	 */
	public void stopUnit() {
		orders.stopUnit();
	}

	// @formatter:off
	@Override
	public boolean isAttackOrderAllowed(Unit target) {
		return orders.isAttackOrderAllowed(target);
	}

	@Override
	public boolean isAttackOrderAllowed() {
		return orders.isAttackOrderAllowed();
	}

	@Override
	public boolean isMoveOrderAllowed() {
		return orders.isMoveOrderAllowed();
	}

	@Override
	public boolean isAttackMoveOrderAllowed() {
		return orders.isAttackMoveOrderAllowed();
	}

	@Override
	public boolean isRetreatOrderAllowed() {
		return orders.isRetreatOrderAllowed();
	}

	@Override
	public boolean isStopOrderAllowed() {
		return orders.isStopOrderAllowed();
	}

	@Override
	public boolean isUseSkillAllowed() {
		return orders.isUseSkillAllowed();
	}

	/**
	 * Returns the conditions which are shared by most ordinary orders
	 * 
	 * @return
	 */
	@Override
	public boolean isGeneralOrderAllowed() {
		return orders.isGeneralOrderAllowed();
	}

	/**
	 * Returns the conditions which are shared by all orders
	 * 
	 * @return
	 */
	@Override
	public boolean isAnyOrderAllowed() {
		return orders.isAnyOrderAllowed();
	}

	@Override
	public boolean isAnyOrderAllowedIgnoringTaunt() {
		return orders.isAnyOrderAllowedIgnoringTaunt();
	}

	// @formatter:on

	/**
	 * Clears any current order on this unit. For internal use.
	 */
	void clearOrder() {
		orders.clearOrder();
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

		getMotion().tick();
		stats.tick();

		orders.tick();

		

		if (activeSkill != null)
			activeSkill.tick();

		tickSkillCooldowns();
		
	}

	private void tickSkillCooldowns() {
		if (skillCooldownRemaining > 0) {
			skillCooldownRemaining -= 1;
		}
		// tick individual skill Cooldowns too
		skills.tickSkillCooldowns();
	}



	@Override
	public float getRetreatCooldownRemaining() {
		return combat.getRetreatCooldownRemaining();
	}

	public boolean areSkillsOnCooldown() {
		return (skillCooldownRemaining > 0);
	}



	public void tickAttacking() {
		combat.tick();
	}
	



	// Debug
	private static int getNextId() {
		return curId++;
	}

	// Debug Rendering
	public void renderAttackingArrow() {
		if (isAttacking()) {
			HoloGL.renderArrow(this, getAttacking(), Color.RED);
		}
	}

	void unitDies() {
		getMotion().stopCurrentMovement();
		this.clearOrder();

		// Stop this (now-dead) unit from attacking
		if (combat.isAttacking()) {
			combat.stopAttacking();
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
		return getMotion().getPath();
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
		return orders.getOrder();
	}

	@Override
	public Unit getOrderTarget() {
		return orders.getOrderTarget();
	}

	@Override
	public boolean isAttacking() {
		return combat.isAttacking();
	}

	@Override
	public boolean isAttacking(UnitInfo target) {
		return combat.isAttacking(target);
	}

	@Override
	public Unit getAttacking() {
		return combat.getAttacking();
	}

	public boolean isCasting() {
		return this.activeSkill != null && activeSkill.getStatus() == Status.CASTING;
	}

	public boolean isChannelling() {
		return this.activeSkill != null && activeSkill.getStatus() == Status.CHANNELING;
	}

	@Override
	public boolean isBusyRetreating() {
		return orders.isBusyRetreating();
	}

	@Override
	public MapInstanceInfo getMapInstance() {
		return mapInstance;
	}

	/**
	 * Some classes only get a reference to WorldInfo because they are not intended to modify the world.
	 * This method can be used to explicitly get a mutable World instance.
	 * 
	 * @return
	 */
	public MapInstance getMapInstanceMutable() {
		return (MapInstance) mapInstance;
	}

	public Set<Unit> getUnitsAttackingThis() {
		return mapInstance.getUnitsAttackingThis(this);
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
		return orders.getOrder() == Order.NONE && !isAttacking() && !isCastingOrChanneling();
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
		return combat.getAttackCooldown();
	}

	@Override
	public float getAttackCooldownRemaining() {
		return combat.getAttackCooldownRemaining();
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

	/**
	 * Only use for special cases, like a taunt ability. Uses startAttacking().
	 */
	public void setAttacking(Unit unit) {
		combat.setAttacking(unit);
	}

}
