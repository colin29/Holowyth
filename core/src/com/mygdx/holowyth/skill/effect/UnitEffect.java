package com.mygdx.holowyth.skill.effect;

import com.mygdx.holowyth.unit.Unit;

public abstract class UnitEffect extends Effect {

	protected Unit source;

	public UnitEffect(Unit caster) {
		super(caster.getWorldMutable());
		this.source = caster;
	}

}
