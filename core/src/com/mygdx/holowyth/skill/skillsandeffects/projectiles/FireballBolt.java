package com.mygdx.holowyth.skill.skillsandeffects.projectiles;

import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.dataobjects.Point;

public class FireballBolt extends ProjectileBase {

	private static float maxDuration = 800;
	private static float speed = 1.8f;

	private float turnSpeed = 1;

	private float damage;
	private float explosionRadius;

	private Unit target;

	public FireballBolt(float damage, float explosionRadius, Unit caster, Unit target) {
		super(caster.x, caster.y, speed, Point.getAngleInDegrees(caster.getPos(), target.getPos()), maxDuration, caster);
		this.damage = damage;
		this.explosionRadius = explosionRadius;

		this.target = target;
	}

	@Override
	public void tick() {
		turnTowardsTarget();
		move();
		detectCollisionsWithEnemies();
		tickDuration();
		handleTargetDead();
	}

	private void turnTowardsTarget() {
		if (target == null)
			return;

		float iRotation = getRotation();

		float desiredAngle = Point.getAngleInDegrees(pos, target.getPos());
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

	@Override
	protected void onCollision(Unit enemy) {
		damageEnemiesInRange();
	}

	private void damageEnemiesInRange() {
		for (var unit : world.getUnits()) {
			if (Point.calcDistance(pos, unit.getPos()) <= explosionRadius + unit.getRadius()) {
				if (isAEnemy(unit)) {
					unit.stats.applyMagicDamage(damage);
				} else {
					unit.stats.applyMagicDamage(damage / 2);
				}
			}
		}
	}

	private void handleTargetDead() {
		if (target == null)
			return;
		if (target.isDead()) {
			target = null;
		}
	}

}