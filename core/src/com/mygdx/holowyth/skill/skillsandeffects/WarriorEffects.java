package com.mygdx.holowyth.skill.skillsandeffects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.skill.effect.CasterEffect;
import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.dataobjects.Point;

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

	public static class TauntEffect extends CasterUnitEffect {

		protected TauntEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		@Override
		public void tick() {
			target.setAttacking(caster);
			target.orderAttackUnit(caster, true);
			target.stats.applyTaunt(60 * 6, caster); // 60 * 2 sec
			target.interruptHard();
			markAsComplete();
		}

	}

	public static class DeafeningCryEffect extends CasterEffect {

		public DeafeningCryEffect(Unit caster) {
			super(caster);
		}

		static float aoeRadius = 80f;

		@Override
		public void tick() {
			for (Unit unit : world.getUnits()) {
				if (unit == caster)
					continue;
				if (Point.calcDistance(caster.getPos(), unit.getPos()) <= aoeRadius + unit.getRadius()) {
					if (unit.getSide() != caster.getSide()) {
						unit.stats.doReelRollAgainst(15, 60 * 4);
					} else {
						unit.stats.doReelRollAgainst(15, 60 * 2);
					}
				}
			}
			markAsComplete();
			world.addEffect(new Effects.CircleOutlineVfx(caster.x, caster.y, aoeRadius, Color.ORANGE, world));
		}

	}
}
