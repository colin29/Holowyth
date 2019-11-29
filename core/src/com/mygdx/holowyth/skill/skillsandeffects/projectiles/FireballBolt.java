package com.mygdx.holowyth.skill.skillsandeffects.projectiles;

import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.dataobjects.Point;

public class FireballBolt extends ProjectileBase {

	private static float maxDuration = 800;
	private static float speed = 1.8f;

	private float damage;

	public FireballBolt(float damage, Unit caster, Unit target) {
		super(caster.x, caster.y, speed, Point.getAngleInDegrees(caster.getPos(), target.getPos()), maxDuration, caster);
		this.damage = damage;
	}

	@Override
	protected void onCollision(Unit enemy) {
		enemy.stats.applyDamageIgnoringArmor(damage);
	}

	@Override
	public void tick() {
		move();
		detectCollisionsWithEnemies();
		tickDuration();
	}

}