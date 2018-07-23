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

/**
 * Represents a skill instance that is cast and then produces effects. A skill manages its effects through it's lifetime.
 * 
 * A skill instance also functions as a identifier (ie. that for marking that key is bound to that particular skill)
 * (When the skill is actually to be used, a clone should be used)
 * 
 * Usage: creation --> set effect --> begin() --> effects start running
 * 
 * 
 * @author Colin Ta
 *
 */
public class Skill implements Cloneable {

	public float cooldown; // in game frames
	public int spCost;

	public String name = "Skill name";

	// World fields
	Unit caster;
	World world;

	// Components
	public Casting casting = new Casting(this); // default behaviour, can assign over this from a sub class.

	public boolean hasChannelingBehaviour = false;

	public enum Status {
		INIT, CASTING, CHANNELING, RESOLVING, DONE
	}

	private Status status = Status.INIT;

	/**
	 * Represents the type of input the spell takes. (e.g. explosion is cast on the ground, whereas backstab targets a
	 * unit)
	 * 
	 * @author Colin Ta
	 *
	 */
	public enum Targeting {
		GROUND, UNIT, NONE
	}

	private final Targeting targeting; // determines what types of effects can be entered

	private List<UnitEffect> effects; // if this is set, then all the effects are instantiated and the skill is fully
										// defined.

	public Skill(Targeting targeting) {
		this.targeting = targeting;
	}

	public Skill(Targeting targeting, List<UnitEffect> effects) {
		this.targeting = targeting;
		this.effects = effects;
	}

	public void begin(Unit caster) {
		if (!areEffectsSet()) {
			System.out.println("Effects not finalized yet " + this.name);
			return;
		}
			

		this.caster = caster;
		this.world = caster.getWorldMutable();

		status = Status.CASTING;
		casting.begin(caster);
	}

	public void tick() {

		if (status == Status.CASTING) {
			casting.tick();
			if (casting.isComplete()) {
				if (hasChannelingBehaviour) {
					status = Status.CHANNELING;
				} else {
					status = Status.RESOLVING;
				}
				onFinishCasting();
			}
		}

		// TODO: skills with Channeling behaviour

		if (status == Status.CHANNELING || status == Status.RESOLVING) {
			for (Effect effect : effects) {
				effect.tick();
			}
		}

		// Remove all completed effects from the list

		CollectionUtils.filter(effects, (item) -> !item.isComplete());

		// If all effects are completed, skill is complete

		if (effects.isEmpty()) {
			status = Status.DONE;
			caster.setActiveSkill(null);
			System.out.printf("Skill %s finished. %n", this.name);
		}

	}

	private void onFinishCasting() {
		// Start all effects
		for (Effect effect : effects) {
			effect.begin();
		}
	}

	public void setEffect(List<UnitEffect> effects) {
		this.effects = effects;
	}

	public boolean areEffectsSet() {
		return effects != null;
	}

	public Status getStatus() {
		return status;
	}

	public Targeting getTargeting() {
		return targeting;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
