package com.mygdx.holowyth.skill.skillsandeffects;

import com.mygdx.holowyth.skill.skill.UnitSkill;
import com.mygdx.holowyth.unit.Unit;

public class MageSkills {
	public static class MagicMissile extends UnitSkill {
		public MagicMissile() {
			super();
			name = "Magic Missile";
			casting.castTime = 60 * 1;
			spCost = 10;
			cooldown = 60 * 12;
		}

		@Override
		public void pluginTargeting(Unit caster, Unit target) {
			setEffects(new MageEffects.MagicMissileEffect(caster, target));
		}
	}

	public static class WindBlades extends UnitSkill {
		public WindBlades() {
			super();
			name = "Wind Blades";
			casting.castTime = 30 * 1;
			spCost = 8;
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
			casting.castTime = 60 * 1;
			spCost = 16;
			cooldown = 60 * 16;
		}

		@Override
		public void pluginTargeting(Unit caster, Unit target) {
			setEffects(new MageEffects.FireBallEffect(caster, target));
		}
	}

}
