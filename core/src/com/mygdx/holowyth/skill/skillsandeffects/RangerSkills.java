package com.mygdx.holowyth.skill.skillsandeffects;

import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.unit.Unit;

public class RangerSkills {
	public static class CrossSlash extends NoneSkill {
		public CrossSlash() {
			super();
			name = "Cross Slash";
			casting.castTime = 60 * 0.5f;
			casting.isInterruptedByDamageOrReel = false;
			spCost = 14;
			cooldown = 60 * 10;
		}

		@Override
		public boolean pluginTargeting(Unit caster) {
			if (!caster.isAttacking())
				return false;
			setEffects(new RangerEffects.CrossSlashEffect(caster, caster.getAttacking()));
			return true;
		}
	}
}
