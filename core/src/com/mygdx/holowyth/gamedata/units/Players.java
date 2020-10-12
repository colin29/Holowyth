package com.mygdx.holowyth.gamedata.units;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.mygdx.holowyth.gamedata.items.Weapons;
import com.mygdx.holowyth.gamedata.skillsandeffects.PassiveSkills;
import com.mygdx.holowyth.gamedata.skillsandeffects.Skills;
import com.mygdx.holowyth.gamedata.skillsandeffects.SwordsmanSkills;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.world.map.UnitMarker;
import com.mygdx.holowyth.world.map.UnitMarker.TemplateUnitMarker;

@SuppressWarnings("null")
@NonNullByDefault
public class Players {
	
	public static final String HEAD_SPRITE_DIR  = "img/sprites/head/";
	public static final UnitMarker lecia = new TemplateUnitMarker() {
		{
		side = Side.PLAYER;
		name = "Lecia";
		baseStats.set(MonsterStats.baseHuman);
		wornEquips.equip(Weapons.shortSword);  // weapon will be copied anyways, no need to clone
		activeSkills.addAll(Skills.swordsmanSkills);
		passiveSkills.add(PassiveSkills.basicCombatTraining);
		passiveSkills.add(SwordsmanSkills.swiftness);
		
		animatedSpriteName = "pipo-charachip030e.png";
		headSpriteName = HEAD_SPRITE_DIR + "Lecia.png";
		}
	};
	public static final UnitMarker elvin = new TemplateUnitMarker() {
		{
		side = Side.PLAYER;
		name = "Elvin";
		baseStats.set(MonsterStats.baseHuman);
		activeSkills.addAll(Skills.warriorSkills);
		passiveSkills.add(PassiveSkills.basicCombatTraining);
		
		animatedSpriteName = "pipo-charachip001b.png";
		headSpriteName = HEAD_SPRITE_DIR + "Elvin.png";
		}
	};
	public static final UnitMarker sonia = new TemplateUnitMarker() {
		{
		side = Side.PLAYER;
		name = "Sonia";
		baseStats.set(MonsterStats.baseHuman);
		wornEquips.equip(Weapons.staff);  
		activeSkills.addAll(Skills.mageSkills);
		
		animatedSpriteName = "pipo-charachip017c.png";
		headSpriteName = HEAD_SPRITE_DIR + "Sonia.png";
		}
	};
	public static final UnitMarker renee = new TemplateUnitMarker() {
		{
		side = Side.PLAYER;
		name = "Renee";
		baseStats.set(MonsterStats.baseHuman);
		wornEquips.equip(Weapons.staff);  
		activeSkills.addAll(Skills.priestSkills);
		
		animatedSpriteName = "renee.png";
		headSpriteName = HEAD_SPRITE_DIR + "Renee.png";
		}
	};

}
