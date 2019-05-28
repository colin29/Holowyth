package com.mygdx.holowyth.unit;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.combatDemo.WorldInfo;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.unit.Unit.Order;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.tools.debugstore.DebugValues;

public class UnitMotion {

	private static float waypointMinDistance = 0.01f;

	// Attack Movement
	private static int attackPathfindingInterval = 30;
	private int framesUntilAttackRepath = attackPathfindingInterval;

	// Normal Movement

	private static float defaultUnitMoveSpeed = Holo.defaultUnitMoveSpeed;

	private float vx, vy;
	// Default movement values
	private static final float defaultTargetFinalSpeed = 0.2f;
	private static final float defaultDecelRate = 0.02f;

	// Accel/Decel shared variables
	private final static float defaultStartingSpeedRatio = 0.4f;
	private final static float defaultStartingSpeed = Holo.defaultUnitMoveSpeed * defaultStartingSpeedRatio;
	private final static float defaultAccelRate = 0.01f;

	/**
	 * The speed the unit is planning to travel towards a waypoint. Corresponds to acceleration and deceleration.
	 */
	private float plannedSpeed;
	private float maxAccelFactor = 5;

	/**
	 * The variables below all depend on speed and are modified by {@link UnitMotion#setSpeedAndRelatedVars(float)}
	 */
	private float speed;
	{
		setSpeedAndRelatedVars(defaultUnitMoveSpeed);
	}

	private float startingSpeed;
	private float accelRate;
	private float targetFinalSpeed; // speed we want the unit to reach the goal with.
	private float decelRate;

	// Path variables

	private Path path;
	private int waypointIndex;

	// Application References

	Unit self;
	List<Unit> units;
	private PathingModule pathing;

	// Knockback variables
	private float knockBackVx;
	private float knockBackVy;
	private boolean isBeingKnockedBack = false;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * This class merely sets planned unit velocities, it is up to the World class to accept/resolve movements
	 * 
	 * @param self
	 * @param world
	 */
	UnitMotion(Unit self, WorldInfo world) {
		this.self = self;
		units = world.getUnits();
		pathing = world.getPathingModule();

		if (self.side == Side.PLAYER) {

			System.out.println(self.getWorldMutable().getDebugStore());

			@SuppressWarnings("unused")
			DebugValues debugValues = self.getWorldMutable().getDebugStore().registerComponent("Player unit Motion");
			// debugValues.add("Distance to nextWayPoint", () ->
			// DataUtil.getRoundedString(getDistanceToNextWayPoint()));
		}
	}

	public void tick() {
		handleRepathing();
		determineMovement();
	}

	/**
	 * Looks for a path in a manner suitable for a move order. If it finds one, the unit's motion adopts that path
	 * 
	 * Since a unitMotion will periodically repath for a unit on attack order, even if the initial path fails, this
	 * method does not report a failure.
	 */
	public void pathFindForAttackOrder() {
		// find path as normal, except for pathing ignore the target's collision
		// body
		ArrayList<Unit> someUnits = new ArrayList<Unit>(units);
		someUnits.remove(self.target);

		Path newPath = pathing.findPathForUnit(self, self.target.x, self.target.y, someUnits);
		if (newPath != null) {
			this.setPath(newPath);
		}
	}

	/**
	 * Looks for a path in a manner suitable for a move order. If it finds one, the unit's motion adopts that path. If
	 * it doesn't, nothing happens
	 * 
	 * @param dx
	 * @param dy
	 * @return true if pathfind was successful and unitMotion has adopted that path. False if not.
	 */
	public boolean pathFindForMoveOrder(float dx, float dy) {
		Path path = pathing.findPathForUnit(self, dx, dy, units);
		if (path != null) {
			setPath(path);
			return true;
		}
		return false;
	}

	/**
	 * Stop the current normal movement. This applies to normal voluntary movement, and does not affect knockback
	 * motion. It is valid to give when a unit is being knocked back, though the unit's normal movement should already
	 * be cleared
	 */
	public void stopCurrentMovement() {
		clearPath();
	}

	/**
	 * Repath regularly if unit is ordered to attack but is not attacking yet
	 */
	private void handleRepathing() {
		if (self.currentOrder == Order.ATTACKUNIT && self.isAttacking()) {
			framesUntilAttackRepath -= 1;
			if (framesUntilAttackRepath <= 0) {
				pathFindForAttackOrder();
				framesUntilAttackRepath = attackPathfindingInterval;
			}
		}

		// TODO:
		// if(this.currentOrder == Order.MOVE){
		// framesUntilAttackRepath -= 1;
		// if (framesUntilAttackRepath <= 0) {
		// pathForAttackingUnit();
		// framesUntilAttackRepath = attackPathfindingInterval;
		// }
		// }
	}

