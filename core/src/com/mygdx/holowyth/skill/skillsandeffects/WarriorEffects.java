package com.mygdx.holowyth.skill.skillsandeffects;

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
}
