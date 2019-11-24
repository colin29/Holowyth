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
			setEffects(new StunTestEffect(caster, caster.getAttacking()));
			return true;
		}
	}

	private static class StunTestEffect extends CasterUnitEffect {
		public StunTestEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		@Override
		public void begin() {
		}

		@Override
		public void tick() {
			target.stats.applyDamage(caster.stats.getDamage() * 3);
			target.stats.doStunRollAgainst(15, 90);
			markAsComplete();
		}
	}

}
