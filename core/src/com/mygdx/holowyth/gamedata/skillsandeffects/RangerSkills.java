package com.mygdx.holowyth.gamedata.skillsandeffects;

import com.mygdx.holowyth.gamedata.skillsandeffects.RangerEffects.CrossSlashEffect;
import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.DataUtil;

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

		@Override
		public String getDescription() {
			return String.format(
					"Perform two cross shaped slashes, dealing %sx2x2 damage. If the opponent blocks the attack, reel the opponent for up to 3 seconds.",
					DataUtil.roundFully(CrossSlashEffect.strikeDamage));
		}
	}
}
