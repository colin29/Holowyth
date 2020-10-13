package com.mygdx.holowyth.gamedata.skillsandeffects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.holowyth.gamedata.skillsandeffects.RangerSkills.Archery;
import com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.ArcheryArrow;
import com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.MagicMissileBolt;
import com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.ProjectileBase;
import com.mygdx.holowyth.graphics.effects.EffectsHandler.DamageEffectParams;
import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.ShapeDrawerPlus;

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
					DamageEffectParams params = new DamageEffectParams();
					params.useFastEffect = true;
					target.stats.applyDamage(strikeDamage, params);

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
	
	static class ArcheryEffect extends CasterUnitEffect {
		public ArcheryEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		List<ProjectileBase> missiles;

		static float missileVfxRadius = 3;

		@Override
		public void begin() {
			missiles = new ArrayList<ProjectileBase>();
			for (int i = 0; i < 1; i++) {
				missiles.add(new ArcheryArrow(caster.x, caster.y + 18 * i,  caster.stats.getDamage()* Archery.atkDamageMultiplier, caster, target));
			}
		}

		@Override
		public void tick() {
			for (var m : missiles) {
				m.tick();
			}
			missiles.removeIf(m -> m.isExpired() || m.isCollided());

			if (missiles.isEmpty())
				markAsComplete();
		}

		@Override
		public void render(SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
			shapeDrawer.setColor(Color.RED, 0.8f);
			batch.begin();
			for (var m : missiles) {
				shapeDrawer.filledCircle(m.getX(), m.getY(), missileVfxRadius);
			}
			batch.end();
		}

	};
}
