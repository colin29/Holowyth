package com.mygdx.holowyth.skill.skillsandeffects;

import com.mygdx.holowyth.combatDemo.rendering.SkillsAimingGraphic;
import com.mygdx.holowyth.skill.skill.GroundSkill;
import com.mygdx.holowyth.skill.skill.UnitSkill;
import com.mygdx.holowyth.unit.Unit;

public class MageSkills {
	public static class MagicMissile extends UnitSkill {
		public MagicMissile() {
			super();
			name = "Magic Missile";
			casting.castTime = 60 * 0.8f;
			spCost = 12;
			cooldown = 60 * 12;
		}

		@Override
		public void pluginTargeting(Unit caster, Unit target) {
			setEffects(new MageEffects.MagicMissileEffect(caster, target));
		}
	}

	public static class ArcaneBolt extends UnitSkill {
		public ArcaneBolt() {
			super();
			name = "Arcane Bolt";
			casting.castTime = 60 * 1.3f;
			spCost = 22;
			cooldown = 60 * 18;
		}

		@Override
		public void pluginTargeting(Unit caster, Unit target) {
			setEffects(new MageEffects.ArcaneBoltEffect(caster, target));
		}
	}

	public static class WindBlades extends UnitSkill {
		public WindBlades() {
			super();
			name = "Wind Blades";
			casting.castTime = 60 * 0.5f;
			spCost = 10;
			cooldown = 60 * 8;
		}

		@Override
		public void pluginTargeting(Unit caster, Unit target) {
			setEffects(new MageEffects.WindBladesEffect(caster, target));
		}
	}

	public static class Fireball extends UnitSkill {
		public Fireball() {
			super();
			name = "Fireball";
			casting.castTime = 60 * 1.3f;
			spCost = 16;
			cooldown = 60 * 16;
		}

		@Override
		public void pluginTargeting(Unit caster, Unit target) {
			setEffects(new MageEffects.FireBallEffect(caster, target));
		}
	}

	public static class Hydroblast extends GroundSkill {
		public Hydroblast() {
			super();
			name = "Hydroblast";
			casting.castTime = 60 * 0.9f;
			spCost = 14;
			cooldown = 60 * 16;

			aimingGraphic = new SkillsAimingGraphic.HydroblastAimingGraphic(MageEffects.HydroblastEffect.coneLength,
					MageEffects.HydroblastEffect.coneInnerLength,
					MageEffects.HydroblastEffect.coneWidthDegrees,
					getWorld());
		}

		@Override
		public void pluginTargeting(Unit caster, float x, float y) {
			setEffects(new MageEffects.HydroblastEffect(caster, x, y));
		}
	}

}
