package com.mygdx.holowyth.skill.effect;

import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.dataobjects.Point;

public abstract class CasterUnitEffect extends CasterEffect {

	protected final Unit target;

	protected CasterUnitEffect(Unit caster, Unit target) {
		super(caster);
		this.target = target;
	}

	protected final float angleFromCasterToTarget() {
		return Point.getAngleInDegrees(caster.getPos(), target.getPos());
	}

}
