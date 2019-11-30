package com.mygdx.holowyth.skill.effect;

import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.dataobjects.Point;

public abstract class CasterGroundEffect extends CasterEffect {

	protected final Point ground;

	protected CasterGroundEffect(Unit caster, float x, float y) {
		super(caster);
		ground = new Point(x, y);
	}

}
