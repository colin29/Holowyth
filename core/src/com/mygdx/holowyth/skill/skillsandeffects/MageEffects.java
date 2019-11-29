package com.mygdx.holowyth.skill.skillsandeffects;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.skill.skillsandeffects.projectiles.FireballBolt;
import com.mygdx.holowyth.skill.skillsandeffects.projectiles.MagicMissileBolt;
import com.mygdx.holowyth.skill.skillsandeffects.projectiles.Projectile;
import com.mygdx.holowyth.skill.skillsandeffects.projectiles.ProjectileBase;
import com.mygdx.holowyth.skill.skillsandeffects.projectiles.WindBladeBolt;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.ShapeDrawerPlus;

public class MageEffects {

	private static Logger logger = LoggerFactory.getLogger(MageEffects.class);

	static class MagicMissileEffect extends CasterUnitEffect {
		public MagicMissileEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		int damage = 5;
		List<MagicMissileBolt> missiles;

		float missileVfxRadius = 5;

		@Override
		public void begin() {
			missiles = new ArrayList<MagicMissileBolt>();
			for (int i = 0; i < 3; i++) {
				missiles.add(new MagicMissileBolt(caster.x, caster.y + 18 * i, damage, target, caster, world.getUnits())); // space the missiles out
			}
		}

		@Override
		public void tick() {

			for (var m : missiles) {
				m.tick();
			}
			missiles.removeIf(m -> m.duration <= 0 || m.collided);

			if (missiles.isEmpty())
				markAsComplete();
		}

		@Override
		public void render(SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
			shapeDrawer.setColor(Color.RED, 0.8f);
			batch.begin();
			for (var m : missiles) {
				shapeDrawer.filledCircle(m.x, m.y, missileVfxRadius);
			}
			batch.end();
		}

	};

	static class WindBladesEffect extends CasterUnitEffect {
		public WindBladesEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		int damage = 6;
		int numMissiles = 4;
		int missilesLeftToFire = numMissiles;
		int missileFiringInterval = 10;

		List<Projectile> missiles = new ArrayList<Projectile>();

		float missileVfxRadius = 4;

		int framesElapsed = 0;

		@Override
		public void tick() {

			if (framesElapsed % missileFiringInterval == 0 && missilesLeftToFire > 0) {
				missilesLeftToFire = Math.max(0, missilesLeftToFire - 1);
				missiles.add(new WindBladeBolt(caster.x, caster.y, damage, target, caster, world.getUnits())); // space the missiles out
			}

			for (var m : missiles) {
				m.tick();
			}
			missiles.removeIf(m -> m.isExpired() || m.isCollided());

			if (missilesLeftToFire == 0 && missiles.isEmpty())
				markAsComplete();

			framesElapsed += 1;
		}

		@Override
		public void render(SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
			shapeDrawer.setColor(HoloGL.rgb(179, 255, 25), 0.8f);
			batch.begin();
			for (var m : missiles) {
				shapeDrawer.filledCircle(m.getX(), m.getY(), missileVfxRadius);
			}
			batch.end();
		}
	}

	static class FireBallEffect extends CasterUnitEffect {

		int damage = 18;

		protected FireBallEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		ProjectileBase projectile;

		private static float missileVfxRadius = 6;

		@Override
		public void begin() {
			projectile = new FireballBolt(caster, target, damage, world);
		}

		@Override
		public void tick() {
			projectile.tick();
			if (projectile.isExpired() || projectile.isCollided())
				markAsComplete();
		}

		@Override
		public void render(SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
			shapeDrawer.setColor(Color.ORANGE, 1f);
			batch.begin();
			shapeDrawer.filledCircle(projectile.getX(), projectile.getY(), missileVfxRadius);
			batch.end();
		}

	}
}
