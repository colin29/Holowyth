package com.mygdx.holowyth.skill;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.mygdx.holowyth.combatDemo.World;
import com.mygdx.holowyth.skill.effect.CasterEffect;
import com.mygdx.holowyth.skill.effect.Effect;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.exceptions.HoloAssertException;
import com.mygdx.holowyth.util.exceptions.HoloException;

/**
 * Represents a skill instance that is cast and then produces effects. A skill manages its effects through it's lifetime.
 * 
 * A skill instance also functions as a identifier (ie. that for marking that key is bound to that particular skill) (When the skill is actually to be
 * used, a clone should be used)
 * 
 * Usage: creation --> set effect --> begin() --> effects start running
 * 
 * 
 * @author Colin Ta
 *
 */
public abstract class Skill implements Cloneable, SkillInfo {

	public float cooldown; // in game frames

	/**
	 * The slotted skill that this skill was copied from <br>
	 * If this is a slotted skill, is null;
	 */
	private Skill parent;
	/**
	 * Is used for skills held in unit's skill slots, represents the current cooldown of that skill
	 */
	public float curCooldown;

	public int spCost;

	public String name = "Skill name";

	// Components
	Unit caster;

	// References
	World world;

	// Components
	public Casting casting = new Casting(this); // default behaviour, can assign over this from a sub class.

	public boolean hasChannelingBehaviour = false;

	public enum Tag {
		MAGIC, RANGED_MAGIC, RANGED
	}

	private Set<Tag> tags = new LinkedHashSet<Tag>();

	/**
	 * Channeled *effects* is not implemented yet. Those effects need to be specially marked 'channeling', and when the skill channel is interrupted
	 * they should be interrupted/removed.
	 *
	 */
	public enum Status {
		INIT, CASTING, CHANNELING, DONE
	}

	private Status status = Status.INIT;

	/**
	 * Represents the type of input the spell takes. (e.g. explosion is cast on the ground, whereas backstab targets a unit)
	 * 
	 * @author Colin Ta
	 *
	 */
	public enum Targeting {
		GROUND, UNIT, UNIT_GROUND, NONE
	}

	private final Targeting targeting; // determines what types of effects can be entered

	private List<CasterEffect> effects = new ArrayList<CasterEffect>();
	private boolean effectsSet = false;

	// if this is set, then all the effects are instantiated and the skill is fully defined.

	public Skill(Targeting targeting) {
		this.targeting = targeting;
	}

	public Skill(Targeting targeting, List<CasterEffect> effects) {
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
		tick();
		// If skill was not insta-cast, we need to stop motion and actions
		if (status == Status.CASTING || status == Status.CHANNELING) {
			caster.stopUnit();
		}
	}

	/**
	 * Tick happens every frame, and continues as long as the unit remains casting or channeling the skill
	 */
	public void tick() {

		if (isSlottedSkill()) {
			throw new HoloAssertException("Should not be ticking a slotted skill.");
		}

		if (status == Status.CASTING) {
			casting.tick();
			if (casting.isComplete()) {

				// Check that sp is sufficient, if not, abort.

				if (hasEnoughSp()) {
					if (!Holo.debugNoManaCost)
						caster.stats.subtractSp(spCost);
				} else {
					status = Status.DONE;
					return;
				}

				if (parent != null) {
					parent.curCooldown = cooldown;
				} else {
					throw new HoloException("Skill used but parent field false");
				}

				if (hasChannelingBehaviour) {
					status = Status.CHANNELING;
				} else {
					status = Status.DONE;
				}

				for (Effect effect : effects) {
					effect.begin();
					world.addEffect(effect);
				}
				onFinishCasting();
			}
		}

		if (status == Status.DONE) {
			caster.setActiveSkill(null);
		}

	}

	/**
	 * If the interrupt goes through, will clear the current skill of unit
	 * 
	 * @param isHardInterrupt
	 */
	public void interrupt(boolean isHardInterrupt) {

		if (status == Status.CASTING) {
			if (isHardInterrupt || casting.isInterruptedByDamageOrReel) {
				System.out.println("Interrupted: " + this.name);
				status = Status.DONE;
				caster.setActiveSkill(null);
				casting.onInterrupt();
			}
		} else if (status == Status.CHANNELING) {
			caster.setActiveSkill(null);
			onChannellingInterrupt();
		}
	}

	public void tickCooldown() {
		curCooldown = Math.max(0, curCooldown - 1);
	}

	/**
	 * Override this if special behaviour is required (like making a particular effect fizzle out 1 second after interrupt) By default all channeling
	 * effects are removed immediately when channeling is interrupted.
	 */
	public void onChannellingInterrupt() {
	}

	private boolean hasEnoughSp() {
		return hasEnoughSp(caster);
	}

	public boolean hasEnoughSp(Unit unit) {
		return unit.stats.getSp() >= spCost;
	}

	private void onFinishCasting() {
	}

	/**
	 * Sets effects as a single effect.
	 * 
	 */
	public void setEffects(CasterEffect effect) {
		effects.clear();
		effects.add(effect);
		effectsSet = true;
	}

	public void setEffects(List<CasterEffect> src) {
		effects.clear();
		effects.addAll(src);
		effectsSet = true;
	}

	@Override
	public boolean areEffectsSet() {
		return effectsSet;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public Targeting getTargeting() {
		return targeting;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Skill newInstance = (Skill) super.clone();
		newInstance.effects = new ArrayList<CasterEffect>();
		newInstance.tags = new LinkedHashSet<Tag>(tags);

		newInstance.casting = (Casting) this.casting.clone();
		newInstance.casting.parent = newInstance;
		return newInstance;
	}

	@Override
	public CastingInfo getCasting() {
		return casting;
	}

	public Skill getParent() {
		return parent;
	}

	public void setParent(Skill parent) {
		this.parent = parent;
	}

	public String getDescription() {
		return "no description";
	}

	public boolean isSlottedSkill() {
		return parent == null;
	}

	public World getWorld() {
		return world;
	}

	public boolean isRangedPhysicalOrMagicSkill() {
		return tags.contains(Tag.RANGED) || tags.contains(Tag.RANGED_MAGIC);
	}

	protected void addTag(Tag tag) {
		tags.add(tag);
	}

}