	/**
	 * Sets the velocity for the unit based on the unit's path. Also accounts for accel+decel at the begin and end of
	 * movement. When movement is complete, a unit's path is set to null.
	 */
	private void determineMovement() {
		if (path == null) {
			vx = 0;
			vy = 0;
			return;
		}
		switch (self.currentOrder) {
		case MOVE:
			determineMovementForMoveOrder();
			break;
		case RETREAT:
			determineMovementForMoveOrder();
			break;
		case ATTACKUNIT:
			determineMovementForAttackOrder();
			break;
		default:
			break;
		}
	}

	/**
	 * Determines the unit's velocity based on path (and related state variables). Path must not be null
	 */
	private void determineMovementForMoveOrder() {

		// Apply acceleration if the unit is not already at full speed.
		// Acceleration is proportional to the current speed of the unit,
		// but is capped at maxAccelFactor times the base rate.
		if (plannedSpeed < speed && !isDecelerating) {
			plannedSpeed = Math.min(plannedSpeed + Math.min(accelRate / plannedSpeed,
					accelRate * maxAccelFactor), speed);
			// curSpeed = Math.min(curSpeed + (speed-curSpeed)*factorAccel,
			// speed);
		}

		// System.out.println("CurSpeed: " + curSpeed);

		Point curWaypoint = path.get(waypointIndex);

		float dist = getDistanceToNextWayPoint();

		float wx = curWaypoint.x;
		float wy = curWaypoint.y;
		float dx = wx - self.x;
		float dy = wy - self.y;

		// Apply deceleration if the unit is approaching the final goal

		if (waypointIndex == path.size() - 1) {
			plannedSpeed = getSpeedApproachingGoal(dist);
		}

		// Check if reached waypoint
		if (dist < waypointMinDistance) {
			waypointIndex += 1;
			// check if completed path
			if (waypointIndex == path.size()) {
				self.clearOrder();
				clearPath();
			}
		}

		// Determine unit movement
		if (dist > plannedSpeed) {
			float sin = dy / dist;
			float cos = dx / dist;

			this.vx = cos * plannedSpeed;
			this.vy = sin * plannedSpeed;
		} else {
			this.vx = dx;
			this.vy = dy;
		}

	}

	/**
	 * 
	 * @return distance to nextWayPoint if path is active, otherwise -1
	 */
	private float getDistanceToNextWayPoint() {
		if (path != null) {
			Point curWaypoint = path.get(waypointIndex);
			float wx = curWaypoint.x;
			float wy = curWaypoint.y;

			float dx = wx - self.x;
			float dy = wy - self.y;

			return (float) Math.sqrt(dx * dx + dy * dy);
		} else {
			return -1;
		}
	}

	/**
	 * Path must not be null
	 */
	private void determineMovementForAttackOrder() {
		determineMovementForMoveOrder();
		// TODO:
		// Can add extra code this so that units don't enter too deeply into the engage range. The math is simple
		// but the fact that the units can't be aware of the other's movement makes it non-trivial

	}

	// Unit Movement

	/**
	 * When a unit is given a move command that is in a similar direction then it is already travelling, it doesn't need
	 * to slow down
	 * 
	 * Looks at the unit's current speed and next waypoint.
	 */
	private float getInitialSpeed() {

		if (plannedSpeed < startingSpeed || plannedSpeed < 0.001) {
			return startingSpeed;
		}

		Point waypoint = path.get(1);
		Vector2 v = new Vector2(vx, vy);
		Vector2 p = new Vector2(waypoint.x - self.x, waypoint.y - self.y);

		if (p.len2() < 0.001) {
			return startingSpeed;
		}

		v.nor();
		p.nor();
		float cross = v.dot(p);

		float s = Math.min(startingSpeed + plannedSpeed * cross, plannedSpeed);
		s = Math.max(s, startingSpeed);
		// System.out.println("initial move speed: " + s);

		return s;

	}

	private float distanceToDecel;
	private boolean isDecelerating = false;

	/**
	 * Is called to determine when to decelerate when the unit arrives at the goal.
	 * 
	 * @return
	 */
	private float getSpeedApproachingGoal(float distanceToGoal) {

		if (plannedSpeed <= targetFinalSpeed) {
			return plannedSpeed;
		}
		distanceToDecel = 0;
		float dSpeed = 0; // is negative

		// sum up distance required to achieve desired speed
		for (float s = plannedSpeed; s > targetFinalSpeed; s -= decelRate * (1 / s)) {
			distanceToDecel += s - decelRate / s;
		}
		dSpeed = -1 * decelRate * plannedSpeed;

		// System.out.format("curSpeed %s, Time to Decel %s, Distance %s, Decel dist %s, %n", curSpeed, timeToDecel,
		// distanceToGoal, distanceToDecel);

		if (distanceToGoal < distanceToDecel) {
			// System.out.println("Decelling");
			plannedSpeed = Math.max(plannedSpeed + dSpeed, targetFinalSpeed);
			isDecelerating = true;
		}

		return plannedSpeed;
	}

