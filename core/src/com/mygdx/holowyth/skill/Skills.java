package com.mygdx.holowyth.skill;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.skill.effect.CasterEffect;
import com.mygdx.holowyth.skill.effect.CasterGroundEffect;
import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.skill.effect.CasterUnitGroundEffect;
import com.mygdx.holowyth.skill.effect.projectiles.MagicMissileBolt;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.ShapeDrawerPlus;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * Static class that holds a collection of skills as well as their associated effects
 * 
 * @author Colin Ta
 *
 */
public class Skills {

	protected static Logger logger = LoggerFactory.getLogger(Skills.class);

	/**
	 * Deals damage to all units in a radius around the caster
	 * 
	 * @param unit
	 */
	private static class StaticShockEffect extends CasterEffect {
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

	public static class StaticShock extends NoneSkill {
		public StaticShock() {
			super();
			name = "Static Shock";
			spCost = 10;
			cooldown = 60;
		}

		@Override
		public void pluginTargeting(Unit caster) {
			setEffects(new StaticShockEffect(caster));
		}
	}

	private static class ExplosionEffect extends CasterGroundEffect {
		public ExplosionEffect(Unit caster, float x, float y) {
			super(caster, x, y);
		}

		int mainDamage = 25;

		static float aoeRadius = 70;
		boolean fired;

		@Override
		public void begin() {
		}

		@Override
		public void tick() {
			applySplashAroundLocation(groundX, groundY, aoeRadius, mainDamage);
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
			aimingHelperRadius = ExplosionEffect.aoeRadius;
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
			aimingHelperRadius = ImplosionEffect.aoeRadius;
		}

		@Override
		public void pluginTargeting(Unit caster, float x, float y) {
			setEffects(new ImplosionEffect(caster, x, y));
		}
	}

	private static class ImplosionEffect extends CasterGroundEffect {
		public ImplosionEffect(Unit caster, float x, float y) {
			super(caster, x, y);
		}

		int damage = 10;
		static float aoeRadius = 200;

		final float knockbackSpeed = 2;

		@Override
		public void begin() {
		}

		@Override
		public void tick() {
			knockBackEnemiesInRadiusTowardsCenter(groundX, groundY, aoeRadius, damage);
			markAsComplete();
		}

		public void knockBackEnemiesInRadiusTowardsCenter(float effectX, float effectY, float effectRadius,
				int damage) {
			List<Unit> units = source.getWorldMutable().getUnits();

			Point effectCenter = new Point(effectX, effectY);
			for (Unit unit : units) {
				if (Point.calcDistance(effectCenter, unit.getPos()) <= effectRadius) {
					if (unit.getSide() != source.getSide()) {
						// knockback the units

						Vector2 unitToEffectCenter = new Vector2(effectX - unit.x, effectY - unit.y);
						Vector2 knockBackVel = new Vector2(unitToEffectCenter).nor().scl(knockbackSpeed);

						unit.motion.applyKnockBackVelocity(knockBackVel.x, knockBackVel.y);
					}
				}
			}
		}

	};

	/**
	 * Skill that knocks back nearby enemies towards the focal point
	 * 
	 * @author Colin Ta
	 *
	 */
	public static class NovaFlare extends GroundSkill {
		public NovaFlare() {
			super();
			name = "Nova Flare";
			casting.castTime = 200;
			spCost = 10;
			cooldown = 20;
			aimingHelperRadius = NovaFlareEffect.aoeRadius;
		}

		@Override
		public void pluginTargeting(Unit caster, float x, float y) {
			setEffects(new NovaFlareEffect(caster, x, y));
		}
	}

	private static class NovaFlareEffect extends CasterGroundEffect {
		public NovaFlareEffect(Unit caster, float x, float y) {
			super(caster, x, y);
		}

		int damage = 35;
		static float aoeRadius = 200;

		final float knockbackMagnitude = 2.5f;

		@Override
		public void begin() {
		}

