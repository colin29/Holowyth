package com.mygdx.holowyth.gamedata.skillsandeffects;

import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.unit.Unit;

public class PriestEffects {

	static class HealEffect extends CasterUnitEffect {
		
		public static int healAmount = 15;
		
		public HealEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		@Override
		public void tick() {
			target.stats.applyHeal(healAmount);
			markAsComplete();
		}
	}
}
