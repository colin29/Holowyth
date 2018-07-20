package com.mygdx.holowyth.skill.effect;

import com.mygdx.holowyth.combatDemo.World;

public abstract class Effect {
	
	private boolean completed = false;
	private String name = "Untitled Effect";

	World world;
	
	Effect(World world){
		this.world = world;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract void begin();
	public abstract void tick();
	public abstract boolean isComplete();

}
