package com.mygdx.holowyth.gamedata.skillsandeffects;

import com.mygdx.holowyth.game.ui.GameLogDisplay;
import com.mygdx.holowyth.gamedata.skillsandeffects.MageEffects.MagicMissileEffect;
import com.mygdx.holowyth.gamedata.skillsandeffects.RangerEffects.CrossSlashEffect;
import com.mygdx.holowyth.skill.ActiveSkill.Tag;
import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.skill.skill.UnitSkill;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.item.Equip.WeaponType;
import com.mygdx.holowyth.util.DataUtil;

public class RangerSkills {
	public static class CrossSlash extends NoneSkill {
		public CrossSlash() {
			super();
			name = "Cross Slash";
			casting.castTime = 60 * 0.5f;
			casting.isInterruptedByDamageOrReel = false;
			spCost = 14;
			cooldown = 60 * 10;
		}

		@Override
		public boolean pluginTargeting(Unit caster) {
			if (!caster.isAttacking())
				return false;
			setEffects(new RangerEffects.CrossSlashEffect(caster, caster.getAttacking()));
			return true;
		}

		@Override
		public String getDescription() {
			return String.format(
					"Perform two cross shaped slashes, dealing %sx2x2 damage. If the opponent blocks the attack, reel the opponent for up to 3 seconds.",
					DataUtil.roundFully(CrossSlashEffect.strikeDamage));
		}
	}

	public static class Archery extends UnitSkill {

		public static float atkDamageMultiplier = 1.25f;
		public static int atkBonus = 10;
		public static float slowAmount = 0.4f;
		public static float slowDuration = 3 * 60;

		public Archery() {
			super();
			name = "Archery";
			casting.castTime = 0.6f * 60;
			spCost = 8;
			cooldown = 60 * 10;
			requiresLOS = true;
			setMaxRange(600);
			addTag(Tag.RANGED);
		}

		@Override
		protected boolean pluginTargeting(Unit caster, Unit target) {
			setEffects(new RangerEffects.ArcheryEffect(caster, target));
			if (!caster.equip.hasWeaponTypeEquipped(WeaponType.BOW)) {
				logErrorMessage("Unit must have a bow equipped");
				return false;
			}
			return true;
		}

		@Override
		public String getDescription() {
			return String.format("Fires an arrow dealing %s damage and slowing the target by %s for %s seconds",
					DataUtil.percentage(atkDamageMultiplier), DataUtil.percentage(slowAmount),
					DataUtil.asSeconds(slowDuration));
		}

	}
}
