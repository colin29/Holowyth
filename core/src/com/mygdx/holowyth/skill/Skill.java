package com.mygdx.holowyth.skill;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections4.CollectionUtils;

import com.mygdx.holowyth.combatDemo.World;
import com.mygdx.holowyth.skill.effect.Effect;
import com.mygdx.holowyth.skill.effect.UnitEffect;
import com.mygdx.holowyth.unit.Unit;

public class Skill {
	
	public float cooldown; // in game frames
	public int spCost;
	
	public String name = "Skill name";

	// World fields
	Unit caster;
	World world;
	
	// Components
	public Casting casting = new Casting(this); //default behaviour, can assign over this from a sub class.
	
	public boolean hasChannelingBehaviour = false;
	
	public enum Status {INIT, CASTING, CHANNELING, RESOLVING, DONE}
	private Status status = Status.INIT;
	
	/**
	 * Represents the type of input the spell takes. (e.g. explosion is cast on the ground, whereas backstab targets a unit)
	 * @author Colin Ta
	 *
	 */
	public enum Targeting {GROUND, UNIT, NONE}
	private final Targeting targeting; // determines what types of effects can be entered
	private boolean targetingComplete;
	
	private List<UnitEffect> effects;
	
	public Skill(Targeting targeting, List<UnitEffect> effects) {
		this.targeting = targeting;
		this.effects = effects;
	}
	
	public void begin(Unit caster) {
		this.caster = caster;
		this.world = caster.getWorldMutable();
		
		
		// Todo: trigger targeting
		
		status = Status.CASTING;
		casting.begin(caster);
	}
	public void tick() {
		
		if(status == Status.CASTING) {
			casting.tick();
			if(casting.isComplete()) {
				if(hasChannelingBehaviour) {
					status = Status.CHANNELING;
				}else {
					status = Status.RESOLVING;
				}
				onFinishCasting();
			}
		}

		// TODO: skills with Channeling behaviour
		
		if(status == Status.CHANNELING || status == Status.RESOLVING) {
			for(Effect effect: effects) {
				effect.tick();
			}
		}
		
		// Remove all completed effects from the list
		
		CollectionUtils.filter(effects, (item)->!item.isComplete());
		
		// If all effects are completed, skill is complete
		
		if(effects.isEmpty()) {
			status = Status.DONE;
			caster.setActiveSkill(null);
			System.out.printf("Skill %s finished. %n", this.name);
		}
		
	}
	
	private void onFinishCasting() {
		// Start all effects
		for(Effect effect: effects) {
			effect.begin();
		}
	}

	public Status getStatus() {
		return status;
	}

	
	public Targeting getTargeting() {
		return targeting;
	}
	
	

	
}
