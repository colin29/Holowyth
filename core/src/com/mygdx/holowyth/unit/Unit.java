package com.mygdx.holowyth.unit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.ai.UnitAI;
import com.mygdx.holowyth.game.MapInstance;
import com.mygdx.holowyth.game.MapInstanceInfo;
import com.mygdx.holowyth.game.session.OwnedItems;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.UnitPFWithPath;
import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.skill.ActiveSkill.Status;
import com.mygdx.holowyth.skill.skill.GroundSkill;
import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.skill.skill.UnitSkill;
import com.mygdx.holowyth.unit.UnitOrders.Order;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.unit.interfaces.UnitStatsInfo;
import com.mygdx.holowyth.unit.interfaces.UnitStatusInfo;
import com.mygdx.holowyth.unit.sprite.UnitGraphics;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.world.map.UnitMarker;

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
 * <b>orders.currentOrder</b> -- a unit with an order will continue trying to do something <br>
 * <b>combat.attacking</b> -- a unit attacking is locked in combat and will regularly attack their target
 * <br>
 * <b>skills.activeSkill</b> -- a unit with an active ability is either casting or channelling that ability
 * <br>
 * 
 * <b>motion.isBeingKnockedBack()</b> -- a unit being knocked back cannot be given new orders or
 * perform any action (it may retain its old order, depending on the type)
 */
public class Unit implements UnitPFWithPath, UnitInfo, UnitOrderable {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/** For debugging. Does not even require units be alive or in a MapInstance. */
	private static Map<Integer, Unit> idToUnit = new HashMap<Integer, Unit>();

	// Unit life-time Components
	public @NonNull final UnitStats stats;
	public @NonNull final UnitSkills skills;
	public @NonNull final UnitGraphics graphics;
	public @NonNull final UnitEquip equip;
	public @NonNull final UnitAI ai;
	public @NonNull final UnitStatus status;
	
	public enum JobClass {
		NONE, WARRIOR, SWORDSMAN, THIEF, MAGE, PRIEST, RANGER
	}
	private @NonNull JobClass jobClass = JobClass.NONE; 

	// Id (for debug)
	private static int curId = 0;
	private final int id;

	// Fields
	@NonNull Side side;
	private float radius = Holo.UNIT_RADIUS;

	/////////////// World specific state /////////////

	// Map life-time Components (are discarded when a unit moves to a new map)
	UnitMotion motion;
	UnitCombat combat;
	UnitOrders orders;

	private MapInstanceInfo mapInstance;
	public float x, y;
	
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


	public Unit(float x, float y, MapInstanceInfo world, @NonNull Side side, String name) {
		this(x, y, side, world);
		setName(name);
	}

