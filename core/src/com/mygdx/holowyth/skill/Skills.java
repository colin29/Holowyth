package com.mygdx.holowyth.skill;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.skill.effect.UnitEffect;
import com.mygdx.holowyth.skill.effect.UnitGroundEffect;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * Static class that holds a collection of skills as well as their associated effects
 * 
 * @author Colin Ta
 *
 */
public class Skills {

	/**
	 * Deals damage to all units in a radius around the caster
	 * 
	 * @param unit
	 */
	private static class NovaFlareEffect extends UnitEffect {
		public NovaFlareEffect(Unit caster) {
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
			x = source.x;
			y = source.y;
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
			List<Unit> units = source.getWorldMutable().getUnits();
			for (Unit unit : units) {
				if (Unit.getDist(source, unit) <= splashRadius) {
					if (unit != source) {
						unit.stats.applyDamage(damage);
					}
				}
			}
		}

		@Override
		public boolean isComplete() {
			return secondaryExplosionFired;
		}

	};

	public static class NovaFlare extends NoneSkill {
		public NovaFlare() {
			super();
			name = "Nova Flare";
			spCost = 10;
			cooldown = 60;
		}

		@Override
		public void pluginTargeting(Unit caster) {
			setEffects(new NovaFlareEffect(caster));
		}
	}

	private static class ExplosionEffect extends UnitGroundEffect {
		public ExplosionEffect(Unit caster, float x, float y) {
			super(caster, x, y);
		}

		int mainDamage = 25;
		float splashRadius = 70;
		boolean fired;

		@Override
		public void begin() {
		}

		@Override
		public void tick() {
			applySplashAroundLocation(x, y, splashRadius, mainDamage);
			fired = true;
		}

		public void applySplashAroundLocation(float x, float y, float splashRadius, int damage) {
			List<Unit> units = source.getWorldMutable().getUnits();

			Point p = new Point(x, y);
			for (Unit unit : units) {
				if (Point.calcDistance(p, unit.getPos()) <= splashRadius) {
					if (unit != source) {
						unit.stats.applyDamage(damage);
					}
				}
			}
		}

		@Override
		public boolean isComplete() {
			return fired;
		}
	};

	public static class Explosion extends GroundSkill {
		public Explosion() {
			super();
			name = "Explosion";
			casting.castTime = 60;
			spCost = 10;
			cooldown = 20;
		}

		@Override
		public void pluginTargeting(Unit caster, float x, float y) {
			setEffects(new ExplosionEffect(caster, x, y));
		}
	}

	public static class ExplosionLongCast extends GroundSkill {
		public ExplosionLongCast() {
			super();
			name = "Explosion";
			casting.castTime = 200;
			spCost = 10;
			cooldown = 20;
		}

		@Override
		public void pluginTargeting(Unit caster, float x, float y) {
			setEffects(new ExplosionEffect(caster, x, y));
		}
	}

	/**
	 * Skill that knocks back nearby enemies towards the focal point
	 * 
	 * @author Colin Ta
	 *
	 */
	public static class Implosion extends GroundSkill {
		public Implosion() {
			super();
			name = "Implosion";
			casting.castTime = 0;
			spCost = 0;
			cooldown = 20;
		}

		@Override
		public void pluginTargeting(Unit caster, float x, float y) {
			setEffects(new ImplosionEffect(caster, x, y));
		}
	}

	private static class ImplosionEffect extends UnitGroundEffect {
		public ImplosionEffect(Unit caster, float x, float y) {
			super(caster, x, y);
		}

		int damage = 10;
		float effectRadius = 200;
		boolean tickedOnce;

		@Override
		public void begin() {
		}

		@Override
		public void tick() {
			knockBackEnemiesInRadiusTowardsCenter(x, y, effectRadius, damage);
			tickedOnce = true;
		}

		public void knockBackEnemiesInRadiusTowardsCenter(float effectX, float effectY, float effectRadius,
				int damage) {
			List<Unit> units = source.getWorldMutable().getUnits();

			final float knockbackSpeed = 3;

			Point effectCenter = new Point(effectX, effectY);
			for (Unit unit : units) {
				if (Point.calcDistance(effectCenter, unit.getPos()) <= effectRadius) {
					if (unit.getSide() != source.getSide()) {
						// knockback the units

						Vector2 unitToEffectCenter = new Vector2(effectX - unit.x, effectY - unit.y);
						Vector2 knockBackVel = new Vector2(unitToEffectCenter).nor().scl(knockbackSpeed);

						unit.motion.beginKnockback(knockBackVel.x, knockBackVel.y);
					}
				}
			}
		}

		@Override
		public boolean isComplete() {
			return tickedOnce;
		}
	};

}
