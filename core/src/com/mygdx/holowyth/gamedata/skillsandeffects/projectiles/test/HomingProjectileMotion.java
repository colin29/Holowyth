package com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.test;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.dataobjects.Point;

@NonNullByDefault
public class HomingProjectileMotion extends ProjectileMotion {
	
	
	public static float turnSpeedDebug = 2;
	public static float speedDebug = 2;
	
	public float turnSpeed = 2;
	protected final UnitInfo target;
	
	public HomingProjectileMotion(float speed, float direction, UnitInfo target, Projectile self) {
		super(speed, direction, self);
		this.target = target;
	}

	@Override
	protected void tick() {
		turnTowardsTarget();
		move();
	}
	
	private void turnTowardsTarget() {
		// get angle to target

		float iRotation = getDirection();

		float angleToTarget = Point.getAngleInDegrees(pos, target.getPos());
		float relativeAngle = normalizeAngle(angleToTarget - iRotation); // represents how many degrees you should turn CCW

		boolean isClockwise;
		float turnRequired; // degrees to the target, going by the shorter direction (clock-wise, counter-clock-wise)
		if (relativeAngle > 180) {
			turnRequired = 360 - relativeAngle;
			isClockwise = true;
		} else { // angle is between 0 and 180
			turnRequired = relativeAngle;
			isClockwise = false;
		}
		if (turnRequired < turnSpeed) {
			iRotation = angleToTarget;
		} else {
			if (isClockwise) {
				iRotation -= turnSpeed;
			} else {
				iRotation += turnSpeed;
			}
		}
		setDirection(iRotation);
	}

	

}
