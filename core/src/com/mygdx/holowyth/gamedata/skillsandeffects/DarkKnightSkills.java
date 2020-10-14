package com.mygdx.holowyth.gamedata.skillsandeffects;

import com.mygdx.holowyth.skill.ActiveSkill.Tag;
import com.mygdx.holowyth.skill.skill.GroundSkill;
import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.skill.skill.UnitSkill;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.DataUtil;

public class DarkKnightSkills {
	public static class BladeInTheDark extends GroundSkill {
		public static float atkdmgMultiplier = 1.25f;
		public static int atkBonus = 6;
		public static float movementSpeedBuff = 0.25f;
		public static float movementSpeedBuffDuration = 5 * 60;

		public BladeInTheDark() {
			name = "Blade in the Dark";
			casting.castTime = 0.3f * 60;
			casting.isInterruptedByDamageOrReel = false;
			spCost = 8;
			cooldown = 15 * 60;
			globalCooldown = 3 * 60;
		}

		@Override
		public boolean pluginTargeting(Unit caster, float x, float y) {
			if (!caster.isAttacking()) {
				return false;
			} else {
				setEffects(new DarkKnightEffects.BladeInTheDarkEffect(caster, caster.getAttacking(), x, y));
				return true;
			}
		}

		@Override
		public String getDescription() {
			return String.format(
					"Strike the enemy for %s damage and disengage without penalty, gaining %s movement speed for %s seconds.",
					DataUtil.percentage(atkdmgMultiplier), DataUtil.percentage(movementSpeedBuff),
					DataUtil.asSeconds(movementSpeedBuffDuration));
		}
	}

	public static class ThrowKnife extends UnitSkill {

		public static float atkDamageMultiplier = 1.25f;
		public static int atkBonus = 15;
		public static int knifeDamage = 5;

		public static float bleedDamageRatio = 1f;
		public static int bleedTickInterval = 2 * 60;
		public static int bleedTotalTicks = 5;

		public ThrowKnife() {
			super();
			name = "Throw Knife";
			casting.castTime = 0.4f * 60;
			spCost = 8;
			cooldown = 10 * 60;
			requiresLOS = true;
			setMaxRange(300);
		}

		@Override
		protected boolean pluginTargeting(Unit caster, Unit target) {
			setEffects(new DarkKnightEffects.ThrowKnifeEffect(caster, target));
			return true;
		}

		@Override
		public String getDescription() {
			return String.format(
					"Throw a knife at the target dealing %s+%s damage, and inflicting 'Bleeding' for %s of damage dealt over %s seconds. Does not benefit from weapons or shields.",
					DataUtil.percentage(atkDamageMultiplier), knifeDamage, DataUtil.percentage(bleedDamageRatio),
					DataUtil.asSeconds(bleedTickInterval * bleedTotalTicks));
		}

	}
}
