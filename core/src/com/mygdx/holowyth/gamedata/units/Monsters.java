package com.mygdx.holowyth.gamedata.units;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.mygdx.holowyth.gamedata.items.Weapons;
import com.mygdx.holowyth.skill.skill.Skills;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.world.map.UnitMarker;
import com.mygdx.holowyth.world.map.UnitMarker.TemplateUnitMarker;

@NonNullByDefault
@SuppressWarnings("unused")
public class Monsters {
	public static final UnitMarker goblin = new TemplateUnitMarker() {
		{
		side = Side.ENEMY;
		name = "Goblin";
		baseStats.set(MonsterStats.goblinScavenger);
		wornEquips.equip(Weapons.club);  // weapon will be copied anyways, no need to clone
		
		animatedSpriteName = "goblin1.png";
		}
	};
}
