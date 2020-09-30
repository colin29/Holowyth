/**
 * 
 */
/**
 * @author Colin Ta
 *
 */
package com.mygdx.holowyth.pathfinding.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.pathfinding.UnitPFWithPath;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;

/**
 * Simpler unit class for use in the Pathfinding demo
 */
public class PFDemoUnit implements UnitPFWithPath {

	public static float waypointMinDistance = 0.01f;

	public float x, y;

	// Movement
	public float vx, vy;
	public float speed = Holo.defaultUnitMoveSpeed;
	public float curSpeed;
	public float linearAccelRate = 0.08f;
	public float factorAccel = 0.01f;
	public float initialMoveSpeed = Holo.defaultUnitMoveSpeed; // 0.32f;

	public float quadAccelNormSpeed = 1f;
	public float quadraticAccelRate = 0.02f;
	public float maxAccelFactor = 5;
	public Path path;

	// World Fields
	PathingModule pathingModule;
	ArrayList<@NonNull PFDemoUnit> units;

	// Collision Detection
	private float radius = Holo.UNIT_RADIUS;

	// Debug
	private static int curId = 0;
	private final int ID;

	// Orders
	Order currentOrder = Order.IDLE;
	PFDemoUnit target; // target for the current command.

	private static int attackPathfindingInterval = 30;
	private int framesUntilAttackRepath = attackPathfindingInterval;

	// Combat
	PFDemoUnit attacking; // unit the unit is current attacking.
	private static Map<PFDemoUnit, Set<PFDemoUnit>> unitsAttacking = new HashMap<PFDemoUnit, Set<PFDemoUnit>>();
	Mode mode = Mode.FLEE;

	public enum Order {
		MOVE, ATTACKUNIT, IDLE
	}

	public enum Mode {
		ENGAGE, FLEE
	}

	PFDemoUnit() {
		this.ID = PFDemoUnit.getNextId();
	}

	public PFDemoUnit(float x, float y, PFWorld world) {
		this();
		this.x = x;
		this.y = y;

		// get neccesary references
		this.units = world.getUnits();
		this.pathingModule = world.getPathingModule();

		PFDemoUnit.unitsAttacking.put(this, new HashSet<PFDemoUnit>());

	}

	/**
	 * Main function, also is where the unit determines its movement
	 */
	public void handleGeneralLogic() {

		handleRepathing();
		determineMovement();

	}

	/** Repath regularly if unit is ordered to attack but is not attacking yet */
	private void handleRepathing() {
		if (this.currentOrder == Order.ATTACKUNIT && attacking == null) {
			framesUntilAttackRepath -= 1;
			if (framesUntilAttackRepath <= 0) {
				pathForAttackingUnit();
				framesUntilAttackRepath = attackPathfindingInterval;
			}
		}
	}

	int waypointIndex;

	// Orders

	public void orderMove(float dx, float dy) {
		if (!isActionAllowed(Order.MOVE)) {
			return;
		}
		Path path = pathingModule.findPathForUnit(this, dx, dy, units);
		if (path != null) {
			clearOrder();
			currentOrder = Order.MOVE;
			this.setPath(path);
		}
	}

