package com.mygdx.holowyth.gamedata.skillsandeffects.projectiles;

import org.eclipse.jdt.annotation.NonNull;

import com.mygdx.holowyth.gamedata.skillsandeffects.DarkKnightSkills.ThrowKnife;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.dataobjects.Point;

public class ThrowKnifeProjectile extends ProjectileBase {

	public final float damage;
	private static final float speed = 5;
	private static final float maxDuration = 60f;

	public int atk;

	public ThrowKnifeProjectile(float x, float y, int atk, float damage, @NonNull Unit caster, @NonNull Unit target) {
		super(x, y, speed, Point.getAngleInDegrees(caster.getPos(), target.getPos()), maxDuration, caster);
		this.atk = atk;
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
		if (caster.stats.isAttackRollSuccessfulCustomAtkValue(atk, enemy.stats, true)) {
			float damageDealt = enemy.stats.applyDamage(damage);
			enemy.status.applyBleed(damageDealt / ThrowKnife.bleedTotalTicks, ThrowKnife.bleedTotalTicks,
					ThrowKnife.bleedTotalTicks * ThrowKnife.bleedTickInterval);

		} else {
			gfx.makeMissEffect(caster);
		}

	}

}
