package com.mygdx.holowyth.gamedata.skillsandeffects;

import com.mygdx.holowyth.gamedata.skillsandeffects.RangerEffects.CrossSlashEffect;
import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.DataUtil;

public class SwordsmanSkills {

	public static class TripleStrike extends NoneSkill {
		public static float atkdmgMultiplier = 0.75f;
		public TripleStrike() {
			name = "Triple Strike";
			casting.castTime = 0.4f * 60;
			casting.isInterruptedByDamageOrReel = false;
			spCost = 9;
			cooldown = 15 * 60;
			globalCooldown = 4 * 60;
		}

		@Override
		public boolean pluginTargeting(Unit caster) {
			if (!caster.isAttacking())
				return false;
			setEffects(new SwordsmanEffects.TripleStrikeEffect(caster, caster.getAttacking()));
			return true;
		}
		@Override
		public String getDescription() {
			return String.format("Strike the enemy rapidly, dealing 3x%s damage", DataUtil.percentage(atkdmgMultiplier));
		}

	}
}
