package com.mygdx.holowyth.gamedata.skillsandeffects;

import com.mygdx.holowyth.gamedata.skillsandeffects.PriestSkills.Heal;
import com.mygdx.holowyth.gamedata.skillsandeffects.PriestSkills.StaffStrike;
import com.mygdx.holowyth.graphics.effects.animated.AnimEffectOnUnit;
import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.unit.Unit;

public class PriestEffects {

	static class HealEffect extends CasterUnitEffect {
		
		public HealEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		@Override
		public void tick() {
			final float healAmount;
			if(target.getCombat().getTimeOutOfCombat() >= Heal.outOfCombatTimeThreshold) {
				healAmount = Heal.healAmount * (1 + Heal.outOfCombathealingBonus);
			}else {
				healAmount = Heal.healAmount;
			}
			target.stats.applyHeal(healAmount, true);
			
			var effect = new AnimEffectOnUnit(target, "holy_cross.png", mapInstance, mapInstance.getAnimations());	
			effect.setSize(72, 72);
			effect.setAlpha(0.85f);
			gfx.addGraphicEffect(effect);
			gfx.makeHealEffect(healAmount, target);
			markAsComplete();
		}
	}

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
}