	public Unit(float x, float y, @NonNull Side side, MapInstanceInfo world) {
		this.id = Unit.getNextId();
		idToUnit.put(id, this);

		this.x = x;
		this.y = y;
		this.side = side;
		this.mapInstance = world;

		// Init unit lifetime components before map-lifetime components, just so that the latter can acquire reference in constructor.
		stats = new UnitStats(this);
		status = new UnitStatus(this);
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
	public Unit(UnitMarker m, MapInstanceInfo mapInstance) {
		this(m.pos.x, m.pos.y, mapInstance, m.side, m.name);

		jobClass = m.jobClass;
		
		stats.base.set(m.baseStats);

		skills.addSkills(m.activeSkills);
		skills.addSkills(m.passiveSkills);
		skills.slotSkills(m.activeSkills);
		equip.equipAllFromTemplate(m.wornEquips);

		graphics.setAnimatedSprite(mapInstance.getAnimations().getSprite(m.animatedSpriteName));
		graphics.setHeadSpriteName(m.headSpriteName);
	}

	/** Specifically for units that have been initialized and removed from a world once */
	public void reinitializeForWorld(MapInstanceInfo world) {
		this.mapInstance = world;
		motion = new UnitMotion(this, world);
		combat = new UnitCombat(this);
		orders = new UnitOrders(this);
		stats.reinitializeForMapInstance();
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

	@Override
	public void orderMove(float x, float y) {
		orders.orderMove(x, y);
	}

	/**
	 * @see UnitOrders#orderAttackUnit(UnitOrderable)
	 */
	@Override
	public boolean orderAttackUnit(UnitOrderable unitOrd) {
		return orders.orderAttackUnit(unitOrd);
	}

	/**
	 * @see UnitOrders#orderAttackUnit(UnitOrderable, boolean)
	 */
	@Override
	public boolean orderAttackUnit(UnitOrderable unitOrd, boolean isHardOrder) {
		return orders.orderAttackUnit(unitOrd, isHardOrder);
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
	 * @see UnitOrders#orderStop()
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

	@Override
	public boolean orderAttackUnitQueueMeleeSkill(UnitOrderable unitOrd, @NonNull NoneSkill skill) {
		return orders.orderAttackUnitQueueMeleeSkill(unitOrd, skill);
	}
	@Override
	public void orderMoveInRangeToUseSkill(float x, float y, @NonNull GroundSkill skill) {
		orders.orderMoveInRangeToUseSkill(x, y, skill);
	}
	public void orderMoveInRangeToUseSkill(@NonNull UnitOrderable target, @NonNull UnitSkill skill) {
		orders.orderMoveInRangeToUseSkill(target, skill);
	}
	@Override
	public void orderMoveToUnit(@NonNull UnitOrderable unit) {
		orders.orderMoveToUnit(unit);
	}

	// @formatter:off
	@Override
	public boolean isAttackOrderAllowed(UnitOrderable target) {
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

	@Override
	public boolean isBusyRetreating() {
		return orders.isBusyRetreating();
	}

	/**
	 * Not doing any action nor has any order assigned
	 */
	@Override
	public boolean isCompletelyIdle() {
		return orders.getOrder() == Order.NONE && !isAttacking() && !isCastingOrChanneling();
	}

	@Override
	public Order getOrder() {
		return orders.getOrder();
	}

	@Override
	public UnitOrderable getOrderTarget() {
		return orders.getOrderTarget();
	}

	@Override
	public float getRetreatCooldownRemaining() {
		return combat.getRetreatCooldownRemaining();
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

	@Override
	public float getAttackCooldown() {
		return stats.getAttackCooldown();
	}

	@Override
	public float getAttackCooldownRemaining() {
		return combat.getAttackCooldownRemaining();
	}

	@Override
	public ActiveSkill getActiveSkill() {
		return skills.getActiveSkill();
	}

	@Override
	public boolean isCastingOrChanneling() {
		if (getActiveSkill() == null)
			return false;
		return (getActiveSkill().getStatus() == Status.CASTING || getActiveSkill().getStatus() == Status.CHANNELING);
	}

	@Override
	public Path getPath() {
		return motion.getPath();
	}

	@Override
	public float getRadius() {
		return radius;
	}

	@Override
	public @NonNull Side getSide() {
		return side;
	}

	@Override
	public String getName() {
		return stats.getName();
	}

	@Override
	public boolean isAPlayerCharacter() {
		return side == Side.PLAYER;
	}

	@Override
	public boolean isEnemy(UnitInfo unit) {
		return getSide() != unit.getSide();
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
	public int getID() {
		return id;
	}

	@Override
	public boolean isDead() {
		return stats.isDead();
	}

	@Override
	public UnitStatsInfo getStats() {
		return stats;
	}

	@Override
	public UnitSkills getSkills() {
		return skills;
	}

	@Override
	public UnitAI getAI() {
		return ai;
	}

	@Override
	public UnitMotion getMotion() {
		return motion;
	}

	@Override
	public MapInstanceInfo getMapInstance() {
		return mapInstance;
	}

	/**
	 * @see UnitOrders#stopUnit()
	 */
	public void stopUnit() {
		orders.stopUnit();
	}

	

	// Tick Logic
	
	/**
	 * Only use for special cases, like a taunt ability. Uses startAttacking().
	 */
	public void setAttacking(Unit unit) {
		combat.setAttacking(unit);
	}

	/**
	 * See {@link UnitSkills#interruptSoft()}
	 */
	public void interruptSoft() {
		skills.interruptSoft();
	}

	public void interruptRangedSkills() {
		skills.interruptRangedSkills();
	}

	/**
	 * Hard interrupts are caused by stun / knockback
	 */
	public void interruptHard() {
		skills.interruptHard();
	}

	public void setActiveSkill(ActiveSkill activeSkill) {
		skills.setActiveSkill(activeSkill);
	}

	public void setName(String name) {
		this.stats.setName(name);
	}
	public void setPos(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Some classes only get a reference to WorldInfo because they are not intended to modify the world.
	 * This method can be used to explicitly get a mutable World reference.
	 */
	public MapInstance getMapInstanceMutable() {
		return (MapInstance) mapInstance;
	}
	
	public boolean isCasting() {
		return skills.isCasting();
	}

	public boolean isChannelling() {
		return skills.isChannelling();
	}

	public boolean isSkillsOnCooldown() {
		return skills.isSkillsOnCooldown();
	}

	/**
	 * Clears any current order on this unit. For internal use.
	 */
	void clearOrder() {
		orders.clearOrder();
	}

	void unitDies() {
		motion.stopCurrentMovement();
		this.clearOrder();
	
		// Stop this (now-dead) unit from attacking
		if (combat.isAttacking()) {
			combat.stopAttacking();
		}
		// Don't actually remove the unit here -- world will handle that
	}

	Set<Unit> getUnitsAttackingThis() {
		return mapInstance.getUnitsAttackingThis(this);
	}

	public void tick() {
		if (isDead())
			return;
	
		ai.tick();
	
		motion.tick();
		status.tick();
		orders.tick();
		skills.tick();

	}
	/**
	 * Called at a different timing than {@link unit#tick()}
	 */
	public void tickAttacking() {
		combat.tick();
	}



	@Override
	public String toString() {
		return String.format("Unit[ID: %s]", this.id);
	
	}

	////////// Debug Methods  ////////
	private static int getNextId() {
		return curId++;
	}

	/**
	 * For debug purposes. Gets any created unit, regardless of whether is in a {@link MapInstance}
	 */
	public static UnitOrderable getUnitByID(int id) {
		return idToUnit.get(id);
	}

	public static float dist(UnitOrderable u1, UnitOrderable u2) {
		return Point.dist(u1.getPos(), u2.getPos());
	}
	public boolean inRange(UnitOrderable other, float range) {
		return Point.dist(this.getPos(), other.getPos())  <= range;
	}

	@Override
	public UnitStatusInfo getStatus() {
		return status;
	}
	public void setInventory(@NonNull OwnedItems owned) {
		equip.getWornEquips().setInventory(owned);
	}
	
	public float getEngageRange(Unit unit) {
		return orders.getEngageRange(unit);
	}

	public UnitCombat getCombat() {
		return combat;
	}

	public static float getAngleInDegrees(UnitOrderable u1, UnitOrderable u2) {
		return Point.getAngleInDegrees(u1.getPos(), u2.getPos());
	}

	public JobClass getJobClass() {
		return jobClass;
	}

}
