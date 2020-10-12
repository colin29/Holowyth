package com.mygdx.holowyth.gamedata.skillsandeffects;

import com.mygdx.holowyth.gamedata.skillsandeffects.DarkKnightSkills.BladeInTheDark;
import com.mygdx.holowyth.gamedata.skillsandeffects.DarkKnightSkills.ThrowSand;
import com.mygdx.holowyth.gamedata.skillsandeffects.PriestSkills.Heal;
import com.mygdx.holowyth.graphics.effects.animated.EffectCenteredOnUnit;
import com.mygdx.holowyth.skill.effect.CasterEffect;
import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.skill.effect.CasterUnitGroundEffect;
import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.DataUtil;

public class DarkKnightEffects {

	public static class BladeInTheDarkEffect extends CasterUnitGroundEffect {

		protected BladeInTheDarkEffect(Unit caster, Unit target, float x, float y) {
			super(caster, target, x, y);
		}

		@Override
		public void tick() {
			if (caster.stats.isAttackRollSuccessful(target.stats, BladeInTheDark.atkBonus)) {
				target.stats.applyDamage(caster.stats.getDamage() * BladeInTheDark.atkdmgMultiplier);
			} else {
				gfx.makeMissEffect(caster);
			}
			caster.getCombat().retreatAvoidAOPfromTarget(x, y);
			caster.status.applySpeedIncrease(BladeInTheDark.movementSpeedBuff,
					BladeInTheDark.movementSpeedBuffDuration);
			markAsComplete();
		}
	}

	static class ThrowSandEffect extends CasterUnitEffect {

		public ThrowSandEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		@Override
		public void tick() {
			if (caster.stats.isAttackRollSuccessful(target.stats, ThrowSand.atkRollBonus)) {
				target.status.applyBlind(ThrowSand.blindDuration);
			} else {
				gfx.makeMissEffect(caster);
			}

			markAsComplete();
		}
	}

}
