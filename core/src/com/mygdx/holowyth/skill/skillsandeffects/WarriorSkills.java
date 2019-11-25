package com.mygdx.holowyth.skill.skillsandeffects;

import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.unit.Unit;

public class WarriorSkills {

	public static class RageBlow extends NoneSkill {
		public RageBlow() {
			super();
			name = "Rage Blow";
			casting.castTime = 60 * 0.8f;
			casting.isInterruptedByDamageOrReel = false;
			spCost = 8;
			cooldown = 60 * 15;
		}

		@Override
		public boolean pluginTargeting(Unit caster) {
			if (!caster.isAttacking())
				return false;
			setEffects(new WarriorEffects.RageBlowEffect(caster, caster.getAttacking()));
			return true;
		}
	}

}
