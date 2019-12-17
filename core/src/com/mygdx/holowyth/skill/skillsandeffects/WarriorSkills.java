package com.mygdx.holowyth.skill.skillsandeffects;

import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.unit.Unit;

public class WarriorSkills {

	public static class RageBlow extends NoneSkill {
		public RageBlow() {
			super();
			name = "Rage Blow";
			casting.castTime = 60 * 1f;
			casting.isInterruptedByDamageOrReel = false;
			spCost = 12;
			cooldown = 60 * 17;
		}

		@Override
		public boolean pluginTargeting(Unit caster) {
			if (!caster.isAttacking())
				return false;
			setEffects(new WarriorEffects.RageBlowEffect(caster, caster.getAttacking()));
			return true;
		}
	}

	public static class Bash extends NoneSkill {
		public Bash() {
			super();
			name = "Bash";
			casting.castTime = 60 * 0.7f;
			casting.isInterruptedByDamageOrReel = false;
			spCost = 8;
			cooldown = 60 * 13;
		}

		@Override
		public boolean pluginTargeting(Unit caster) {
			if (!caster.isAttacking())
				return false;
			setEffects(new WarriorEffects.BashEffect(caster, caster.getAttacking()));
			return true;
		}
	}

	public static class DeafeningCry extends NoneSkill {
		public DeafeningCry() {
			super();
			name = "Deafening Cry";
			casting.castTime = 60 * 0.9f;
			casting.isInterruptedByDamageOrReel = false;
			spCost = 12;
			cooldown = 60 * 20;
		}

		@Override
		public boolean pluginTargeting(Unit caster) {
			setEffects(new WarriorEffects.DeafeningCryEffect(caster));
			return true;
		}
	}

}
