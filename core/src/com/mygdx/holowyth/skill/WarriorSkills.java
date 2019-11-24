package com.mygdx.holowyth.skill;

import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.skill.skilltypes.NoneSkill;
import com.mygdx.holowyth.unit.Unit;

public class WarriorSkills {

	public static class RageBlowSkill extends NoneSkill {
		public RageBlowSkill() {
			super();
			name = "Rage Blow";
			casting.castTime = 60 * 0.8f;
			spCost = 8;
			cooldown = 60 * 15;
		}

		@Override
		public boolean pluginTargeting(Unit caster) {
			if (!caster.isAttacking())
				return false;
			setEffects(new RageBlowEffect(caster, caster.getAttacking()));
			return true;
		}
	}

	private static class RageBlowEffect extends CasterUnitEffect {
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
