package com.mygdx.holowyth.gamedata.skillsandeffects;

import com.mygdx.holowyth.game.rendering.aiminggraphic.SkillsAimingGraphics;
import com.mygdx.holowyth.gamedata.skillsandeffects.MageEffects.MagicMissileEffect;
import com.mygdx.holowyth.graphics.effects.animated.EffectCenteredOnFixedPos;
import com.mygdx.holowyth.graphics.effects.animated.EffectCenteredOnUnit;
import com.mygdx.holowyth.skill.Casting;
import com.mygdx.holowyth.skill.skill.GroundSkill;
import com.mygdx.holowyth.skill.skill.UnitSkill;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.DataUtil;

public class MageSkills {
	public static class MagicMissile extends UnitSkill {
		public MagicMissile() {
			super();
			name = "Magic Missile";
			casting.castTime = 60 * 0.8f;
			spCost = 12;
			cooldown = 60 * 12;
			addTag(Tag.RANGED_MAGIC);
		}

		@Override
		protected void pluginTargeting(Unit caster, Unit target) {
			setEffects(new MageEffects.MagicMissileEffect(caster, target));
		}

		@Override
		public String getDescription() {
			return String.format("Fires 3 missiles that do %s damage and knock the target back. A basic mage skill.", MagicMissileEffect.damage);
		}

	}

	public static class ArcaneBolt extends UnitSkill {
		public ArcaneBolt() {
			super();
			name = "Arcane Bolt";
			casting.castTime = 60 * 1.3f;
			spCost = 22;
			cooldown = 60 * 18;
			addTag(Tag.RANGED_MAGIC);

		}

		@Override
		protected void pluginTargeting(Unit caster, Unit target) {
			setEffects(new MageEffects.ArcaneBoltEffect(caster, target));
		}

		@Override
		public String getDescription() {
			return "Fires a powerful slow-moving missile that is capable of stunning even the hardiest of foes. A more advanced version of Magic Missile.";
		}
	}

	public static class WindBlades extends UnitSkill {
		public WindBlades() {
			super();
			name = "Wind Blades";
			casting.castTime = 60 * 0.5f;
			spCost = 10;
			cooldown = 60 * 8;
			addTag(Tag.RANGED_MAGIC);
		}

		@Override
		protected void pluginTargeting(Unit caster, Unit target) {
			setEffects(new MageEffects.WindBladesEffect(caster, target));
		}

		@Override
		public String getDescription() {
			return "Unleashes a swift barrage of wind scythes. Uses little mana, but is ineffective against armor.";
		}
	}

	public static class Fireball extends UnitSkill {
		public Fireball() {
			super();
			name = "Fireball";
			casting.castTime = 60 * 1.3f;
			spCost = 16;
			cooldown = 60 * 16;
			addTag(Tag.RANGED_MAGIC);
		}

		@Override
		protected void pluginTargeting(Unit caster, Unit target) {
			setEffects(new MageEffects.FireBallEffect(caster, target));
		}

		@Override
		public String getDescription() {
			return String.format(
					"Summons a fireball that explodes on impact, dealing %s damage to nearby enemies (half damage to allies).",
					DataUtil.roundFully(MageEffects.FireBallEffect.damage));
		}
	}

	public static class Hydroblast extends GroundSkill {
		public Hydroblast() {
			super();
			name = "Hydroblast";
			casting.castTime = 60 * 1.2f;
			spCost = 14;
			cooldown = 60 * 16;
			addTag(Tag.RANGED_MAGIC);

			aimingGraphic = new SkillsAimingGraphics.HydroblastAimingGraphic(MageEffects.HydroblastEffect.coneLength,
					MageEffects.HydroblastEffect.coneInnerLength,
					MageEffects.HydroblastEffect.coneWidthDegrees,
					getMapInstance());
		}

		@Override
		public void pluginTargeting(Unit caster, float x, float y) {
			setEffects(new MageEffects.HydroblastEffect(caster, x, y));
		}

		@Override
		public String getDescription() {
			return String.format(
					"Floods a cone-shaped area with water, slowing units by %s for %s seconds. Units caught in the inner area are knocked back slightly.",
					DataUtil.percentage(MageEffects.HydroblastEffect.slowAmount), DataUtil.asSeconds(MageEffects.HydroblastEffect.slowDuration));
		}
	}

	public static class Thunderclap extends GroundSkill {
		public Thunderclap() {
			super();
			name = "Thunderclap";
			casting.castTime = 60 * 1.6f;
			spCost = 16;
			cooldown = 60 * 18;
			addTag(Tag.RANGED_MAGIC);

			defaultAimingHelperRadius = MageEffects.ThunderclapEffect.aoeRadius;

		}

		@Override
		public void pluginTargeting(Unit caster, float x, float y) {
			setEffects(new MageEffects.ThunderclapEffect(caster, x, y));
		}

		@Override
		public String getDescription() {
			return String.format(
					"Summons a thunderclap, stunning all nearby units for %s seconds (+10 to save for allies).",
					DataUtil.asSeconds(MageEffects.ThunderclapEffect.stunDuration));
		}
	}

	public static class BlindingFlash extends GroundSkill {
		public BlindingFlash() {
			super();
			name = "Blinding Flash";
			spCost = 18;
			cooldown = 60 * 24;
			globalCooldown = 60 * 3;
			addTag(Tag.RANGED_MAGIC);
			defaultAimingHelperRadius = MageEffects.BlindingFlashEffect.aoeRadius;
		}

		@Override
		public void pluginTargeting(Unit caster, float x, float y) {
			setEffects(new MageEffects.BlindingFlashEffect(caster, x, y));
			
			casting = new Casting(this) {
				{
				castTime = 60 * 1.8f;
				}
				@Override
				protected void onBeginCast() {
					var effect = new EffectCenteredOnUnit(caster, "casting_glow.png", mapInstance, mapInstance.getAnimations());	
					effect.setSize(72, 72);
					effect.setAlpha(0.85f);
					mapInstance.getGfx().addGraphicEffect(effect);
				}
				@Override
				protected void onFinishCast() {
					var flash = new EffectCenteredOnFixedPos(x, y, "lightning_ball.png", mapInstance, mapInstance.getAnimations());	
					mapInstance.getGfx().addGraphicEffect(flash);
				}
				
			};
		}

		@Override
		public String getDescription() {
			return String.format(
					"Saturates the area in light, blinding all units for %s seconds (halved for allies).",
					DataUtil.asSeconds(MageEffects.BlindingFlashEffect.blindDuration));
		}
	}

}
