package com.mygdx.holowyth.gamedata.skillsandeffects;

import java.util.LinkedHashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.gamedata.skillsandeffects.SwordsmanSkills.Pinwheel;
import com.mygdx.holowyth.gamedata.skillsandeffects.SwordsmanSkills.TripleStrike;
import com.mygdx.holowyth.graphics.effects.animated.AnimEffectOnUnit;
import com.mygdx.holowyth.graphics.effects.animated.AnimatedEffect;
import com.mygdx.holowyth.skill.effect.CasterEffect;
import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.dataobjects.Point;

public class SwordsmanEffects {
	public static class TripleStrikeEffect extends CasterUnitEffect {

		private int framesElapsed = 0;
		private int strikesDone = 0;

		protected TripleStrikeEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		boolean hit;

		public void begin() {
			hit = caster.stats.isAttackRollSuccessful(target.stats, 5);
			if (!hit) {
				gfx.makeMissEffect(caster);
			}
		}

		@Override
		public void tick() {
			if (framesElapsed % 15 == 0) {
				if (hit) {
					target.stats.applyDamage(caster.stats.getDamage() * TripleStrike.atkdmgMultiplier);
				}
				strikesDone++;
			}
			if (strikesDone == 3) {
				markAsComplete();
			}
			framesElapsed++;
		}
	}

	public static class PinwheelEffect extends CasterEffect {

		private int framesElapsed = 0;
		private int strikesDone = 0;

		private Map<Unit, Boolean> atkResult = new LinkedHashMap<>();

		public PinwheelEffect(Unit caster) {
			super(caster);
		}

		AnimEffectOnUnit effect;

		public void begin() {
			effect = new AnimEffectOnUnit(caster, "pinwheel.png", mapInstance, mapInstance.getAnimations());
			effect.loop = true;
			effect.setAlpha(1f);
			effect.setSize(150, 150);
			gfx.addGraphicEffect(effect);
		}

		@Override
		public void tick() {
			if (framesElapsed % Pinwheel.strikeInterval == 0 && framesElapsed != 0) {
				for (Unit unit : mapInstance.getUnits()) {
					if (unit.isEnemy(caster)) {
						if (Unit.getDist(caster, unit) <= Pinwheel.aoeRadius + caster.getRadius()) {
							if (!atkResult.containsKey(unit)) {
								atkResult.put(unit, caster.stats.isAttackRollSuccessful(unit.stats, Pinwheel.atkBonus));
								if (!atkResult.get(unit)) {
									gfx.makeMissEffect(unit); // should be a blocked txt
								}
							}
							boolean hit = atkResult.get(unit);
							if (hit) {
								unit.stats.applyDamage(caster.stats.getDamage() * Pinwheel.atkdmgMultiplier);
							}
						}
					}
				}
				strikesDone++;
				if (strikesDone == 5) {
					effect.markAsComplete();
					markAsComplete();
				}
			}
			framesElapsed++;
		}

	}
}
