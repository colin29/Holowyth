package com.mygdx.holowyth.gamedata.skillsandeffects.projectiles;

import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.dataobjects.Point;

public class ArcheryArrow extends ProjectileBase {

	
	public final float damage;
	private static final float speed = 10;
	private static final float maxDuration = 60f;
	
	public ArcheryArrow(float x, float y, float damage, Unit caster, Unit target) {
		super(x, y, speed, Point.getAngleInDegrees(caster.getPos(), target.getPos()), maxDuration, caster);
		this.damage = damage;
	}

	@Override
	public void tick() {
		move();
		detectCollisionsWithEnemies();
		detectCollisionWithObstacles();
		tickDuration();
	}

	@Override
	protected void onCollision(Unit enemy) {
		enemy.stats.applyDamage(damage);
	}

}
