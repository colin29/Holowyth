package com.mygdx.holowyth.gamedata.skillsandeffects;

import com.mygdx.holowyth.gamedata.skillsandeffects.WarriorEffects.BashEffect;
import com.mygdx.holowyth.gamedata.skillsandeffects.WarriorEffects.DeafeningCryEffect;
import com.mygdx.holowyth.gamedata.skillsandeffects.WarriorEffects.RageBlowEffect;
import com.mygdx.holowyth.gamedata.skillsandeffects.WarriorEffects.TauntEffect;
import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.DataUtil;

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
			setEffects(new RageBlowEffect(caster, caster.getAttacking()));
			return true;
		}

		@Override
		public String getDescription() {
			return String.format("Strikes the enemy, doing %s attack damage and slowing the enemy for %s for %s seconds",
					DataUtil.percentage(RageBlowEffect.damageMultiplier),
					DataUtil.percentage(RageBlowEffect.slowAmount),
					DataUtil.asSeconds(RageBlowEffect.slowDuration));
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
			setEffects(new BashEffect(caster, caster.getAttacking()));
			return true;
		}

		@Override
		public String getDescription() {
			return String.format("Shoves the opponent, knocking them back. If attack roll suceeds, also does %s attack damage ",
					DataUtil.percentage(BashEffect.attackDamageMultiple));
		}

	}

	public static class Taunt extends NoneSkill {
		public Taunt() {
			super();
			name = "Taunt";
			casting.castTime = 0;
			spCost = 5;
			cooldown = 60 * 8;
		}

		@Override
		public boolean pluginTargeting(Unit caster) {
			if (!caster.isAttacking())
				return false;
			setEffects(new TauntEffect(caster, caster.getAttacking()));
			return true;
		}

		@Override
		public String getDescription() {
			return String.format("Taunts the enemy unit, forcing it to attack you for %s seconds.",
					DataUtil.asSeconds(TauntEffect.tauntDuration));
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
			setEffects(new DeafeningCryEffect(caster));
			return true;
		}

		@Override
		public String getDescription() {
			return String.format("Let out a bellowing howl that reels nearby units for %s seconds (+10 to save for allies).",
					DataUtil.asSeconds(DeafeningCryEffect.reelDuration));
		}
	}

}
