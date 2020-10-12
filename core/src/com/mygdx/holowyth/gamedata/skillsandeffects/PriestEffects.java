package com.mygdx.holowyth.gamedata.skillsandeffects;

import com.mygdx.holowyth.gamedata.skillsandeffects.PriestSkills.Heal;
import com.mygdx.holowyth.gamedata.skillsandeffects.PriestSkills.StaffStrike;
import com.mygdx.holowyth.graphics.effects.animated.EffectCenteredOnUnit;
import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.unit.Unit;

public class PriestEffects {

	static class StaffStrikeEffect extends CasterUnitEffect {
		public StaffStrikeEffect(Unit caster, Unit target) {
			super(caster, target);
		}
		@Override
		public void tick() {
			if (caster.stats.isAttackRollSuccessful(target.stats, StaffStrike.atkBonus)) {
				target.stats.applyDamage(caster.stats.getDamage() * StaffStrike.atkdmgMultiplier);
				target.stats.doReelRollAgainst(caster.stats.getForce(), StaffStrike.reelDuration);
			}else {
				gfx.makeMissEffect(caster);
			}
			markAsComplete();
		}
	}

	static class HealEffect extends CasterUnitEffect {
		
		public HealEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		@Override
		public void tick() {
			target.stats.applyHeal(Heal.healAmount);
			
			var effect = new EffectCenteredOnUnit(target, "holy_cross.png", mapInstance, mapInstance.getAnimations());	
			effect.setSize(72, 72);
			effect.setAlpha(0.85f);
			mapInstance.getGfx().addGraphicEffect(effect);
			markAsComplete();
		}
	}
}
