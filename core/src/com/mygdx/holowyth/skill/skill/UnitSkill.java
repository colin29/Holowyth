package com.mygdx.holowyth.skill.skill;

import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.unit.Unit;

public abstract class UnitSkill extends ActiveSkill {

	protected UnitSkill() {
		super(Targeting.UNIT);
	}

	public boolean setTargeting(Unit caster, Unit target) {
		if (tags.contains(Tag.ALLIED_TARGETING)) {
			if (caster.isEnemy(target)) {
				return false;
			}
		} else {
			if (!caster.isEnemy(target)) {
				logger.debug("Can't use skill {} on an ally.", name);
				return false;
			}
		}
		pluginTargeting(caster, target);
		return true;
	}

	protected abstract void pluginTargeting(Unit caster, Unit target);

}
