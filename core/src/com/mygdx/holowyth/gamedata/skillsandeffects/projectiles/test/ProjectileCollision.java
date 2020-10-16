package com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.test;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.mygdx.holowyth.game.MapInstance;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * Represent the collision aspect of a projectile, which collides with certain things
 */
@NonNullByDefault
public abstract class ProjectileCollision {

	private final Projectile self;
	protected final Point pos;
	protected final Unit.Side side;
	protected final MapInstance mapInstance;
	protected final EffectsHandler gfx;
	
	protected boolean collided;

	public ProjectileCollision(Projectile self) {
		this.self = self;
		pos = self.pos;
		this.side = self.side;
		mapInstance = self.mapInstance;
		gfx = self.gfx;
	}

	/**
	 * If collided, should mark collided to true
	 */
	protected abstract void detectCollisionsWithEnemies();

	protected abstract void detectCollisionWithObstacles();

	/**
	 * Action that should happen when projectile collides with an enemy. This is not necessarily the
	 * same unit as the target
	 */
	protected void onCollision(Unit enemy) {
	}

	protected void tick() {
		detectCollisionsWithEnemies();
		detectCollisionWithObstacles();
	}

	/**
	 * Action that should happen when projectile collides with terrain.
	 */
	protected void onCollisionWithObstacle() {
	}

	public final boolean isCollided() {
		return collided;
	}

	protected final float getVx() {
		return self.getVx();
	}

	protected final float getVy() {
		return self.getVy();
	}

}