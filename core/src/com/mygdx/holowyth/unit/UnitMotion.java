package com.mygdx.holowyth.unit;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.combatDemo.WorldInfo;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.unit.Unit.Order;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.util.DataUtil;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloGL;
import com.mygdx.holowyth.util.data.Point;
import com.mygdx.holowyth.util.debug.DebugValues;

public class UnitMotion {

	public static float waypointMinDistance = 0.01f;

	private static int attackPathfindingInterval = 30;
	private int framesUntilAttackRepath = attackPathfindingInterval;

	// Movement

	private static float defaultUnitMoveSpeed = Holo.defaultUnitMoveSpeed;

	public float vx, vy;
	private float speed = Holo.defaultUnitMoveSpeed;
	public float curSpeed;

	// These are the default values. They can be changed later at will.

	private static float defaultStartingSpeed = Holo.defaultUnitMoveSpeed * 0.4f;
	private float startingSpeed = defaultStartingSpeed;

	private static float defaultQuadraticAccelRate = 0.01f;
	public float quadAccelRate = 0.01f;
	private float maxAccelFactor = 5;

	public Path path;

	int waypointIndex;

	Unit self;
	ArrayList<Unit> units;
	private PathingModule pathing;

	/**
	 * This class merely sets unit velocities, it is up to the World class to accept/resolve movements
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

			DebugValues debugValues = self.getWorldMutable().getDebugStore().registerComponent("Player unit Motion");
			// debugValues.add("vx, )
			debugValues.add("Distance to decel", () -> DataUtil.getRoundedString(distanceToDecel));
			debugValues.add("Distance to nextWayPoint", () -> DataUtil.getRoundedString(getDistanceToNextWayPoint()));
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
			if (curSpeed < speed && !isDecelerating) {
				curSpeed = Math.min(curSpeed + Math.min(quadAccelRate / curSpeed,
						quadAccelRate * maxAccelFactor), speed);
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
				curSpeed = getSpeedApproachingGoal(dist);
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
			if (dist > curSpeed) {
				float sin = dy / dist;
				float cos = dx / dist;

				this.vx = cos * curSpeed;
				this.vy = sin * curSpeed;
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

		if (curSpeed < startingSpeed || curSpeed < 0.001) {
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

		float s = Math.min(startingSpeed + curSpeed * cross, curSpeed);
		s = Math.max(s, startingSpeed);
		// System.out.println("initial move speed: " + s);

		return s;

	}

	private static float defaultTargetFinalSpeed = 0.2f;
	private float targetFinalSpeed = defaultTargetFinalSpeed; // speed we want the unit to reach the goal with.

	public float linearDecelRate = 0.01f;

	private static float defaultQuadraticDecelRate = 0.02f;
	public float quadDecelRate = defaultQuadraticDecelRate;

	private int linearDecelEndTime = 0; // produces time on the end where the unit is moving at minimum speed
	private boolean isDecelerating = false;
	private static final boolean useQuadraticDecel = true;

	private float distanceToDecel;

	/**
	 * Is called to determine when to decelerate when the unit arrives at the goal.
	 * 
	 * @return
	 */
	private float getSpeedApproachingGoal(float distanceToGoal) {

		if (curSpeed <= targetFinalSpeed) {
			return curSpeed;
		}
		float timeToDecel = 0;
		distanceToDecel = 0;
		float dSpeed = 0; // is negative

		if (useQuadraticDecel) {
			// sum up distance required to achieve desired speed
			for (float s = curSpeed; s > targetFinalSpeed; s -= quadDecelRate * (1 / s)) {
				distanceToDecel += s - quadDecelRate / s;
			}
			dSpeed = -1 * quadDecelRate * curSpeed;

		} else { // use linear deceleration
			timeToDecel = (curSpeed - targetFinalSpeed) / linearDecelRate + linearDecelEndTime; // in frames
			distanceToDecel = (curSpeed + targetFinalSpeed) / 2f * timeToDecel;
			dSpeed = -linearDecelRate;
		}

		// System.out.format("curSpeed %s, Time to Decel %s, Distance %s, Decel dist %s, %n", curSpeed, timeToDecel,
		// distanceToGoal, distanceToDecel);

		if (distanceToGoal < distanceToDecel) {
			// System.out.println("Decelling");
			curSpeed = Math.max(curSpeed + dSpeed, targetFinalSpeed);
			isDecelerating = true;
		}

		return curSpeed;
	}

	/**
	 * @param path
	 *            should not be null
	 */
	private void setPath(Path path) {
		this.path = path;
		waypointIndex = 0;
		curSpeed = getInitialSpeed();
		isDecelerating = false;
	}

	private void clearPath() {
		this.path = null;
		waypointIndex = -1;
		vx = 0;
		vy = 0;
	}

	public void renderNextWayPoint(ShapeRenderer shapeRenderer) {
		if (path != null) {
			Point p = path.get(waypointIndex);
			HoloGL.renderCircle(p.x, p.y, shapeRenderer, Color.FIREBRICK);
		}
	}

	public Path getPath() {
		return this.path;
	}

	/**
	 * Also scales accel proportionately to speed
	 * 
	 * @param speed
	 */
	public void setSpeed(float speed) {
		this.speed = speed;
		this.quadAccelRate = defaultQuadraticAccelRate * (speed / defaultUnitMoveSpeed);
		this.quadDecelRate = defaultQuadraticDecelRate * (speed / defaultUnitMoveSpeed);

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

}
