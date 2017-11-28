package com.mygdx.holowyth.pathfinding;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.util.constants.Holo;
import com.mygdx.holowyth.util.data.Pair;
import com.mygdx.holowyth.util.data.Point;

public class Unit {

	public static float waypointMinDistance = 0.01f;

	public float x, y;
	public float vx, vy;
	public float speed = Holo.defaultUnitMoveSpeed;
	public float curSpeed;
	public float linearAccelRate = 0.08f;
	public float factorAccel = 0.01f;
	public float initialMoveSpeed = Holo.defaultUnitMoveSpeed; //0.32f;

	public float quadAccelNormSpeed = 1f;
	public float quadraticAccelRate = 0.02f;
	public float maxAccelFactor = 5;
	Path path;

	private static final float SQRT2 = 1.414214f;
	
	private float radius = Holo.UNIT_RADIUS;
	
	Unit() {
	}

	public Unit(float x, float y) {
		this();
		this.x = x;
		this.y = y;
		
	}

	// ** Main function **/
	public void tickLogic() {
		determineMovement();
	}

	int waypointIndex;

	public void setPath(Path path) {
		this.path = path;
		waypointIndex = 0;
		this.curSpeed = calculateInitialMoveSpeed();
		isDecelerating = false;
	}
	
	/**
	 * When a unit is given a move command that is in a similiar direction then it is already travelling, it doesn't need to slow down
	 */
	private float calculateInitialMoveSpeed(){
		
		if(curSpeed < initialMoveSpeed || curSpeed < 0.001){
			return initialMoveSpeed;
		}
		
		Point waypoint = path.get(1);
		Vector2 v = new Vector2(vx, vy);
		Vector2 p = new Vector2(waypoint.x-this.x, waypoint.y-this.y);
		
		
		if(p.len2() < 0.001){
			return initialMoveSpeed;
		}
		
		v.nor();
		p.nor();
		float cross = v.dot(p);
		
		float s = Math.min(initialMoveSpeed + curSpeed*cross, curSpeed);
		s = Math.max(s, initialMoveSpeed);
//		System.out.println("initial  move speed: " + s);
		
		return s;
		
		
	}

	private void determineMovement() {
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
					path = null;
					waypointIndex = -1;
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
			//sum up distance required to achieve desired speed
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

//		System.out.format("curSpeed %s, Time to Decel %s, Distance %s, Decel dist %s, %n", curSpeed, timeToDecel,
//				distanceToGoal, distanceToDecel);

		if (distanceToGoal < distanceToDecel) {
//			System.out.println("Decelling");
			curSpeed = Math.max(curSpeed + dSpeed, targetFinalSpeed);
			isDecelerating = true;
		}

		return curSpeed;
	}

	public void move() {
		this.x += vx;
		this.y += vy;
	}
	
	public float getRadius(){
		return radius;
	}

}
