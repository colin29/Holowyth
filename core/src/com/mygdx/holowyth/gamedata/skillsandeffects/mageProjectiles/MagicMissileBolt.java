package com.mygdx.holowyth.gamedata.skillsandeffects.mageProjectiles;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.dataobjects.Point;

public class MagicMissileBolt extends ProjectileBase {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public static float maxDuration = 1000;

	// Accelerating and Steering
	private float timeInFlight = 0;
	public static float intialSpeed = 1.5f;
	public float maxProjectileSpeed = 2.5f;
	private float timeToReachMaxVelocity = 90;

	public float turnSpeed = 2; // degrees per frame;

	public float damage;

	// Sidewinding
	private float sideWindMaxAngle = 30; // the max amount sidewind will swerve missile away from target
	private float sideWindPeriod = 80; // period acccounts for a 180 (swing right, then left)
	private float sideWindCounter = sideWindPeriod * 0.75f;
	private float sideWindBaseTurnSpeed = 3f;

	Unit target; // target is allowed to be null;

	public MagicMissileBolt(float x, float y, float damage, Unit target, Unit caster, List<@NonNull Unit> units) {
		super(x, y, intialSpeed, Point.getAngleInDegrees(caster.getPos(), target.getPos()), maxDuration, caster);
		this.damage = damage;
		this.target = target;

	}

	@Override
	public void tick() {
		turnTowardsTarget();
		sideWindProjectile();
		updateSpeed();

		move();
		detectCollisionsWithEnemies();
		detectCollisionWithObstacles();
		tickDuration();

		handleTargetDead();
	}

	private void updateSpeed() {
		timeInFlight += 1;

		float additionalSpeedPortion = Math.max(0, Math.min(timeInFlight / timeToReachMaxVelocity, 1));
		float additonalSpeedTotal = maxProjectileSpeed - intialSpeed;

		setSpeed(intialSpeed + additonalSpeedTotal * additionalSpeedPortion);
	}

	private void turnTowardsTarget() {
		if (target == null)
			return;

		// get angle to target

		float iRotation = getRotation();

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

		setRotation(iRotation);
	}

	/**
	 * Rotate the projectile to make it veer side to side
	 */
	private void sideWindProjectile() {

		float angleToTarget = Point.getAngleInDegrees(pos, target.getPos());
		float relativeAngle = normalizeAngle(angleToTarget - getRotation()); // represents how many degrees you should turn CCW

		if (relativeAngle >= 180) // recenter relative angle from [-180, 180)
			relativeAngle -= 360;

		float amountToRotate = 0;

		// We want the sidewinder to follow a sin motion, so the speed should be the derivative, or cos
		float sideWindTurnSpeed = Math.abs(sideWindBaseTurnSpeed * (float) (Math.cos(sideWindCounter / sideWindPeriod * -Math.PI)));

		if (sideWindCounter >= sideWindPeriod / 2) { // sidewinding left (CCW)

			if (relativeAngle > -1 * sideWindMaxAngle) {
				// if turning in the wrong direction, only turn up until sideWindMaxAngle
				amountToRotate = Math.min(sideWindTurnSpeed, sideWindMaxAngle + relativeAngle);
				setRotation(getRotation() + amountToRotate);
			}
		} else {
			float reverseRelativeAngle = relativeAngle * -1;
			if (reverseRelativeAngle > -1 * sideWindMaxAngle) {
				amountToRotate = Math.min(sideWindTurnSpeed, sideWindMaxAngle + reverseRelativeAngle);
				setRotation(getRotation() - amountToRotate);
			}
		}

		sideWindCounter += 1;
		if (sideWindCounter >= sideWindPeriod) {
			sideWindCounter = 0;
		}
	}

	private void handleTargetDead() {
		if (target == null)
			return;
		if (target.isDead()) {
			duration = Math.min(15, duration);
		}
	}

	@Override
	protected void onCollision(Unit enemy) {
		enemy.stats.applyDamage(damage);
		

		// Apply knockback stun in direction missile was travelling
		Vector2 knockBackVec = getVelocity().setLength(0.5f);
		enemy.stats.doKnockBackRollAgainst(15, 60 * 0.5f, knockBackVec, 1.5f);

	}
}
