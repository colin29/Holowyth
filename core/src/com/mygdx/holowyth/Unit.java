package com.mygdx.holowyth;

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
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.pathfinding.UnitInterPF;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloGL;
import com.mygdx.holowyth.util.data.Point;
import com.mygdx.holowyth.util.data.Segment;

public class Unit implements UnitInterPF {

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
	ArrayList<Unit> units;

	// Collision Detection
	private float radius = Holo.UNIT_RADIUS;

	// Debug
	private static int curId = 0;
	private final int ID;

	// Orders
	Order currentOrder = Order.IDLE;
	Unit target; // target for the current command.

	private static int attackPathfindingInterval = 30;
	private int framesUntilAttackRepath = attackPathfindingInterval;

	// Combat
	Unit attacking; // unit the unit is current attacking.
	private static Map<Unit, Set<Unit>> unitsAttacking = new HashMap<Unit, Set<Unit>>();
	Mode mode = Mode.FLEE;
	Side side;

	public enum Side { // for now, simple, two forces
		PLAYER, ENEMY
	}

	public enum Order {
		MOVE, ATTACKUNIT, IDLE, ATTACKMOVE
	}

	public enum Mode {
		ENGAGE, FLEE
	}

	Unit() {
		this.ID = Unit.getNextId();
	}

	public Unit(float x, float y, World world, Side side) {
		this();
		this.x = x;
		this.y = y;
		this.side = side;

		// get neccesary references
		this.units = world.getUnits();
		this.pathingModule = world.getPathingModule();

		Unit.unitsAttacking.put(this, new HashSet<Unit>());

	}

	/**
	 * Main function, also is where the unit determines its movement
	 */
	public void handleGeneralLogic() {

		handleRepathing();
		determineMovement();

		makeIdleEnemiesAggro();

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
		if (!isMoveOrderAllowed()) {
			return;
		}
		Path path = pathingModule.findPathForUnit(this, dx, dy, units);
		if (path != null) {
			clearOrder();
			currentOrder = Order.MOVE;
			this.setPath(path);
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

		pathForAttackingUnit();
	}
	
	public void orderAttackMove(float x, float y){
		if (!isAttackMoveOrderAllowed()) {
			return;
		}
		//TODO:
		
	}

	private void pathForAttackingUnit() {
		// find path as normal, except for pathing ignore the target's collision body
		ArrayList<Unit> someUnits = new ArrayList<Unit>(units);
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

	private static final float SQRT2 = 1.414214f;

	/**
	 * When a unit is given a move command that is in a similar direction then it is already travelling, it doesn't need
	 * to slow down
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
	 * Sets the velocity for the unit based on the unit's path. Also accounts for accel+decel at the begin and end of
	 * movement. When movement is complete, a unit's path is set to null
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
			if (dist < Unit.waypointMinDistance) {
				waypointIndex += 1;
				// check if completed path
				if (waypointIndex == path.size()) {
					currentOrder = Order.IDLE;
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
			float dist = Point.calcDistance(a, b);
			if (dist <= this.radius + target.radius + Holo.defaultUnitEngageRange) {
				startAttacking(target);
			} else if (dist >= this.radius + target.radius + Holo.defaultUnitDisengageRange) {
				stopAttacking(target);
			}
		}

		if (this.mode == Mode.ENGAGE) {
			Set<Unit> attackingMe = unitsAttacking.get(this);
			if (!attackingMe.isEmpty() && attacking == null) {
				orderAttackUnit(attackingMe.iterator().next());
			}
		}

	}

	private void startAttacking(Unit target) {
		clearPath();
		attacking = target;
		unitsAttacking.get(target).add(this);
	}

	private void stopAttacking(Unit target) {
		attacking = null;
		unitsAttacking.get(target).remove(this);
	}

	// Debug
	private static int getNextId() {
		return curId++;
	}

	public String toString() {
		return String.format("Unit[ID: %s]", this.ID);

	}

	public void renderNextWayPoint(ShapeRenderer shapeRenderer) {
		if (path != null) {
			Point p = path.get(waypointIndex);
			HoloGL.renderCircle(p.x, p.y, shapeRenderer, Color.FIREBRICK);
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

	private boolean isAttackOrderAllowed(Unit target) {
		if (attacking != null)
			return false;
		if (target.side == this.side) {
			return false;
		}
		return true;
	}

	private boolean isMoveOrderAllowed() {
		if (attacking != null)
			return false;
		return true;
	}
	private boolean isAttackMoveOrderAllowed() {
		return isMoveOrderAllowed();
	}

	/**
	 * Makes this unit aggro to nearby visible targets if it is an enemy faction unit
	 */
	private void makeIdleEnemiesAggro() {
		if (this.side == Side.ENEMY && currentOrder == Order.IDLE) {
			
			PriorityQueue<Unit> closestEnemies = new PriorityQueue<Unit>(closestUnitComp);
			for(Unit u: units){
				if(u == this)
					continue;
				if(u.side != Side.ENEMY){
					closestEnemies.add(u);
				}
			}
			if(!closestEnemies.isEmpty()){
				Unit closestEnemy = closestEnemies.remove();
				float x = getDist(this, closestEnemy);
				if(getDist(this, closestEnemy) <= Holo.idleAggroRange){
					this.orderAttackUnit(closestEnemy);
				}
			}
		}
	}

	// Convenience functions
	public static float getDist(Unit u1, Unit u2) {
		return Point.calcDistance(u1.getPos(), u2.getPos());
	}

	//Tools
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
		return this.path;
	}

}
