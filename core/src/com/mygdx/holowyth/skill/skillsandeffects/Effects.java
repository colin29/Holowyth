package com.mygdx.holowyth.skill.skillsandeffects;

import java.util.List;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.combatDemo.World;
import com.mygdx.holowyth.skill.effect.CasterEffect;
import com.mygdx.holowyth.skill.effect.CasterGroundEffect;
import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.skill.effect.CasterUnitGroundEffect;
import com.mygdx.holowyth.skill.effect.Effect;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.ShapeDrawerPlus;
import com.mygdx.holowyth.util.dataobjects.Point;

public class Effects {

	/**
	 * Deals damage to all units in a radius around the caster
	 * 
	 * @param unit
	 */
	static class StaticShockEffect extends CasterEffect {
		public StaticShockEffect(Unit caster) {
			super(caster);
		}

		int time;
		int timeSecondaryExplosion = 30;
		int mainDamage = 20;
		int secondaryDamage = 5;
		float splashRadius = 70;
		boolean secondaryExplosionFired;

		float x, y;

		@Override
		public void begin() {
			x = caster.x;
			y = caster.y;
			time = 0;
		}

		@Override
		public void tick() {
			if (time == 0) {
				applySplashAroundLocation(x, y, splashRadius, mainDamage);
			}
			if (time == timeSecondaryExplosion) {
				applySplashAroundLocation(x, y, splashRadius, secondaryDamage);
				secondaryExplosionFired = true;
			}
			time += 1;
		}

		public void applySplashAroundLocation(float x, float y, float splashRadius, int damage) {
			List<Unit> units = caster.getWorldMutable().getUnits();
			for (Unit unit : units) {
				if (Unit.getDist(caster, unit) <= splashRadius) {
					if (unit != caster) {
						unit.stats.applyMagicDamage(damage);
					}
				}
			}
		}

		@Override
		public boolean isComplete() {
			return secondaryExplosionFired;
		}

	}

	static class ExplosionEffect extends CasterGroundEffect {
		public ExplosionEffect(Unit caster, float x, float y) {
			super(caster, x, y);
		}

		int mainDamage = 25;

		static float aoeRadius = 70;
		boolean fired;

		@Override
		public void tick() {
			applySplashAroundLocation(ground.x, ground.y, aoeRadius, mainDamage);
			fired = true;
		}

		public void applySplashAroundLocation(float x, float y, float splashRadius, int damage) {
			List<Unit> units = caster.getWorldMutable().getUnits();

			Point p = new Point(x, y);
			for (Unit unit : units) {
				if (Point.calcDistance(p, unit.getPos()) <= splashRadius) {
					if (unit != caster) {
						unit.stats.applyMagicDamage(damage);
					}
				}
			}
		}

		@Override
		public boolean isComplete() {
			return fired;
		}
	}

	static class ImplosionEffect extends CasterGroundEffect {
		public ImplosionEffect(Unit caster, float x, float y) {
			super(caster, x, y);
		}

		int damage = 10;
		static float aoeRadius = 200;

		final float knockbackSpeed = 2;

		@Override
		public void tick() {
			knockBackEnemiesInRadiusTowardsCenter(ground.x, ground.y, aoeRadius, damage);
			markAsComplete();
		}

		public void knockBackEnemiesInRadiusTowardsCenter(float effectX, float effectY, float effectRadius,
				int damage) {
			List<Unit> units = caster.getWorldMutable().getUnits();

			Point effectCenter = new Point(effectX, effectY);
			for (Unit unit : units) {
				if (Point.calcDistance(effectCenter, unit.getPos()) <= effectRadius) {
					if (unit.getSide() != caster.getSide()) {
						// knockback the units

						Vector2 unitToEffectCenter = new Vector2(effectX - unit.x, effectY - unit.y);
						Vector2 knockBackVel = new Vector2(unitToEffectCenter).nor().scl(knockbackSpeed);

						unit.stats.applyKnockbackStun(0, 0, knockBackVel);
					}
				}
			}
		}

	};

	static class NovaFlareEffect extends CasterGroundEffect {
		public NovaFlareEffect(Unit caster, float x, float y) {
			super(caster, x, y);
		}

		int damage = 35;
		static float aoeRadius = 200;

		final float knockbackMagnitude = 2.5f;

		@Override
		public void tick() {
			knockBackEnemiesWithinRadius(ground.x, ground.y, aoeRadius, damage);
			var units = world.getUnits();
			Point effectCenter = new Point(ground.x, ground.y);
			for (Unit unit : units) {
				if (Point.calcDistance(effectCenter, unit.getPos()) <= aoeRadius) {
					if (unit != caster) {
						unit.stats.applyMagicDamage(damage);
					}
				}
			}

			markAsComplete();
		}

		public void knockBackEnemiesWithinRadius(float effectX, float effectY, float effectRadius,
				int damage) {
			List<Unit> units = caster.getWorldMutable().getUnits();

			Point effectCenter = new Point(effectX, effectY);
			for (Unit unit : units) {
				if (Point.calcDistance(effectCenter, unit.getPos()) <= effectRadius) {
					if (unit != caster) {
						// knockback the units

						Vector2 unitToEffectCenter = new Vector2(unit.x - effectX, unit.y - effectY);
						Vector2 knockBackVel = new Vector2(unitToEffectCenter).nor().scl(knockbackMagnitude);

						unit.stats.applyKnockbackStun(0, 0, knockBackVel);
					}
				}
			}
		}

	};

	static class ForcePushEffect extends CasterUnitGroundEffect {

		final float knockbackSpeedBase = 1.2f;

		protected ForcePushEffect(Unit caster, Unit target, float x, float y) {
			super(caster, target, x, y);
		}

		@Override
		public void tick() {
			Vector2 targetToPoint = new Vector2(x - target.x, y - target.y);

			final float knockBackSpeed = knockbackSpeedBase * targetToPoint.len() / 100;
			Vector2 knockBackVel = new Vector2(targetToPoint).nor().scl(knockBackSpeed);

			target.stats.applyKnockbackStun(60, 60, knockBackVel);
			markAsComplete();
		}

	}

	static class StunTestEffect extends CasterUnitEffect {
		public StunTestEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		@Override
		public void tick() {
			target.stats.doStunRollAgainst(15, 90);
			markAsComplete();
		}
	}

	static class CircleOutlineVfx extends Effect {

		private static int vfxDuration = 80;

		private float x, y;
		private float aoeRadius;
		private int framesElapsed = 0;

		private Color color;

		public CircleOutlineVfx(float x, float y, float aoeRadius, Color vfxColor, World world) {
			super(world);
			this.aoeRadius = aoeRadius;

			this.x = x;
			this.y = y;
			this.color = vfxColor;
		}

		@Override
		public void tick() {
			if (framesElapsed >= vfxDuration) {
				markAsComplete();
			}
			framesElapsed += 1;
		}

		@Override
		public void render(SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
			shapeDrawer.setColor(color, getOpacity());

			batch.begin();
			shapeDrawer.circle(x, y, aoeRadius);
			batch.end();
		}

		private float getOpacity() {
			return 0.9f * (1 - framesElapsed / (float) vfxDuration);
		}

	}

}
