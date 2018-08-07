package com.mygdx.holowyth.unit;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.combatDemo.WorldInfo;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.unit.Unit.Order;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloGL;
import com.mygdx.holowyth.util.data.Point;
import com.mygdx.holowyth.util.debug.DebugValues;

public class UnitMotion {

	private static float waypointMinDistance = 0.01f;

	// Attack Movement
	private static int attackPathfindingInterval = 30;
	private int framesUntilAttackRepath = attackPathfindingInterval;

	// Normal Movement

	private static float defaultUnitMoveSpeed = Holo.defaultUnitMoveSpeed;

	public float vx, vy;

	// Default movement values
	private static final float defaultTargetFinalSpeed = 0.2f;
	private static final float defaultDecelRate = 0.02f;

	private final static float defaultStartingSpeed = Holo.defaultUnitMoveSpeed * 0.4f;
	private final static float defaultAccelRate = 0.01f;

	// Accel/Decel shared variables

	/**
	 * The speed the unit is planning to travel towards a waypoint. Corresponds to acceleration and deceleration.
	 */
	private float curPlannedSpeed;
	private float maxAccelFactor = 5;

	/**
	 * The variables below all depend on speed and are modified by {@link UnitMotion#setSpeed(float)}
	 */
	private float speed = defaultUnitMoveSpeed;
	{
		setSpeed(speed);
	}

	private float startingSpeed;
	private float accelRate;
	private float targetFinalSpeed; // speed we want the unit to reach the goal with.
	private float decelRate;

	// Path variables

	private Path path;
	private int waypointIndex;

	// Knockback

	private KnockBack knockback = null;

	// Application References

	Unit self;
	ArrayList<Unit> units;
	private PathingModule pathing;

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

	public void pathForAttackingUnit() {
		// find path as normal, except for pathing ignore the target's collision
		// body
		ArrayList<Unit> someUnits = new ArrayList<Unit>(units);
		someUnits.remove(self.target);

		Path path = pathing.findPathForUnit(self, self.target.x, self.target.y, someUnits);
		if (path != null) {
			this.setPath(path);
		}
	}

	/**
	 * 
	 * @param dx
	 * @param dy
	 * @return true if the move order was successful
	 */
	public boolean orderMove(float dx, float dy) {
		Path path = pathing.findPathForUnit(self, dx, dy, units);
		if (path != null) {
			setPath(path);
			return true;
		}
		return false;
	}

	public void stopCurrentMovement() {
		clearPath();
	}

