package com.mygdx.holowyth.skill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mygdx.holowyth.combatDemo.World;
import com.mygdx.holowyth.skill.Skill.Targeting;
import com.mygdx.holowyth.skill.effect.UnitEffect;
import com.mygdx.holowyth.skill.effect.UnitGroundEffect;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.data.Point;

/**
 * Static class that holds a collection of skills
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

		public void pluginTargeting(Unit caster) {
			setEffect(new ArrayList<UnitEffect>(Arrays.asList(new NovaFlareEffect(caster))));
		}
	}

	private static class ExplosionEffect extends UnitGroundEffect {
		public ExplosionEffect(Unit caster, float x, float y) {
			super(caster, x, y);
			System.out.println(x + " " + y);
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
			setEffect(new ArrayList<UnitEffect>(Arrays.asList(new ExplosionEffect(caster, x, y))));
		}
	}

}