		@Override
		public void tick() {
			knockBackEnemiesWithinRadius(groundX, groundY, aoeRadius, damage);
			var units = world.getUnits();
			Point effectCenter = new Point(groundX, groundY);
			for (Unit unit : units) {
				if (Point.calcDistance(effectCenter, unit.getPos()) <= aoeRadius) {
					if (unit != source) {
						unit.stats.applyDamage(damage);
					}
				}
			}

			markAsComplete();
		}

		public void knockBackEnemiesWithinRadius(float effectX, float effectY, float effectRadius,
				int damage) {
			List<Unit> units = source.getWorldMutable().getUnits();

			Point effectCenter = new Point(effectX, effectY);
			for (Unit unit : units) {
				if (Point.calcDistance(effectCenter, unit.getPos()) <= effectRadius) {
					if (unit != source) {
						// knockback the units

						Vector2 unitToEffectCenter = new Vector2(unit.x - effectX, unit.y - effectY);
						Vector2 knockBackVel = new Vector2(unitToEffectCenter).nor().scl(knockbackMagnitude);

						unit.motion.applyKnockBackVelocity(knockBackVel.x, knockBackVel.y);
					}
				}
			}
		}

	};

	public static class ForcePush extends UnitGroundSkill {
		{
			name = "Force Push";
		}

		@Override
		public void pluginTargeting(Unit caster, Unit target, float x, float y) {
			setEffects(new ForcePushEffect(caster, target, x, y));
		}
	}

	public static class ForcePushEffect extends CasterUnitGroundEffect {

		final float knockbackSpeedBase = 1.2f;

		protected ForcePushEffect(Unit caster, Unit target, float x, float y) {
			super(caster, target, x, y);
		}

		@Override
		public void begin() {
			Vector2 targetToPoint = new Vector2(x - target.x, y - target.y);

			final float knockBackSpeed = knockbackSpeedBase * targetToPoint.len() / 100;
			Vector2 knockBackVel = new Vector2(targetToPoint).nor().scl(knockBackSpeed);
			if (knockBackVel.isZero()) // cover degenerate case
				targetToPoint.set(knockBackSpeed, 0);

			target.motion.applyKnockBackVelocity(knockBackVel.x, knockBackVel.y);
			markAsComplete();
		}

		@Override
		public void tick() {
		}

	}

	/**
	 * 
	 * @author Colin Ta
	 *
	 */
	public static class MagicMissile extends UnitSkill {
		public MagicMissile() {
			super();
			name = "Magic Missile";
			casting.castTime = 0;
			spCost = 10;
			cooldown = 5;
		}

		@Override
		public void pluginTargeting(Unit caster, Unit target) {
			setEffects(new MagicMissileEffect(caster, target));
		}
	}

	private static class MagicMissileEffect extends CasterUnitEffect {
		public MagicMissileEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		int damage = 8;
		List<MagicMissileBolt> missiles;

		float missileVfxRadius = 5;

		@Override
		public void begin() {
			missiles = new ArrayList<MagicMissileBolt>();
			for (int i = 0; i < 3; i++) {
				missiles.add(new MagicMissileBolt(source.x, source.y + 18 * i, damage, target, source, world.getUnits())); // space the missiles out
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
				if (m.isSideWindingLeft()) {
					shapeDrawer.setColor(Color.CYAN, 0.8f);
				} else {
					shapeDrawer.setColor(Color.RED, 0.8f);
				}
				shapeDrawer.filledCircle(m.x, m.y, missileVfxRadius);
			}
			batch.end();
		}

	};

	public static class StunTestSkill extends UnitSkill {
		public StunTestSkill() {
			super();
			name = "StunTestSkill";
			casting.castTime = 0;
			spCost = 10;
			cooldown = 5;
		}

		@Override
		public void pluginTargeting(Unit caster, Unit target) {
			setEffects(new StunTestEffect(caster, target));
		}
	}

	private static class StunTestEffect extends CasterUnitEffect {
		public StunTestEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		@Override
		public void begin() {
		}

		@Override
		public void tick() {
			logger.debug("applied stun");
			target.stats.applyStun(90);
			markAsComplete();
		}
	}

}
