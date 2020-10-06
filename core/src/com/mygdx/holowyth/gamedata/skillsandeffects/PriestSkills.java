package com.mygdx.holowyth.gamedata.skillsandeffects;

import com.mygdx.holowyth.skill.skill.UnitSkill;
import com.mygdx.holowyth.unit.Unit;

public class PriestSkills {
	public static class HealSkill extends UnitSkill {
		public HealSkill() {
			super();
			name = "Heal";
			casting.castTime = 60 * 1.5f;
			spCost = 12;
			cooldown = 60 * 12;
			addTag(Tag.ALLIED_TARGETING);
		}

		@Override
		protected void pluginTargeting(Unit caster, Unit target) {
			setEffects(new PriestEffects.HealEffect(caster, target));
		}

		@Override
		public String getDescription() {
			return "Heals an ally";
		}

	}
}
