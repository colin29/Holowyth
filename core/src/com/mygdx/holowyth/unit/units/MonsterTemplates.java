package com.mygdx.holowyth.unit.units;

import com.mygdx.holowyth.map.UnitMarker.TemplateUnitMarker;
import com.mygdx.holowyth.skill.skill.Skills;
import com.mygdx.holowyth.unit.Unit.Side;

public class MonsterTemplates {
	public static final TemplateUnitMarker goblin = new TemplateUnitMarker() {
		{
		side = Side.ENEMY;
		name = "Goblin";
		baseStats.set(MonsterStats.goblin);
		animatedSpriteName = "goblin1.png";
		activeSkills.addAll(Skills.warriorSkills);
		}
	};
}