	public void orderAttackUnit(PFDemoUnit unit) {
		if (!isActionAllowed(Order.ATTACKUNIT)) {
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

		pathForAttackingUnit();
	}

	private void pathForAttackingUnit() {
		// find path as normal, except for pathing ignore the target's collision body
		ArrayList<@NonNull PFDemoUnit> someUnits = new ArrayList<@NonNull PFDemoUnit>(units);
		someUnits.remove(target);

		Path path = pathingModule.findPathForUnit(this, target.x, target.y, someUnits);
		if (path != null) {
			this.setPath(path);
		}
	}

	/**
	 * @param path
	 *            should not be null
	 */
	private void setPath(Path path) {
		this.path = path;
		waypointIndex = 0;
		curSpeed = calculateInitialMoveSpeed();
		isDecelerating = false;
	}

	private void clearPath() {
		this.path = null;
		waypointIndex = -1;
		vx = 0;
		vy = 0;
	}

	/** Clears any current order on this unit */
	private void clearOrder() {
		currentOrder = Order.IDLE;
		target = null;
	}

	// Unit Movement

	/**
	 * When a unit is given a move command that is in a similar direction then it is already travelling, it doesn't need to slow down
	 */
	private float calculateInitialMoveSpeed() {

		if (curSpeed < initialMoveSpeed || curSpeed < 0.001) {
			return initialMoveSpeed;
		}

		Point waypoint = path.get(1);
		Vector2 v = new Vector2(vx, vy);
		Vector2 p = new Vector2(waypoint.x - this.x, waypoint.y - this.y);

		if (p.len2() < 0.001) {
			return initialMoveSpeed;
		}

		v.nor();
		p.nor();
		float cross = v.dot(p);

		float s = Math.min(initialMoveSpeed + curSpeed * cross, curSpeed);
		s = Math.max(s, initialMoveSpeed);
		// System.out.println("initial move speed: " + s);

		return s;

	}

	/**
	 * Sets the velocity for the unit based on the unit's path. Also accounts for accel+decel at the begin and end of movement. When movement is
	 * complete, a unit's path is set to null
	 */
	private void determineMovement() {
		switch (currentOrder) {
		case MOVE:
			determineMovementForMoveOrderedUnit();
		case ATTACKUNIT:
			determineMovementForAttackOrderedUnit();
		default:
			break;
		}
	}

	private void determineMovementForMoveOrderedUnit() {
		if (path != null) {

			// Apply acceleration if the unit is not already at full speed
			if (curSpeed < speed && !isDecelerating) {
				curSpeed = Math.min(curSpeed + Math.min(quadraticAccelRate * quadAccelNormSpeed / curSpeed,
						quadraticAccelRate * maxAccelFactor), speed);
				// curSpeed = Math.min(curSpeed + (speed-curSpeed)*factorAccel, speed);
			}

			// System.out.println("CurSpeed: " + curSpeed);

			Point curWaypoint = path.get(waypointIndex);

			float wx = curWaypoint.x;
			float wy = curWaypoint.y;

			float dx = wx - x;
			float dy = wy - y;

			float dist = (float) Math.sqrt(dx * dx + dy * dy);

			// Apply deceleration if the unit is approaching the final goal

			if (waypointIndex == path.size() - 1) {
				curSpeed = calculateSpeedApproachingGoal(dist);
			}

			// Check if reached waypoint
			if (dist < PFDemoUnit.waypointMinDistance) {
				waypointIndex += 1;
				// check if completed path
				if (waypointIndex == path.size()) {
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

	private void determineMovementForAttackOrderedUnit() {
		if (path != null) {
			determineMovementForMoveOrderedUnit();
			// Can add extra code this so that units don't enter too deeply into the engage range. The math is simple
			// but the fact that the units can't be aware of the other's movement makes it non-trivial
		}
	}

	public float targetFinalSpeed = 0.2f;// speed/2f; // speed we want the unit to reach the goal with.
	public float linearDecelRate = 0.01f;
	public float quadDecelRate = 0.02f;
	private float quadDecelNormSpeed = 1f; // means that something at this speed should decel at the listed rate
	private int delay = 0;

	boolean isDecelerating = false;

	boolean useQuadraticDecel = true;

	/**
	 * Is called to determine when to decelerate when the unit arrives at the goal.
	 * 
	 * @return
	 */
	private float calculateSpeedApproachingGoal(float distanceToGoal) {

		if (curSpeed <= targetFinalSpeed) {
			return curSpeed;
		}
		float timeToDecel = 0;
		float distanceToDecel = 0;
		float dSpeed = 0; // is negative

		if (useQuadraticDecel) {
			// sum up distance required to achieve desired speed
			for (float s = curSpeed; s > targetFinalSpeed; s -= quadDecelRate * (quadDecelNormSpeed / s)) {
				distanceToDecel += s - quadDecelRate * (quadDecelNormSpeed / s);
				//
			}
			dSpeed = -1 * quadDecelRate * (quadDecelNormSpeed) / curSpeed;

		} else { // use linear deceleration
			timeToDecel = (curSpeed - targetFinalSpeed) / linearDecelRate + delay; // in frames
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

	// Combat

	/** Handles the combat logic for a unit for one frame */
	public void handleCombatLogic() {

		if (currentOrder == Order.ATTACKUNIT) {
			// If the unit is on an attackUnit command and its in engage range, make it start attacking the target
			// If the unit falls out of engage range, stop it from attacking
			Point a, b;
			a = this.getPos();
			b = target.getPos();
			float dist = Point.dist(a, b);
			if (dist <= this.radius + target.radius + Holo.defaultUnitEngageRange) {
				startAttacking(target);
			} else if (dist >= this.radius + target.radius + Holo.defaultUnitDisengageRange) {
				stopAttacking(target);
			}
		}

		if (this.mode == Mode.ENGAGE) {
			Set<PFDemoUnit> attackingMe = unitsAttacking.get(this);
			if (!attackingMe.isEmpty() && attacking == null) {
				orderAttackUnit(attackingMe.iterator().next());
			}
		}

	}

	private void startAttacking(PFDemoUnit target) {
		clearPath();
		attacking = target;
		unitsAttacking.get(target).add(this);
	}

	private void stopAttacking(PFDemoUnit target) {
		attacking = null;
		unitsAttacking.get(target).remove(this);
	}

	// Debug
	private static int getNextId() {
		return curId++;
	}

	@Override
	public String toString() {
		return String.format("Unit[ID: %s]", this.ID);

	}

	public void renderNextWayPoint(ShapeRenderer shapeRenderer) {
		if (path != null) {
			Point p = path.get(waypointIndex);
			HoloGL.renderCircle(p.x, p.y, Color.FIREBRICK);
		}
	}

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

			HoloGL.renderSegment(s, arrowColor);

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

			HoloGL.renderSegment(wingSeg, arrowColor);

			shapeRenderer.identity();
			shapeRenderer.translate(edgePoint.x, edgePoint.y, 0);
			shapeRenderer.rotate(0, 0, 1, (float) Math.toDegrees(backwardsAngle) - arrowAngle);

			HoloGL.renderSegment(wingSeg, arrowColor);

			shapeRenderer.identity();

		}
	}

	/**
	 * Determines whether the particular action is allowed in the current state
	 */
	private boolean isActionAllowed(Order order) {
		switch (order) {
		case ATTACKUNIT:
			if (attacking == null) {
				return true;
			}
		case MOVE:
			if (attacking == null) {
				return true;
			}
		case IDLE:
			break;
		default:
			break;
		}
		return false;

	}

	// Getters
	@Override
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
		return this.path;
	}

}
