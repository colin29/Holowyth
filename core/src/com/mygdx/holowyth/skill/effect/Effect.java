package com.mygdx.holowyth.skill.effect;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.holowyth.combatDemo.World;
import com.mygdx.holowyth.util.ShapeDrawerPlus;

public abstract class Effect {

	/**
	 * The default marker for complete status. A subclass can override isComplete and define their own definition
	 */
	private boolean markedAsComplete = false;
	private String name = "Untitled Effect";

	protected final World world;

	Effect(World world) {
		this.world = world;
	}

	public String getName() {
		return name;
	}

	public abstract void begin();

	public abstract void tick();

	/**
	 * An effect that has isComplete() true after tick() can rely on being removed without another tick() being called.
	 */
	public boolean isComplete() {
		return markedAsComplete;
	}

	protected void markAsComplete() {
		markedAsComplete = true;
	}

	public void render(SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {

	}

}
