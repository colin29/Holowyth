package com.mygdx.holowyth.gamedata.skillsandeffects;

import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.skill.skill.UnitSkill;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.DataUtil;

public class PriestSkills {
	public static class Heal extends UnitSkill {
		
		public static int healAmount = 15;
		
		public Heal() {
			super();
			name = "Heal";
			casting.castTime = 60 * 1.5f;
			spCost = 12;
			cooldown = 12 * 60;
			setMaxRange(150);
			addTag(Tag.ALLIED_TARGETING);
		}

		@Override
		protected boolean pluginTargeting(Unit caster, Unit target) {
			setEffects(new PriestEffects.HealEffect(caster, target));
			return true;
		}

		@Override
		public String getDescription() {
			return String.format("Heals an ally for %s", healAmount);
		}

	}
	
	public static class StaffStrike extends NoneSkill {
		
		public static float atkdmgMultiplier = 1;
		public static int atkBonus = 5;
		public static int reelDuration = 2 * 60;
		
		public StaffStrike() {
			super();
			name = "Staff Strike";
			casting.setCastTimeSec(1.5f);
			spCost = 12;
			setCooldownSec(12);
			isMeleeSkill = true;
		}

		@Override
		public boolean pluginTargeting(Unit caster) {
			if (!caster.isAttacking())
				return false;
			setEffects(new PriestEffects.StaffStrikeEffect(caster, caster.getAttacking()));
			return true;
		}

		@Override
		public String getDescription() {
			return String.format("Smashes the target with a staff, doing %s damage and reeling the target for %s seconds",  DataUtil.percentage(atkdmgMultiplier), reelDuration);
		}

	}
}
