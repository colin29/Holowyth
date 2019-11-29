package com.mygdx.holowyth.skill.skillsandeffects.projectiles;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.combatDemo.World;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * Implements the concepts of: <br>
 * -position, velocity, and rotation <br>
 * -having a duration and expiring <br>
 * -having a Side, and colliding with only enemies <br>
 * 
 * If you override an implementation (such as collided or duration), you should override the whole thing
 */
public abstract class ProjectileBase {

	protected final Point pos;

	// Speed and Velocity
	private float speed = 4.5f; // let's make the velocity increase from 2 to 5
	private float rotation;

	// Duration
	private float duration;

	// Sides and Colliding
	protected final Unit.Side side;
	protected final World world;

	// Extra info for sub-classes
	protected Unit caster;

	public ProjectileBase(float x, float y, float speed, float rotation, float maxDuration, Unit caster, World world) {
		pos = new Point(x, y);
		this.speed = speed;
		this.rotation = rotation;

		duration = maxDuration;

		this.side = caster.getSide();

		this.caster = caster;

		this.world = world;
	}

	public abstract void tick();

	public final float getX() {
		return pos.x;
	}

	public final float getY() {
		return pos.y;
	}

	///// Speed and Velocity /////

	/**
	 * Moves the projectile for one frame's motion according to its velocity
	 */
	protected void move() {
		calculateVelocity();
		pos.x += velocity.x;
		pos.y += velocity.y;
	}

	protected void setSpeed(float speed) {
		if (speed < 0) {
			throw new HoloIllegalArgumentsException("Speed must be non-negative");
		}
		this.speed = speed;
	}

	protected void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public float getRotation() {
		return rotation;
	}

	public float getSpeed() {
		return speed;
	}

	/**
	 * Use getVelocity() instead, this is lazily updated
	 */
	private final Vector2 velocity = new Vector2();

	private boolean collided;

	public Vector2 getVelocity() {
		calculateVelocity();
		return new Vector2(velocity);
	}

	private void calculateVelocity() {
		velocity.set(speed, 0);
		velocity.rotate(rotation);
	}

	///// Duration /////

	/**
	 * Should call this in your tick method, unless you are overriding the default duration implementation
	 */
	protected void tickDuration() {
		duration = Math.max(0, duration - 1);
	}

	public boolean isExpired() {
		return duration <= 0;
	}

	///// Sides and Colliding /////

	/**
	 * If bullet collides with a collision target, call onCollision() with the enemy hit, then stop searching. If you override this, you should
	 * override isCollided() too.
	 */
	protected void detectCollisionsWithEnemies() {
		var enemies = getCollisionTargets();

		for (Unit enemy : enemies) {
			if (Point.calcDistance(pos, enemy.getPos()) < enemy.getRadius()) {
				onCollision(enemy);
				collided = true;
				return;
			}
		}
	}

	/**
	 * Action that should happen when projectile collides. This is not necessarily the same unit as the target
	 */
	protected abstract void onCollision(Unit enemy);

	protected List<Unit> getCollisionTargets() {
		var units = new ArrayList<Unit>(world.getUnits());
		var targets = new ArrayList<Unit>();
		if (caster.getSide() == Side.PLAYER) {
			CollectionUtils.select(units, (u) -> u.getSide() == Side.ENEMY, targets);
		} else {
			CollectionUtils.select(units, (u) -> u.getSide() == Side.PLAYER, targets);
		}
		return targets;
	}

	public boolean isCollided() {
		return collided;
	}

}
