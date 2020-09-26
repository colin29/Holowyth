package com.mygdx.holowyth.skill.skillsandeffects;

import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.unit.Unit;

public class RangerEffects {
	static class CrossSlashEffect extends CasterUnitEffect {
		public CrossSlashEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		int framesElapsed = 0;
		static int secondStrikeDelay = 60 / 2;
		static int delayBetweenStrikesInX = 6;
		static float strikeDamage = 8;
		boolean attackHits;

		@Override
		public void begin() {
			attackHits = caster.stats.isAttackRollSuccessful(target.stats, 5);
		}

		@Override
		public void tick() {

			if (attackHits) {
				if (framesElapsed == 0 ||
						framesElapsed == delayBetweenStrikesInX ||
						framesElapsed == secondStrikeDelay ||
						framesElapsed == secondStrikeDelay + delayBetweenStrikesInX) {
					target.stats.applyDamage(strikeDamage, true);
				}
			} else {
				if (framesElapsed == delayBetweenStrikesInX) {
					target.stats.doReelRollAgainst(15, 60 * 3);
				}
			}

			if (framesElapsed == 0 || framesElapsed == secondStrikeDelay) {
				mapInstance.getGfx().makeBlockEffect(caster, target);
			}

			if (framesElapsed == secondStrikeDelay + delayBetweenStrikesInX) {
				markAsComplete();
			}

			framesElapsed += 1;
		}
	}
}
