package com.mygdx.holowyth.unit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.combatDemo.WorldInfo;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.pathfinding.UnitInterPF;
import com.mygdx.holowyth.unit.Unit.Order;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloGL;
import com.mygdx.holowyth.util.data.Point;
import com.mygdx.holowyth.util.data.Segment;

/**
 * 
 * Implements commands like move, attackMove, attack target unit. <br>
 * Also controls automatic behavior of units.
 * 
 * @author Colin Ta
 *
 */
public class Unit implements UnitInterPF, UnitInfo {

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

	// Orders
	Order currentOrder = Order.IDLE;
	Unit target; // target for the current command.

	// Combat
	Unit attacking;
	/**
	 * Maps from a unit to a list of units attacking that one unit.
	 */
	static Map<Unit, Set<Unit>> unitsAttacking = new HashMap<Unit, Set<Unit>>();
	Mode mode = Mode.PASSIVE;
	
	/**
	 * A unit's friendly/foe status.
	 */
	Side side;

	private WorldInfo world;

	public enum Side { // for now, simple, two forces
		PLAYER, ENEMY
	}

	public enum Order {
		MOVE, ATTACKUNIT, IDLE, ATTACKMOVE
	}

	public enum Mode {
		ENGAGE, PASSIVE
	}

	public Unit(float x, float y, WorldInfo world, Side side) {
		this.ID = Unit.getNextId();
		this.x = x;
		this.y = y;
		this.side = side;

		// get neccesary references
		this.units = world.getUnits();

		Unit.unitsAttacking.put(this, new HashSet<Unit>());

		this.motion = new UnitMotion(this, world);
		this.world = world;
		
		this.stats = new UnitStats(this);
		
		
	}
	
	public Unit(float x, float y, WorldInfo world, Side side, String name) {
		this(x, y, world, side);
		setName(name);
	}

	
	public void setName(String name) {
		this.stats.setName(name);
	}

	// Orders

	public void orderMove(float dx, float dy) {
		if (!isMoveOrderAllowed()) {
			return;
		}
		if (motion.orderMove(dx, dy)) {
			clearOrder();
			currentOrder = Order.MOVE;
		}
	}

	public void orderAttackUnit(Unit unit) {
		if (!isAttackOrderAllowed(unit)) {
			return;
		}
		if (unit == this) {
			System.out.println("Warning: invalid attack command (unit can't attack itself)");
			return;
		}
		clearOrder();

		this.currentOrder = Order.ATTACKUNIT;
		this.target = unit;

		this.attacking = null; // assuming this is a valid order to make, the unit is now no longer attacking anything
								// yet

		this.motion.pathForAttackingUnit();
	}

	public void orderAttackMove(float x, float y) {
		if (!isAttackMoveOrderAllowed()) {
			return;
		}
		// TODO:

	}

	/** 
	 * Clears any current order on this unit. For internal use.
	 */
	void clearOrder() {
		currentOrder = Order.IDLE;
		target = null;
	}

	// Combat

	/**
	 * Main function, also is where the unit determines its movement
	 */
	public void handleGeneralLogic() {
		motion.tick();
		makeIdleEnemiesAggro();
	
	}

	
	private int attackCooldown = 60; 
	private int attackCooldownLeft = 0;
	
