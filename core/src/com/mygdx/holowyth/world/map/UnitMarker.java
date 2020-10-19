package com.mygdx.holowyth.world.map;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.UnitStatValues;
import com.mygdx.holowyth.unit.WornEquips;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * Is a POJO object that contain the information enough to spawn an appropriate unit.
 * 
 * The (future) map editor deals in these.
 * 
 * @author Colin
 *
 */
@NonNullByDefault
public class UnitMarker {
	
	public boolean isTemplate = false;
	public String name = "Unnamed Unit";
	public final Point pos = new Point();
	public Unit.Side side;
	public Unit.JobClass jobClass = Unit.JobClass.NONE; 
	public final  UnitStatValues baseStats = new UnitStatValues();
	public final WornEquips wornEquips;
	public final List<ActiveSkill> activeSkills = new ArrayList<ActiveSkill>();
	public final List<Skill> passiveSkills = new ArrayList<Skill>();

	public @Nullable String animatedSpriteName;
	public @Nullable String headSpriteName;

	public UnitMarker() {
		 wornEquips = new WornEquips();
		 side = Side.ENEMY;  // default to avoid null
	}
	public UnitMarker(UnitMarker src) {
		name = src.name;
		pos.set(src.pos.x,  src.pos.y);
		side = src.side;
		baseStats.set(src.baseStats);
		
		wornEquips = src.wornEquips.cloneObject();
		
		// Skills that are held in Unit Markers are template instances, so its fine if they are shared
		activeSkills.addAll(src.activeSkills);
		passiveSkills.addAll(src.passiveSkills);
		
		animatedSpriteName = src.animatedSpriteName;
	}
	
	/**
	 * Identical to UnitMarker except it sets hint field isTemplate to true on construction
	 * @author Colin
	 *
	 */
	public static class TemplateUnitMarker extends UnitMarker{
		public TemplateUnitMarker() {
			super();
			isTemplate = true;
		}
	}
	
	
}
