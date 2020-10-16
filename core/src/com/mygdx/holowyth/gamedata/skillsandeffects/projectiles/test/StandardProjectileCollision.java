package com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.mygdx.holowyth.pathfinding.HoloPF;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;

/**
 * Collision collides against both enemy units and obstacles
 *
 */
@NonNullByDefault
public class StandardProjectileCollision extends ProjectileCollision {

	protected float collisionRadius = 0;
	
	public StandardProjectileCollision(Projectile self) {
		super(self);
	}

	@Override
	protected void detectCollisionsWithEnemies() {
		for (Unit enemy : getEnemies()) {
			if (Point.dist(pos, enemy.getPos()) < enemy.getRadius()) {
				onCollision(enemy);
				collided = true;
				return;
			}
		}
	}

	protected List<Unit> getEnemies() {
		var units = new ArrayList<Unit>(mapInstance.getUnits());
		var targets = new ArrayList<Unit>();
		if (side == Side.PLAYER) {
			CollectionUtils.select(units, (u) -> u.getSide() == Side.ENEMY, targets);
		} else {
			CollectionUtils.select(units, (u) -> u.getSide() == Side.PLAYER, targets);
		}
		return targets;
	}
	
	
	@Override
	protected void detectCollisionWithObstacles() {
		PathingModule pathing = mapInstance.getPathingModule();
		final var motion = new Segment(pos.x, pos.y, pos.x + getVx(), pos.y + getVy());
		if (!HoloPF.isSegmentPathableAgainstObstaclesNonExpandedSeg(motion, pathing.getObstacleSegs(), pathing.getObstaclePoints(),
				collisionRadius)) {
			onCollisionWithObstacle();
			collided = true;
		}
	}
	
	public final void setCollisionRadius(float collisionRadius) {
		this.collisionRadius = collisionRadius;
	}
	
	
}
