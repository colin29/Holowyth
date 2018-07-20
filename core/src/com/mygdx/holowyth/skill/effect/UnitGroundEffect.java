package com.mygdx.holowyth.skill.effect;

import com.mygdx.holowyth.unit.Unit;

public abstract class UnitGroundEffect extends UnitEffect{

	float x, y;
	
	UnitGroundEffect(Unit caster, float x, float y) {
		super(caster);
		this.x = x;
		this.y = y;
	}

}
