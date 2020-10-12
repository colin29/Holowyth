package com.mygdx.holowyth.gamedata.skillsandeffects;

import com.mygdx.holowyth.gamedata.skillsandeffects.SwordsmanSkills.TripleStrike;
import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.unit.Unit;

public class SwordsmanEffects {
	public static class TripleStrikeEffect extends CasterUnitEffect {
		
		private int framesElapsed = 0;
		private int strikesDone = 0;
		
		protected TripleStrikeEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		@Override
		public void tick() {

			if(framesElapsed % 15 == 0) {
				if (caster.stats.isAttackRollSuccessful(target.stats, 5)) {
					target.stats.applyDamage(caster.stats.getDamage() * TripleStrike.atkdmgMultiplier);
				}else {
					gfx.makeMissEffect(caster);
				}
				strikesDone++;
			}
			if(strikesDone == 3) {
				markAsComplete();
			}
			framesElapsed++;
		}
	}
}
