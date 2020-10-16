package com.mygdx.holowyth.gamedata.skillsandeffects;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.holowyth.gamedata.skillsandeffects.DarkKnightSkills.BladeInTheDark;
import com.mygdx.holowyth.gamedata.skillsandeffects.DarkKnightSkills.ThrowKnife;
import com.mygdx.holowyth.gamedata.skillsandeffects.PriestSkills.Heal;
import com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.ProjectileBase;
import com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.test.HomingProjectileMotion;
import com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.test.Projectile;
import com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.test.ProjectileMotion;
import com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.test.ProjectileWithAtk;
import com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.test.StandardProjectileCollision;
import com.mygdx.holowyth.graphics.effects.animated.AnimEffectOnUnit;
import com.mygdx.holowyth.skill.effect.CasterEffect;
import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.skill.effect.CasterUnitGroundEffect;
import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.UnitStatValues;
import com.mygdx.holowyth.util.DataUtil;
import com.mygdx.holowyth.util.ShapeDrawerPlus;
import com.mygdx.holowyth.util.dataobjects.Point;

public class DarkKnightEffects {

	public static class BladeInTheDarkEffect extends CasterUnitGroundEffect {

		protected BladeInTheDarkEffect(Unit caster, Unit target, float x, float y) {
			super(caster, target, x, y);
		}

		@Override
		public void tick() {
			if (caster.stats.isAttackRollSuccessful(target.stats, BladeInTheDark.atkBonus)) {
				target.stats.applyDamage(caster.stats.getDamage() * BladeInTheDark.atkdmgMultiplier);
			} else {
				gfx.makeMissEffect(caster);
			}
			caster.getCombat().retreatAvoidAOPfromTarget(x, y);
			caster.status.applySpeedIncrease(BladeInTheDark.movementSpeedBuff,
					BladeInTheDark.movementSpeedBuffDuration);
			markAsComplete();
		}
	}

	static class ThrowKnifeEffect extends CasterUnitEffect {

		public ThrowKnifeEffect(Unit caster, Unit target) {
			super(caster, target);
			
		}

		List<Projectile> missiles = new ArrayList<Projectile>();
		static float missileVfxRadius = 3;
		
		@Override
		public void begin() {
			UnitStatValues stats = caster.stats.getFinalStats().subtract(caster.stats.getEquipBonusesHands());
			final float damage = stats.rangedDamage * ThrowKnife.atkDamageMultiplier + ThrowKnife.knifeDamage;
			final int atk = stats.atk + ThrowKnife.atkBonus;
			Projectile knife = makeKnifeProjectile(atk, damage);
			missiles.add(knife);
		}
		
		private Projectile makeKnifeProjectile(int atk, float damage) {
			var proj = new Projectile(caster.x, caster.y, caster);
			proj.setMotion(new HomingProjectileMotion(6f, Unit.getAngleInDegrees(caster, target), target, proj));
			proj.setCollision(new StandardProjectileCollision(proj) {
				@Override
				protected void onCollision(@NonNull Unit enemy) {
					if (caster.stats.isAttackRollSuccessfulCustomAtkValue(atk, enemy.stats, true)) {
						float damageDealt = enemy.stats.applyDamage(damage);
						enemy.status.applyBleed(damageDealt / ThrowKnife.bleedTotalTicks, ThrowKnife.bleedTotalTicks,
								ThrowKnife.bleedTotalTicks * ThrowKnife.bleedTickInterval);
					} else {
						gfx.makeMissEffect(caster);
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
			missiles.removeIf(m -> m.isDone());
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
	}
	

}