	/**
	 * @param path
	 *            should not be null
	 */
	private void setPath(Path path) {
		this.path = path;
		waypointIndex = 0;
		plannedSpeed = getInitialSpeed();
		isDecelerating = false;
	}

	private void clearPath() {
		this.path = null;
		waypointIndex = -1;
		vx = 0;
		vy = 0;
	}

	public void renderNextWayPoint() {
		if (path != null) {
			Point p = path.get(waypointIndex);
			HoloGL.renderCircle(p.x, p.y, Color.FIREBRICK);
		}
	}

	public Path getPath() {
		return this.path;
	}

	/**
	 * Also scales accel/decel variables proportionately to speed
	 * 
	 * @param speed
	 */
	public void setSpeedAndRelatedVars(float speed) {
		this.speed = speed;
		this.accelRate = defaultAccelRate * (speed / defaultUnitMoveSpeed);
		this.decelRate = defaultDecelRate * (speed / defaultUnitMoveSpeed);

		this.startingSpeed = speed * defaultStartingSpeedRatio;
		this.targetFinalSpeed = defaultTargetFinalSpeed * (float) Math.pow((speed / defaultUnitMoveSpeed), 1.6);

	}

	/**
	 * Velocity refers to the same thing as vx and vy, that is the current speed of the unit
	 * 
	 * @return
	 */
	public float getVelocityMagnitude() {
		return (float) Math.sqrt(vx * vx + vy * vy);
	}

	/**
	 * Called by world when the pathing unit fails to make progress (even with the push out algorithm). Generally this
	 * will make the unit repath/ reattack
	 * 
	 * @return
	 */
	public void onBlocked() {
		System.out.println("onBlocked called");
		switch (self.currentOrder) {
		case MOVE:
			pathFindForMoveOrder(getDest().x, getDest().y);
		case RETREAT:
			pathFindForMoveOrder(getDest().x, getDest().y);
		case ATTACKUNIT:
			determineMovementForAttackOrder();
		default:
			break;
		}
	}

	public boolean isBeingKnockedBack() {
		return isBeingKnockedBack;
	}

	public void beginKnockback(float initialVx, float initialVy) {
		if (isBeingKnockedBack) {
			logger.warn("beginKnockback() called but unit is already in knockback state. Skipping.");
			return;
		}
		isBeingKnockedBack = true;
		knockBackVx = initialVx;
		knockBackVy = initialVy;

		stopCurrentMovement();
		self.clearOrder();
		self.stopAttacking();

	}

	public void endKnockback() {
		if (!isBeingKnockedBack) {
			logger.warn("endKnockback called, but unit is not being knocked back. No effect.");
			return;
		}
		knockBackVx = 0;
		knockBackVy = 0;
		isBeingKnockedBack = false;
	}

	/**
	 * If the unit is not in knockback state, sets it to knockback state with this velocity. If it is already being
	 * knocked back, adds this to its velocity.
	 */
	public void applyKnockBackVelocity(float dx, float dy) {
		if (isBeingKnockedBack) {
			setKnockbackVelocity(knockBackVx + dx, knockBackVy + dy);
		} else {
			beginKnockback(dx, dy);
		}

	}

	public void setKnockbackVx(float knockBackVx) {
		if (this.isBeingKnockedBack) {
			this.knockBackVx = knockBackVx;
		} else {
			logger.warn(
					"setKnockbackVx, but unit is not in knockbackState. This would suggest bad logic. Ignoring set");
		}
	}

	public void setKnockbackVy(float knockBackVy) {
		if (this.isBeingKnockedBack) {
			this.knockBackVy = knockBackVy;
		} else {
			logger.warn(
					"setKnockbackVy, but unit is not in knockbackState. This would suggest bad logic. Ignoring set");
		}
	}

	public void setKnockbackVelocity(float knockBackVx, float knockBackVy) {
		setKnockbackVx(knockBackVx);
		setKnockbackVy(knockBackVy);
	}

	public Point getDest() {
		if (path == null)
			return null;
		return path.get(path.size() - 1);
	}

	public int getWayPointIndex() {
		return waypointIndex;
	}

	public float getCurPlannedSpeed() {
		return plannedSpeed;
	}

	public float getKnockBackVx() {
		return knockBackVx;
	}

	public float getKnockBackVy() {
		return knockBackVy;
	}

	public Vector2 getKnockBackVelocity() {
		return new Vector2(knockBackVx, knockBackVy);
	}

	public float getVx() {
		return vx;
	}

	public float getVy() {
		return vy;
	}

	public Vector2 getVelocity() {
		return new Vector2(vx, vy);
	}

}
