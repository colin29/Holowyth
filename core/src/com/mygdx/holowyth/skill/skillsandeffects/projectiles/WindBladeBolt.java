
package com.mygdx.holowyth.skill.skillsandeffects.projectiles;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.dataobjects.Point;

public class WindBladeBolt extends ProjectileBase {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private static float speed = 4.5f;
	private float turnSpeed = 2; // degrees per frame;

	private static float maxDuration = 1000;

	private float damage;

	Unit caster;
	Unit target; // target is allowed to be null;

	public WindBladeBolt(float x, float y, float damage, Unit caster, Unit target) {
		super(x, y, speed, Point.getAngleInDegrees(caster.getPos(), target.getPos()), maxDuration, caster);
		this.damage = damage;
		this.target = target;

		setSpeed(4.5f);
	}

	@Override
	public void tick() {
		turnTowardsTarget();
		move();
		detectCollisionsWithEnemies();
		detectCollisionWithObstacles();
		tickDuration();

		handleTargetDead();
	}

	float maxOffSetRange = 15;
	float maxOffset = RandomUtils.nextFloat(0, maxOffSetRange * 2) - maxOffSetRange;

	private void turnTowardsTarget() {
		if (target == null)
			return;

		float iRotation = getRotation();

		float desiredAngle = Point.getAngleInDegrees(pos, target.getPos()) + calculateAngleOffset(); // seek towards an offset angle, in order to arc
		float relativeAngle = normalizeAngle(desiredAngle - iRotation); // how many degrees you should turn CCW to face target

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
			iRotation = desiredAngle;
		} else {
			if (isClockwise) {
				iRotation -= turnSpeed;
			} else {
				iRotation += turnSpeed;
			}
		}

		setRotation(iRotation);
	}

	private float calculateAngleOffset() {
		var dist = Point.calcDistance(pos, target.getPos());
		float maxDistance = 200;
		return Math.min(maxOffset, Math.max(0, maxOffset / maxDistance * dist));
	}

	private void handleTargetDead() {
		if (target == null)
			return;
		if (target.isDead())
			target = null;
	}

	@Override
	protected void onCollision(Unit enemy) {
		enemy.stats.applyDamage(damage);
	}

}
