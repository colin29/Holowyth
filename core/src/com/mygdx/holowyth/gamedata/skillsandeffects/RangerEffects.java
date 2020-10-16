package com.mygdx.holowyth.gamedata.skillsandeffects;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.holowyth.gamedata.skillsandeffects.DarkKnightSkills.ThrowKnife;
import com.mygdx.holowyth.gamedata.skillsandeffects.RangerSkills.Archery;
import com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.ProjectileBase;
import com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.test.HomingProjectileMotion;
import com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.test.Projectile;
import com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.test.StandardProjectileCollision;
import com.mygdx.holowyth.graphics.effects.EffectsHandler.DamageEffectParams;
import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.UnitStatValues;
import com.mygdx.holowyth.util.ShapeDrawerPlus;

public class RangerEffects {
	static class CrossSlashEffect extends CasterUnitEffect {
		public CrossSlashEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		int framesElapsed = 0;
		static int crossStrikeDelay = 30;
		static int individualStrikeDelay = 6;
		static float strikeDamage = 8;
		boolean attackRollSucceeded;

		@Override
		public void begin() {
			attackRollSucceeded = caster.stats.isAttackRollSuccessful(target.stats, 5);
		}

		@Override
		public void tick() {

			if (attackRollSucceeded) {
				if (framesElapsed == 0 ||
						framesElapsed == individualStrikeDelay ||
						framesElapsed == crossStrikeDelay ||
						framesElapsed == crossStrikeDelay + individualStrikeDelay) {
					DamageEffectParams params = new DamageEffectParams();
					params.useFastEffect = true;
					target.stats.applyDamage(strikeDamage, params);
				}
			} else {
				if (framesElapsed == individualStrikeDelay) {
					target.stats.doReelRollAgainst(15, 60 * 3);
				}
				if (framesElapsed == 0 || framesElapsed == crossStrikeDelay) {
					gfx.makeBlockEffect(caster, target);
				}

			}
			if (framesElapsed == crossStrikeDelay + individualStrikeDelay) {
				markAsComplete();
			}
			framesElapsed += 1;
		}
	}
	
	static class ArcheryEffect extends CasterUnitEffect {
		public ArcheryEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		final List<Projectile> missiles = new ArrayList<Projectile>();
		static float missileVfxRadius = 3;

		@Override
		public void begin() {
			int atk = target.stats.getRangedAtk() + Archery.atkBonus;
			float damage = caster.stats.getRangedDamage() * Archery.atkDamageMultiplier; 
			Projectile proj = makeProjectile(atk, damage);
			missiles.add(proj);
		}
	
		
		private Projectile makeProjectile(int atk, float damage) {
			var proj = new Projectile(caster.x, caster.y, caster);
			proj.setMotion(new HomingProjectileMotion(9f, Unit.getAngleInDegrees(caster, target), target, proj));
			proj.setCollision(new StandardProjectileCollision(proj) {
				@Override
				protected void onCollision(@NonNull Unit enemy) {
					if (caster.stats.isAttackRollSuccessfulCustomAtkValue(atk, enemy.stats, true)) {
						enemy.stats.applyDamage(damage);
						enemy.status.applySlow(0.5f, 3*60);
					} else {
						gfx.makeMissEffect(caster);
						enemy.status.applySlow(0.3f, 3*60);
					}

				}
			});
			return proj;
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
