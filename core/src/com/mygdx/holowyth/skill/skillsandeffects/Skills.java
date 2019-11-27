package com.mygdx.holowyth.skill.skillsandeffects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.skill.skill.GroundSkill;
import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.skill.skill.UnitGroundSkill;
import com.mygdx.holowyth.skill.skill.UnitSkill;
import com.mygdx.holowyth.unit.Unit;

/**
 * Static class that holds a collection of skills as well as their associated effects
 * 
 * @author Colin Ta
 *
 */
public class Skills {

	protected static Logger logger = LoggerFactory.getLogger(Skills.class);

	public static class StaticShock extends NoneSkill {
		public StaticShock() {
			super();
			name = "Static Shock";
			spCost = 10;
			cooldown = 60;
		}

		@Override
		public boolean pluginTargeting(Unit caster) {
			setEffects(new Effects.StaticShockEffect(caster));
			return true;
		}
	}

	public static class Explosion extends GroundSkill {
		public Explosion() {
			super();
			name = "Explosion";
			casting.castTime = 60;
			spCost = 10;
			cooldown = 20;
			aimingHelperRadius = Effects.ExplosionEffect.aoeRadius;
		}

		@Override
		public void pluginTargeting(Unit caster, float x, float y) {
			setEffects(new Effects.ExplosionEffect(caster, x, y));
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
			casting.isInterruptedByDamageOrReel = true;
			spCost = 0;
			cooldown = 20;
			aimingHelperRadius = Effects.ImplosionEffect.aoeRadius;
		}

		@Override
		public void pluginTargeting(Unit caster, float x, float y) {
			setEffects(new Effects.ImplosionEffect(caster, x, y));
		}
	}

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
			aimingHelperRadius = Effects.NovaFlareEffect.aoeRadius;
		}

		@Override
		public void pluginTargeting(Unit caster, float x, float y) {
			setEffects(new Effects.NovaFlareEffect(caster, x, y));
		}
	}

	public static class ForcePush extends UnitGroundSkill {
		public ForcePush() {
			super();
			name = "Force Push";
		}

		@Override
		public void pluginTargeting(Unit caster, Unit target, float x, float y) {
			setEffects(new Effects.ForcePushEffect(caster, target, x, y));
		}
	}

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
			setEffects(new Effects.StunTestEffect(caster, target));
		}
	}

}
