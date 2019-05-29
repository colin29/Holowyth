package com.mygdx.holowyth.skill.effect;

import com.mygdx.holowyth.unit.Unit;

public abstract class CasterUnitGroundEffect extends CasterEffect {

	protected final Unit target;
	protected final float x, y;

	protected CasterUnitGroundEffect(Unit caster, Unit target, float x, float y) {
		super(caster);
		this.target = target;
		this.x = x;
		this.y = y;
	}

}
