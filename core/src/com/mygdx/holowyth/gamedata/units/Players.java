package com.mygdx.holowyth.gamedata.units;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.mygdx.holowyth.gamedata.items.Weapons;
import com.mygdx.holowyth.gamedata.skillsandeffects.PassiveSkills;
import com.mygdx.holowyth.gamedata.skillsandeffects.Skills;
import com.mygdx.holowyth.gamedata.skillsandeffects.SwordsmanSkills;
import com.mygdx.holowyth.unit.Unit.JobClass;
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
		jobClass = JobClass.SWORDSMAN;
		baseStats.set(MonsterStats.baseHuman);
		wornEquips.equip(Weapons.shortSword);  // weapon will be copied anyways, no need to clone
		activeSkills.addAll(Skills.swordsmanSkills);
		passiveSkills.add(PassiveSkills.basicCombatTraining);
		passiveSkills.add(SwordsmanSkills.swiftness);
		
		animatedSpriteName = "lecia.png";
		headSpriteName = HEAD_SPRITE_DIR + "Lecia.png";
		}
	};
	public static final UnitMarker elvin = new TemplateUnitMarker() {
		{
		side = Side.PLAYER;
		name = "Elvin";
		jobClass = JobClass.WARRIOR;
		wornEquips.equip(Weapons.shortSword); 
		baseStats.set(MonsterStats.baseHuman);
		activeSkills.addAll(Skills.warriorSkills);
		passiveSkills.add(PassiveSkills.basicCombatTraining);
		
		animatedSpriteName = "elvin.png";
		headSpriteName = HEAD_SPRITE_DIR + "Elvin.png";
		}
	};
	public static final UnitMarker sonia = new TemplateUnitMarker() {
		{
		side = Side.PLAYER;
		jobClass = JobClass.MAGE;
		name = "Sonia";
		baseStats.set(MonsterStats.baseHuman);
		wornEquips.equip(Weapons.staff);  
		activeSkills.addAll(Skills.mageSkills);
		
		animatedSpriteName = "sonia.png";
		headSpriteName = HEAD_SPRITE_DIR + "Sonia.png";
		}
	};
	public static final UnitMarker renee = new TemplateUnitMarker() {
		{
		side = Side.PLAYER;
		name = "Renee";
		jobClass = JobClass.PRIEST;
		baseStats.set(MonsterStats.baseHuman);
		wornEquips.equip(Weapons.staff);  
		activeSkills.addAll(Skills.priestSkills);
		
		animatedSpriteName = "renee.png";
		headSpriteName = HEAD_SPRITE_DIR + "Renee.png";
		}
	};
	public static final UnitMarker seth = new TemplateUnitMarker() {
		{
		side = Side.PLAYER;
		jobClass = JobClass.THIEF;
		name = "Seth";
		baseStats.set(MonsterStats.baseHuman);
		wornEquips.equip(Weapons.dagger);  
		activeSkills.addAll(Skills.darkKnightSkills);
		
		animatedSpriteName = "seth.png";
		headSpriteName = HEAD_SPRITE_DIR + "Seth.png";
		}
	};
	public static final UnitMarker mikal = new TemplateUnitMarker() {
		{
		side = Side.PLAYER;
		name = "Mikal";
		jobClass = JobClass.RANGER;
		baseStats.set(MonsterStats.baseHuman);
		activeSkills.addAll(Skills.rangerSkills);
		
		animatedSpriteName = "mikal.png";
		headSpriteName = HEAD_SPRITE_DIR + "Mikal.png";
		
		wornEquips.equip(Weapons.bow);
		}
	};

}
