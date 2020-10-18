package com.mygdx.holowyth.gamedata.skillsandeffects;

import com.mygdx.holowyth.gamedata.skillsandeffects.RangerEffects.CrossSlashEffect;
import com.mygdx.holowyth.skill.Casting.CastingType;
import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.util.DataUtil;

public class SwordsmanSkills {

	public static class TripleStrike extends NoneSkill {
		public static float atkdmgMultiplier = 0.75f;
		public TripleStrike() {
			name = "Triple Strike";
			casting.castTime = 0.4f * 60;
			casting.isInterruptedByDamageOrReel = false;
			spCost = 9;
			cooldown = 15 * 60;
			globalCooldown = 4 * 60;
			isMeleeSkill = true;
		}

		@Override
		public boolean pluginTargeting(Unit caster) {
			if (!caster.isAttacking())
				return false;
			setEffects(new SwordsmanEffects.TripleStrikeEffect(caster, caster.getAttacking()));
			return true;
		}
		@Override
		public String getDescription() {
			return String.format("Strike the enemy rapidly, dealing 3x%s damage", DataUtil.percentage(atkdmgMultiplier));
		}
	}
	
	public final static Skill swiftness = new Skill() {
		{
			name = "Swiftness";
		}

		@Override
		public void onUnitAttack(Unit parent, UnitOrderable target) {
			parent.status.applySpeedIncrease(0.25f, 4 * 60);
		}
	};
	
	public static class Pinwheel extends NoneSkill {
		
		public static int totalStrikes = 5;
		public static int strikeInterval = 20;
		public static float aoeRadius = 60f;
		
		public static float atkdmgMultiplier = 0.60f;
		public static int atkBonus = 10;
		
		public Pinwheel() {
			name = "Pinwheel";
			casting.castTime = 1f * 60;
			casting.isInterruptedByDamageOrReel = false;
			casting.castingType = CastingType.MOBILE;
			spCost = 15;
			cooldown = 20 * 60;
			globalCooldown = 4 * 60;
			channelingType = ChannelingType.MOBILE;
			
		}

		@Override
		public boolean pluginTargeting(Unit caster) {
			setEffects(new SwordsmanEffects.PinwheelEffect(caster));
			return true;
		}
		@Override
		public String getDescription() {
			return String.format("Continously strike all nearby enemies, dealing up to %sx%s damage. Can move while channeling.", totalStrikes, DataUtil.percentage(atkdmgMultiplier));
		}
	}
}
