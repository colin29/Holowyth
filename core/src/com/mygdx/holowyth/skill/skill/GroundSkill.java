package com.mygdx.holowyth.skill.skill;

import com.mygdx.holowyth.game.rendering.aiminggraphic.AimingGraphic;
import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.unit.Unit;

public abstract class GroundSkill extends ActiveSkill {

	public float defaultAimingHelperRadius; // if not using can ignore

	public AimingGraphic aimingGraphic;

	protected GroundSkill() {
		super(Targeting.GROUND);
	}

	public abstract boolean pluginTargeting(Unit caster, float x, float y);

}
