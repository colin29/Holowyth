package com.mygdx.holowyth.gamedata.skillsandeffects.projectiles;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.graphics.effects.animated.AnimEffectOnFixedPos;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.util.dataobjects.Point;

public class ArcaneBoltBolt extends ProjectileBase {

	private static float maxDuration = 800;
	private static float speed = 1.8f;

	private float turnSpeed = 1;

	private float damage;

	private UnitOrderable target;

	public ArcaneBoltBolt(float damage, Unit caster, UnitOrderable target) {
		super(caster.x, caster.y, speed, Point.getAngleInDegrees(caster.getPos(), target.getPos()), maxDuration, caster);
		this.damage = damage;
		this.target = target;
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
		enemy.stats.applyDamage(damage);

		Vector2 knockBackVec = getVelocity().setLength(1.5f);
		enemy.stats.doKnockBackRollAgainst(25, 60 * 3f, knockBackVec);
		gfx.addGraphicEffect(new AnimEffectOnFixedPos(pos.x, pos.y, "12.png", mapInstance));
	}

	private void handleTargetDead() {
		if (target == null)
			return;
		if (target.isDead()) {
			target = null;
		}
	}

}
