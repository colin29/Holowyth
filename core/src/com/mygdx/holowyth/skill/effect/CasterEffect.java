package com.mygdx.holowyth.skill.effect;

import com.mygdx.holowyth.unit.Unit;

public abstract class CasterEffect extends Effect {

	/**
	 * Could potentially be null to mark no source
	 */
	protected final Unit caster;

	public CasterEffect(Unit caster) {
		super(caster.getMapInstanceMutable());
		this.caster = caster;
	}

}
