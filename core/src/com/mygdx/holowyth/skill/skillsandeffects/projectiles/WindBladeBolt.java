
package com.mygdx.holowyth.skill.skillsandeffects.projectiles;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.util.dataobjects.Point;

public class WindBladeBolt implements Projectile {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	public final Point pos;

	private float speed = 4.5f; // let's make the velocity increase from 2 to 5
	private Vector2 velocity = new Vector2();

	private float rotation; // 0 is in the x axis, rotating counter-clockwise, in degrees
	private float turnSpeed = 2; // in degrees per frame;

	private float maxDuration = 1000; // 180;
	private float duration = maxDuration;

	private boolean collided = false;
	private float damage;

	List<Unit> units;
	Unit caster;
	Unit target; // target is allowed to be null;

	public WindBladeBolt(float x, float y, float damage, Unit target, Unit caster, List<Unit> units) {
		pos = new Point(x, y);

		this.caster = caster;
		this.units = units;

		this.damage = damage;

		this.target = target;
		rotation = Point.getAngleInDegrees(new Point(x, y), target.getPos()) + calculateAngleOffset();
		calculateVelocity();
	}

	private List<Unit> getCollisionTargets() {
		var targets = new ArrayList<Unit>();

		if (caster.getSide() == Side.PLAYER) {
			CollectionUtils.select(units, (u) -> u.getSide() == Side.ENEMY, targets);
		} else {
			CollectionUtils.select(units, (u) -> u.getSide() == Side.PLAYER, targets);
		}
		return targets;
	}

	@Override
	public void tick() {
		turnTowardsTarget();

		calculateVelocity();
		pos.x += velocity.x;
		pos.y += velocity.y;

		duration -= 1;

		detectCollisionsWithEnemies();

		handleTargetDead();
	}

	private void calculateVelocity() {
		velocity.set(speed, 0);
		velocity.rotate(rotation);
	}

	float maxOffSetRange = 15;
	float maxOffset = RandomUtils.nextFloat(0, maxOffSetRange * 2) - maxOffSetRange;

	private float calculateAngleOffset() {
		var dist = Point.calcDistance(pos, target.getPos());

		float maxDistance = 200;

		return Math.min(maxOffset, Math.max(0, maxOffset / maxDistance * dist));
	}

	private void turnTowardsTarget() {
		if (target == null)
			return;

		rotation = normalizeAngle(rotation);

		float desiredAngle = Point.getAngleInDegrees(pos, target.getPos()) + calculateAngleOffset(); // seek towards an offset angle, in order to arc
		float relativeAngle = normalizeAngle(desiredAngle - rotation); // how many degrees you should turn CCW to face target

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
			rotation = desiredAngle;
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

	private void detectCollisionsWithEnemies() {
		var enemies = getCollisionTargets();

		for (Unit enemy : enemies) {
			if (Point.calcDistance(pos, enemy.getPos()) < enemy.getRadius()) {
				enemy.stats.applyDamage(damage, true);
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

	@Override
	public float getX() {
		return pos.x;
	}

	@Override
	public float getY() {
		return pos.y;
	}

	@Override
	public boolean isExpired() {
		return duration <= 0;
	}

	@Override
	public boolean isCollided() {
		return collided;
	}

	@Override
	public Vector2 getVelocity() {
		// TODO Auto-generated method stub
		return null;
	}
}