	/** Handles the combat logic for a unit for one frame */
	public void handleCombatLogic() {

		if(attacking != null && attacking.stats.isDead()) {
			stopAttacking();
		}
		
		if (currentOrder == Order.ATTACKUNIT) {
			// If the unit is on an attackUnit command and its in engage range, make it start attacking the target
			// If the unit falls out of engage range, stop it from attacking
			
			
			
			if(target.stats.isDead()) {
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
				if(attackCooldownLeft <= 0) {
					this.attack(attacking);
					attackCooldownLeft = attackCooldown;
				}else {
					attackCooldownLeft -=1;
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
	
	private void attack(Unit enemy) {
		this.stats.attack(enemy.stats);
	}

	private void startAttacking(Unit target) {
		System.out.println("started attacking");
		motion.stopCurrentMovement();
		attacking = target;
		unitsAttacking.get(attacking).add(this);
		
		attackCooldownLeft = attackCooldown / 2;
	}

	private void stopAttacking() {
		unitsAttacking.get(this.attacking).remove(this);
		attacking = null;
	}

	private boolean isAttacking() {
		return attacking != null;
	}


	private boolean isAttackOrderAllowed(Unit target) {
		if(stats.isDead())
			return false;
		if (attacking != null)
			return false;
		if (target.side == this.side) {
			return false;
		}
		return true;
	}

	private boolean isMoveOrderAllowed() {
		if(stats.isDead())
			return false;
		if (attacking != null)
			return false;
		return true;
	}

	private boolean isAttackMoveOrderAllowed() {
		if(stats.isDead())
			return false;
		return isMoveOrderAllowed();
	}

	
	// Automatic Unit Behaviour
	
	/**
	 * Makes this unit aggro to the closest target if it is an enemy faction unit
	 */
	private void makeIdleEnemiesAggro() {
		if (this.side == Side.ENEMY && currentOrder == Order.IDLE) {

			PriorityQueue<Unit> closestTargets = new PriorityQueue<Unit>(closestUnitComp);
			for (Unit u : units) {
				if (u == this)
					continue;
				if (u.side != Side.ENEMY) {
					closestTargets.add(u);
				}
			}
			if (!closestTargets.isEmpty()) {
				Unit closestEnemy = closestTargets.remove();
				if (getDist(this, closestEnemy) <= Holo.idleAggroRange) {
					this.orderAttackUnit(closestEnemy);
				}
			}
		}
	}

	
	// Debug
	private static int getNextId() {
		return curId++;
	}

	// Debug Rendering
	public void renderAttackingLine(ShapeRenderer shapeRenderer) {
		if (this.attacking != null) {
	
			Color arrowColor = Color.RED;
			float wingLength = 8f;
			float arrowAngle = 30f;
	
			// draw a line from the center of this unit to the edge of the other unit
			float len = new Segment(this.getPos(), attacking.getPos()).getLength();
			float newLen = len - attacking.radius * 0.35f;
			float dx = attacking.x - this.x;
			float dy = attacking.y - this.y;
			float ratio = newLen / len;
			float nx = dx * ratio;
			float ny = dy * ratio;
			Point edgePoint = new Point(x + nx, y + ny);
			Segment s = new Segment(this.getPos(), edgePoint);
	
			HoloGL.renderSegment(s, shapeRenderer, arrowColor);
	
			// Draw the arrow wings
	
			// calculate angle of the main arrow line
			float angle = (float) Math.acos(dx / len);
			if (dy < 0) {
				angle = (float) (2 * Math.PI - angle);
			}
	
			float backwardsAngle = (float) (angle + Math.PI);
	
			// draw a line in the +x direction, then rotate it and transform it as needed.
	
			Segment wingSeg = new Segment(0, 0, wingLength, 0); // create the base wing segment
	
			shapeRenderer.identity();
			shapeRenderer.translate(edgePoint.x, edgePoint.y, 0);
			shapeRenderer.rotate(0, 0, 1, (float) Math.toDegrees(backwardsAngle) + arrowAngle);
	
			HoloGL.renderSegment(wingSeg, shapeRenderer, arrowColor);
	
			shapeRenderer.identity();
			shapeRenderer.translate(edgePoint.x, edgePoint.y, 0);
			shapeRenderer.rotate(0, 0, 1, (float) Math.toDegrees(backwardsAngle) - arrowAngle);
	
			HoloGL.renderSegment(wingSeg, shapeRenderer, arrowColor);
	
			shapeRenderer.identity();
	
		}
	}

	void unitDies() {
		
		motion.stopCurrentMovement();
		this.clearOrder();
		System.out.println("unit died");
		
		// Stop this (now-dead) unit from attacking
		if(attacking != null) {
			stopAttacking();
		}
	}


	// For now we allow multiple player characters
	public boolean isPlayerCharacter() {
		return side == Side.PLAYER;
	}



	public String toString() {
		return String.format("Unit[ID: %s]", this.ID);

	}
	
	// Convenience functions
	public static float getDist(Unit u1, Unit u2) {
		return Point.calcDistance(u1.getPos(), u2.getPos());
	}

	// Tools
	private Comparator<Unit> closestUnitComp = (Unit u1, Unit u2) -> {
		if (Point.calcDistanceSqr(this.getPos(), u1.getPos())
				- Point.calcDistanceSqr(this.getPos(), u2.getPos()) >= 0) {
			return -1;
		} else {
			return 1;
		}
	};

	// Getters
	public float getRadius() {
		return radius;
	}

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

	public Side getSide() {
		return side;
	}

	public UnitStatsInfo getStats() {
		return stats;
	}

	public Order getCurrentOrder() {
		return currentOrder;
	}

	public UnitInfo getTarget() {
		return target;
	}

	public UnitInfo getAttacking() {
		return attacking;
	}

	public WorldInfo getWorld() {
		return world;
	}
	
}
