package com.mygdx.holowyth.gamedata.skillsandeffects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.holowyth.gamedata.skillsandeffects.DarkKnightSkills.BladeInTheDark;
import com.mygdx.holowyth.gamedata.skillsandeffects.DarkKnightSkills.ThrowKnife;
import com.mygdx.holowyth.gamedata.skillsandeffects.PriestSkills.Heal;
import com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.ProjectileBase;
import com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.ThrowKnifeProjectile;
import com.mygdx.holowyth.graphics.effects.animated.EffectCenteredOnUnit;
import com.mygdx.holowyth.skill.effect.CasterEffect;
import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.skill.effect.CasterUnitGroundEffect;
import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.UnitStatValues;
import com.mygdx.holowyth.util.DataUtil;
import com.mygdx.holowyth.util.ShapeDrawerPlus;

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

		float damage;
		
		public ThrowKnifeEffect(Unit caster, Unit target) {
			super(caster, target);
			
		}

		List<ProjectileBase> missiles = new ArrayList<ProjectileBase>();
		static float missileVfxRadius = 3;
		
		@Override
		public void begin() {
			UnitStatValues stats = caster.stats.getFinalStats().subtract(caster.stats.getEquipBonusesHands());
			damage = stats.rangedDamage * ThrowKnife.atkDamageMultiplier + ThrowKnife.knifeDamage;
			
			var knife = new ThrowKnifeProjectile(caster.x, caster.y, stats.atk + ThrowKnife.atkBonus, damage, caster, target);
			missiles.add(knife);
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
	}

}
