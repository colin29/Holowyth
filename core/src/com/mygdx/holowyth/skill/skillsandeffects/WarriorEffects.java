package com.mygdx.holowyth.skill.skillsandeffects;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.unit.Unit;

public class WarriorEffects {
	static class RageBlowEffect extends CasterUnitEffect {
		public RageBlowEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		@Override
		public void tick() {
			if (caster.stats.isAttackRollSuccessful(target.stats, 10)) {
				target.stats.applyDamage(caster.stats.getDamage() * 3);
			} else {
				world.getGfx().makeBlockEffect(caster, target);
			}

			target.stats.doStunRollAgainst(15, 90);
			markAsComplete();
		}
	}

	static class BashEffect extends CasterUnitEffect {
		public BashEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		@Override
		public void tick() {
			if (caster.stats.isAttackRollSuccessful(target.stats, 10)) {
				target.stats.applyDamage(caster.stats.getDamage() * 1);
			} else {
				world.getGfx().makeBlockEffect(caster, target);
			}
			var knockbackVec = new Vector2(1, 0).setAngle(angleFromCasterToTarget()).setLength(1.5f);
			target.stats.doKnockBackRollAgainst(15, 60 * 1, knockbackVec);
			markAsComplete();
		}
	}
}
