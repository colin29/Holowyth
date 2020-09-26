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

		static float attackDamageMultiple = 3;
		static float slowAmount = 0.6f;
		static int slowDuration = 60 * 2;

		@Override
		public void tick() {
			if (caster.stats.isAttackRollSuccessful(target.stats, 10)) {
				target.stats.applyDamage(caster.stats.getDamage() * attackDamageMultiple);
			} else {
				mapInstance.getGfx().makeBlockEffect(caster, target);
			}

			target.stats.applySlow(slowAmount, slowDuration);
			markAsComplete();
		}
	}

	static class BashEffect extends CasterUnitEffect {
		public BashEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		static float attackDamageMultiple = 1;

		@Override
		public void tick() {
			if (caster.stats.isAttackRollSuccessful(target.stats, 10)) {
				target.stats.applyDamage(caster.stats.getDamage() * attackDamageMultiple);
			} else {
				mapInstance.getGfx().makeBlockEffect(caster, target);
			}
			var knockbackVec = new Vector2(1, 0).setAngle(angleFromCasterToTarget()).setLength(1.5f);
			target.stats.doKnockBackRollAgainst(15, 60 * 1, knockbackVec);
			markAsComplete();
		}
	}

	public static class TauntEffect extends CasterUnitEffect {

		static int tauntDuration = 60 * 4;

		protected TauntEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		@Override
		public void tick() {
			target.interruptHard();
			target.orderAttackUnit(caster, true);
			target.setAttacking(caster);
			target.stats.applyTaunt(tauntDuration, caster);
			markAsComplete();
		}

	}

	public static class DeafeningCryEffect extends CasterEffect {

		public DeafeningCryEffect(Unit caster) {
			super(caster);
		}

		static float aoeRadius = 80f;
		static float reelDuration = 60 * 4;

		@Override
		public void tick() {
			for (Unit unit : mapInstance.getUnits()) {
				if (unit == caster)
					continue;
				if (Point.calcDistance(caster.getPos(), unit.getPos()) <= aoeRadius + unit.getRadius()) {
					if (unit.getSide() != caster.getSide()) {
						unit.stats.doReelRollAgainst(15, reelDuration);
					} else {
						unit.stats.doReelRollAgainst(5, reelDuration);
					}
				}
			}
			markAsComplete();
			mapInstance.addEffect(new Effects.CircleOutlineVfx(caster.x, caster.y, aoeRadius, Color.ORANGE, mapInstance));
		}

	}
}
