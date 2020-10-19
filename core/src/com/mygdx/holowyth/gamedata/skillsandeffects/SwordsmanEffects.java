package com.mygdx.holowyth.gamedata.skillsandeffects;

import java.util.LinkedHashMap;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.gamedata.skillsandeffects.SwordsmanSkills.Pinwheel;
import com.mygdx.holowyth.gamedata.skillsandeffects.SwordsmanSkills.TripleStrike;
import com.mygdx.holowyth.graphics.effects.animated.AnimEffectOnUnit;
import com.mygdx.holowyth.skill.effect.CasterEffect;
import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.unit.Unit;

public class SwordsmanEffects {
	public static class TripleStrikeEffect extends CasterUnitEffect {

		private int framesElapsed = 0;
		private int strikesDone = 0;

		protected TripleStrikeEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		boolean hit;
		private int delay = 0;
		private int strikeInterval = 12;

		public void begin() {
			hit = caster.stats.isAttackRollSuccessful(target.stats, 10);
			
			for(int i=0;i<3;i++) {

				Vector2 offset = new Vector2(0, 4); // offset each so they form a triangle
				float rotation = i * 120;
				offset.rotate(45 + rotation);
				
				var effect = new AnimEffectOnUnit(target, "slash_white.png", mapInstance);
				effect.setSize(60, 60);
				effect.setRotation(i*120);
				effect.setDelay(delay + i*strikeInterval);
				effect.offset(offset.x, offset.y);
				gfx.addGraphicEffect(effect);	
			}
			if (!hit) {
				gfx.makeMissEffect(caster);
			}
		}

		@Override
		public void tick() {
			if ((framesElapsed - delay) % strikeInterval == 0 && (framesElapsed-delay) > 0) {
				if (hit) {
					target.stats.applyDamage(caster.stats.getDamage() * TripleStrike.atkdmgMultiplier);
				}else {
					gfx.makeBlockEffect(caster, target);
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
			effect = new AnimEffectOnUnit(caster, "pinwheel.png", mapInstance);
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
						if (Unit.dist(caster, unit) <= Pinwheel.aoeRadius + caster.getRadius()) {
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
