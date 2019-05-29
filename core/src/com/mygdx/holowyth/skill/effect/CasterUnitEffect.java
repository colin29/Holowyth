package com.mygdx.holowyth.skill.effect;

import com.mygdx.holowyth.unit.Unit;

public abstract class CasterUnitEffect extends CasterEffect {

	protected final Unit target;

	protected CasterUnitEffect(Unit caster, Unit target) {
		super(caster);
		this.target = target;
	}

}
