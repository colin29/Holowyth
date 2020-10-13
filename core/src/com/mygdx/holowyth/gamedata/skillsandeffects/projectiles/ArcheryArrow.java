package com.mygdx.holowyth.gamedata.skillsandeffects.projectiles;

import org.eclipse.jdt.annotation.NonNull;

import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.dataobjects.Point;

public class ArcheryArrow extends ProjectileBase {

	
	public final float damage;
	private static final float speed = 10;
	private static final float maxDuration = 60f;
	
	public boolean atkRollSucceeded; 
	
	public ArcheryArrow(float x, float y, float damage, @NonNull Unit caster, @NonNull Unit target) {
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
		if(atkRollSucceeded) {
			enemy.stats.applyDamage(damage);	
		}else {
			gfx.makeMissEffect(caster);
		}
		
	}

}
