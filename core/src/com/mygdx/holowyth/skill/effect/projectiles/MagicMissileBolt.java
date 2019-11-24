package com.mygdx.holowyth.skill.effect.projectiles;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * 
 * This missile acquires the nearest enemy target.
 */
public class MagicMissileBolt {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public float x, y;

	private float timeInFlight = 0;
	private float timeToReachMaxVelocity = 90;
	public float baseProjectileSpeed = 1.5f;
	public float maxProjectileSpeed = 2.5f;
	public float speed = baseProjectileSpeed; // let's make the velocity increase from 2 to 5

	public float rotation; // 0 is in the x axis, rotating counter-clockwise, in degrees
	public float turnSpeed = 2; // in degrees per frame;

	public float maxDuration = 1000; // 180;
	public float duration = maxDuration;

	public boolean collided = false;

	public float damage;

	private float sideWindMaxAngle = 30; // the max amount sidewind will swerve missile away from target
	private float sideWindPeriod = 80; // period acccounts for a 180 (swing right, then left)
	private float sideWindCounter = sideWindPeriod * 0.75f;
	private float sideWindBaseTurnSpeed = 3f;

	List<Unit> units;
	Unit caster;
	Unit target; // target is allowed to be null;

	public MagicMissileBolt(float x, float y, float damage, Unit target, Unit caster, List<Unit> units) {
		this.x = x;
		this.y = y;

		this.caster = caster;
		this.units = units;

		this.damage = damage;

		this.target = target;
		rotation = Point.getAngleInDegrees(new Point(x, y), target.getPos());

	}

	/**
	 * vector purely for calculation, does not store state
	 */
	private Vector2 vec = new Vector2();

	// private void acquireTarget() {
	// Side side = caster.getSide();
	// if (side != Side.PLAYER && side != Side.ENEMY) {
	// throw new HoloAssertException("Unhandled side type");
	//
	// }
	// List<Unit> targets = getValidTargets();
	// target = getClosestTarget(targets);
	// }

	private List<Unit> getValidTargets() {
		var targets = new ArrayList<Unit>();

		if (caster.getSide() == Side.PLAYER) {
			CollectionUtils.select(units, (u) -> u.getSide() == Side.ENEMY, targets);
		} else {
			CollectionUtils.select(units, (u) -> u.getSide() == Side.PLAYER, targets);
		}
		return targets;
	}

	private Unit getClosestTarget(List<Unit> targets) {
		if (targets.isEmpty()) {
			return null;
		}

		Unit closest = targets.get(0);
		Point thisPos = new Point(x, y);
		float minDist = Float.MAX_VALUE;

		float dist;
		for (var target : targets) {
			dist = Point.calcDistance(thisPos, target.getPos());
			if (dist < minDist) {
				closest = target;
				minDist = dist;
			}
		}
		return closest;
	}

	public void tick() {
		turnTowardsTarget();
		sideWindProjectile();

		updateSpeed();

		vec.set(speed, 0);
		vec.rotate(rotation);
		x += vec.x;
		y += vec.y;

		duration -= 1;

		detectCollisionsWithEnemies();

		handleTargetDead();
	}

	private void updateSpeed() {
		timeInFlight += 1;

		float additionalSpeedPortion = Math.max(0, Math.min(timeInFlight / timeToReachMaxVelocity, 1));
		float additonalSpeedTotal = maxProjectileSpeed - baseProjectileSpeed;

		speed = baseProjectileSpeed + additonalSpeedTotal * additionalSpeedPortion;
	}

	private void turnTowardsTarget() {
		if (target == null)
			return;

		// get angle to target

		rotation = normalizeAngle(rotation);

		Point thisPos = new Point(x, y);

		float angleToTarget = Point.getAngleInDegrees(thisPos, target.getPos());
		float relativeAngle = normalizeAngle(angleToTarget - rotation); // represents how many degrees you should turn CCW

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
			rotation = angleToTarget;
		} else {
			if (isClockwise) {
				rotation -= turnSpeed;
			} else {
				rotation += turnSpeed;
			}
		}

		rotation = normalizeAngle(rotation);
	}

	/**
	 * @return value in the range [0, 360)
	 */
	private float normalizeAngle(float angle) {
		return (angle %= 360) > 0 ? angle : (angle + 360);
	}

	/**
	 * Rotate the projectile to make it veer side to side
	 */
	private void sideWindProjectile() {

		Point thisPos = new Point(x, y);
		float angleToTarget = Point.getAngleInDegrees(thisPos, target.getPos());
		float relativeAngle = normalizeAngle(angleToTarget - rotation); // represents how many degrees you should turn CCW

		if (relativeAngle >= 180) // recenter relative angle from [-180, 180)
			relativeAngle -= 360;

		float amountToRotate = 0;

		isSideWindingLeftDebug = false;

		// We want the sidewinder to follow a sin motion, so the speed should be the derivative, or cos
		float sideWindTurnSpeed = Math.abs(sideWindBaseTurnSpeed * (float) (Math.cos(sideWindCounter / sideWindPeriod * -Math.PI)));

		if (sideWindCounter >= sideWindPeriod / 2) { // sidewinding left (CCW)

			if (relativeAngle > -1 * sideWindMaxAngle) {
				isSideWindingLeftDebug = true;
				// if turning in the wrong direction, only turn up until sideWindMaxAngle
				amountToRotate = Math.min(sideWindTurnSpeed, sideWindMaxAngle + relativeAngle);
				rotation += amountToRotate;
			}
		} else {
			float reverseRelativeAngle = relativeAngle * -1;
			if (reverseRelativeAngle > -1 * sideWindMaxAngle) {
				isSideWindingLeftDebug = true;
				// if turning in the wrong direction, only turn up until sideWindMaxAngle
				amountToRotate = Math.min(sideWindTurnSpeed, sideWindMaxAngle + reverseRelativeAngle);
				rotation -= amountToRotate;
			}
		}

		// Increment counter
		sideWindCounter += 1;
		if (sideWindCounter >= sideWindPeriod) {
			sideWindCounter = 0;
		}
		// logger.debug("{}", sideWindCounter);
	}

	private boolean isSideWindingLeftDebug = false;

	public boolean isSideWindingLeft() {
		return isSideWindingLeftDebug;
	}

	private void detectCollisionsWithEnemies() {
		var enemies = getValidTargets();

		Point thisPos = new Point(x, y);
		for (Unit enemy : enemies) {
			if (Point.calcDistance(thisPos, enemy.getPos()) < enemy.getRadius()) {
				enemy.stats.applyDamageIgnoringArmor(damage);

				// Apply knockback stun in direction missile was travelling
				Vector2 knockBackVec = new Vector2(vec).setLength(0.5f);

				enemy.stats.doKnockBackRollAgainst(15, 20, knockBackVec, 1.5f);

				collided = true;
				return;
			}
		}
	}

	private void handleTargetDead() {
		if (target == null)
			return;

		if (target.isDead() && collided == false) {
			duration = Math.min(15, duration);
		}
	}
}