	/**
	 * Repath regularly if unit is ordered to attack but is not attacking yet
	 */
	private void handleRepathing() {
		if (self.currentOrder == Order.ATTACKUNIT && self.attacking == null) {
			framesUntilAttackRepath -= 1;
			if (framesUntilAttackRepath <= 0) {
				pathForAttackingUnit();
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

		if (knockback != null) {
			knockback.tick();
			return;
		}

		switch (self.currentOrder) {
		case MOVE:
			determineMovementForMoveOrder();
		case RETREAT:
			determineMovementForMoveOrder();
		case ATTACKUNIT:
			determineMovementForAttackOrder();
		default:
			break;
		}
	}

	private void determineMovementForMoveOrder() {

		if (path != null) {
			// Apply acceleration if the unit is not already at full speed.
			// Acceleration is proportional to the current speed of the unit,
			// but is capped at maxAccelFactor times the base rate.
			if (curPlannedSpeed < speed && !isDecelerating) {
				curPlannedSpeed = Math.min(curPlannedSpeed + Math.min(accelRate / curPlannedSpeed,
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
				curPlannedSpeed = getSpeedApproachingGoal(dist);
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
			if (dist > curPlannedSpeed) {
				float sin = dy / dist;
				float cos = dx / dist;

				this.vx = cos * curPlannedSpeed;
				this.vy = sin * curPlannedSpeed;
			} else {
				this.vx = dx;
				this.vy = dy;
			}

		} else {
			vx = 0;
			vy = 0;
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

	private void determineMovementForAttackOrder() {
		if (path != null) {
			determineMovementForMoveOrder();
			// TODO:
			// Can add extra code this so that units don't enter too deeply into the engage range. The math is simple
			// but the fact that the units can't be aware of the other's movement makes it non-trivial

		}
	}

	// Unit Movement

	/**
	 * When a unit is given a move command that is in a similar direction then it is already travelling, it doesn't need
	 * to slow down
	 * 
	 * Looks at the unit's current speed and next waypoint.
	 */
	private float getInitialSpeed() {

		if (curPlannedSpeed < startingSpeed || curPlannedSpeed < 0.001) {
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

		float s = Math.min(startingSpeed + curPlannedSpeed * cross, curPlannedSpeed);
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

		if (curPlannedSpeed <= targetFinalSpeed) {
			return curPlannedSpeed;
		}
		distanceToDecel = 0;
		float dSpeed = 0; // is negative

		// sum up distance required to achieve desired speed
		for (float s = curPlannedSpeed; s > targetFinalSpeed; s -= decelRate * (1 / s)) {
			distanceToDecel += s - decelRate / s;
		}
		dSpeed = -1 * decelRate * curPlannedSpeed;

		// System.out.format("curSpeed %s, Time to Decel %s, Distance %s, Decel dist %s, %n", curSpeed, timeToDecel,
		// distanceToGoal, distanceToDecel);

		if (distanceToGoal < distanceToDecel) {
			// System.out.println("Decelling");
			curPlannedSpeed = Math.max(curPlannedSpeed + dSpeed, targetFinalSpeed);
			isDecelerating = true;
		}

		return curPlannedSpeed;
	}

	/**
	 * @param path
	 *            should not be null
	 */
	private void setPath(Path path) {
		this.path = path;
		waypointIndex = 0;
		curPlannedSpeed = getInitialSpeed();
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
	public void setSpeed(float speed) {
		this.speed = speed;
		this.accelRate = defaultAccelRate * (speed / defaultUnitMoveSpeed);
		this.decelRate = defaultDecelRate * (speed / defaultUnitMoveSpeed);

		this.startingSpeed = defaultStartingSpeed * (speed / defaultUnitMoveSpeed);
		this.targetFinalSpeed = defaultTargetFinalSpeed * (float) Math.pow((speed / defaultUnitMoveSpeed), 1.4);

	}

	/**
	 * Returns the velocity, which is the actual speed the unit is trying to travel. Contrast to "speed", which is how
	 * fast the unit can move
	 * 
	 * @return
	 */
	public float getVelocity() {
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
			orderMove(getDest().x, getDest().y);
		case RETREAT:
			orderMove(getDest().x, getDest().y);
		case ATTACKUNIT:
			determineMovementForAttackOrder();
		default:
			break;
		}
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
		return curPlannedSpeed;
	}

	public void applyKnockBack(float direction, float initSpeed) {
		this.knockback = new KnockBack(direction, initSpeed);
	}

	public class KnockBack {

		private float curSpeed;
		private float direction;

		private int initialPeriod = 10;
		private int initialCounter = initialPeriod;
		private static final float decelRate = 0.1f;

		KnockBack(float direction, float initSpeed) {
			this.direction = direction;
			this.curSpeed = initSpeed;
		}

		public void tick() {

			float actualDecelRate;
			if (initialCounter > 0) {
				actualDecelRate = decelRate / 4;
				initialCounter -= 1;
			} else {
				actualDecelRate = decelRate;
			}

			UnitMotion.this.vx = curSpeed * (float) Math.cos(direction / 180 * (2 * Math.PI));
			UnitMotion.this.vy = curSpeed * (float) Math.sin(direction / 180 * (2 * Math.PI));

			System.out.println("knockback velocity: " + getVelocity());

			curSpeed -= actualDecelRate;

			if (curSpeed < 0.001f) {
				System.out.println("knockback completed");
				UnitMotion.this.vx = 0;
				UnitMotion.this.vy = 0;
				UnitMotion.this.knockback = null;
			}

		}
	}

}
