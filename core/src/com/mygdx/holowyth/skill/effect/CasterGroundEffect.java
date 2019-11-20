package com.mygdx.holowyth.skill.effect;

import com.mygdx.holowyth.unit.Unit;

public abstract class CasterGroundEffect extends CasterEffect {

	protected final float groundX, groundY;

	protected CasterGroundEffect(Unit caster, float x, float y) {
		super(caster);
		this.groundX = x;
		this.groundY = y;
	}

}
