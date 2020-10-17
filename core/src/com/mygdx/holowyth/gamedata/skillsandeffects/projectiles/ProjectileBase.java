package com.mygdx.holowyth.gamedata.skillsandeffects.projectiles;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.game.MapInstanceInfo;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.pathfinding.HoloPF;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;
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

	Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final Point pos;
	protected float duration;
	
	// Speed and Velocity
	private float speed;
	private float rotation;
	/**
	 * Use getVelocity() instead, this is lazily updated
	 */
	private final Vector2 velocity = new Vector2();
	
	

	// Sides and Colliding
	protected final Unit.Side side;
	protected final MapInstanceInfo mapInstance;
	protected final EffectsHandler gfx;
	private float collisionRadius = 0;
	private boolean collided;


	// Extra info for sub-classes
	@NonNull protected UnitOrderable caster;

	public ProjectileBase(float x, float y, float speed, float rotation, float maxDuration, Unit caster) {
		pos = new Point(x, y);
		this.speed = speed;
		this.rotation = rotation;

		duration = maxDuration;

		this.side = caster.getSide();
		this.caster = caster;

		this.mapInstance = caster.getMapInstanceMutable();
		gfx = mapInstance.getGfx();
	}

	/**
	 */
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

	private void calculateVelocity() {
		velocity.set(speed, 0);
		velocity.rotate(rotation);
	}
	
	protected void setSpeed(float speed) {
		if (speed < 0) {
			throw new HoloIllegalArgumentsException("Speed must be non-negative");
		}
		this.speed = speed;
	}

	/**
	 * Normalizes and sets angle
	 */
	protected void setRotation(float rotation) {
		this.rotation = normalizeAngle(rotation);
	}

	/**
	 * @return value in the range [0, 360)
	 */
	protected static float normalizeAngle(float angle) {
		return (angle %= 360) > 0 ? angle : (angle + 360);
	}

	/**
	 * @return angle in the range [0, 360)
	 */
	public float getRotation() {
		return rotation;
	}

	public float getSpeed() {
		return speed;
	}

	/**
	 * Returns a new vector
	 */
	public Vector2 getVelocity() {
		calculateVelocity();
		return new Vector2(velocity);
	}

	public float getVx() {
		return getVelocity().x;
	}

	public float getVy() {
		return getVelocity().y;
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
			if (Point.dist(pos, enemy.getPos()) < enemy.getRadius()) {
				onCollision(enemy);
				collided = true;
				return;
			}
		}
	}

	protected void detectCollisionWithObstacles() {
		PathingModule pathing = mapInstance.getPathingModule();
		final var motion = new Segment(pos.x, pos.y, pos.x + getVx(), pos.y + getVy());
		if (!HoloPF.isSegmentPathableAgainstObstaclesNonExpandedSeg(motion, pathing.getObstacleSegs(), pathing.getObstaclePoints(),
				collisionRadius)) {
			onCollisionWithObstacle();
			collided = true;
		}
	}

	/**
	 * Action that should happen when projectile collides with an enemy. This is not necessarily the same unit as the target
	 */
	protected abstract void onCollision(Unit enemy);

	/**
	 * Action that should happen when projectile collides with terrain.
	 */
	protected void onCollisionWithObstacle() {
	}

	protected List<Unit> getCollisionTargets() {
		var units = new ArrayList<Unit>(mapInstance.getUnits());
		var targets = new ArrayList<Unit>();
		if (caster.getSide() == Side.PLAYER) {
			CollectionUtils.select(units, (u) -> u.getSide() == Side.ENEMY, targets);
		} else {
			CollectionUtils.select(units, (u) -> u.getSide() == Side.PLAYER, targets);
		}
		return targets;
	}

	protected final boolean isAEnemy(UnitOrderable unit) {
		return side != unit.getSide();
	}

	public boolean isCollided() {
		return collided;
	}

	/**
	 * Default radius is 0, set if desired. 0 radius looks better with most small projectiles.
	 */
	public void setCollisionRadius(float collisionRadius) {
		this.collisionRadius = collisionRadius;
	}

}
