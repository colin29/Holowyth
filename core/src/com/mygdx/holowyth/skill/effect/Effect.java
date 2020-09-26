package com.mygdx.holowyth.skill.effect;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.holowyth.gameScreen.MapInstance;
import com.mygdx.holowyth.util.ShapeDrawerPlus;

public abstract class Effect {

	/**
	 * The default marker for complete status. A subclass can override isComplete and define their own definition
	 */
	private boolean markedAsComplete = false;
	private String name = "Untitled Effect";

	protected final MapInstance mapInstance;

	public Effect(MapInstance world) {
		this.mapInstance = world;
	}

	public String getName() {
		return name;
	}

	/**
	 * Use for initialization. For order consistency, altering the world should always happen in tick()
	 * 
	 * This seperate method is needed because Effect is initialized when targeting is plugged in: when skill is used. <br>
	 * But begin() is called when the effect is about to be added to world: after any casting is done.
	 */
	public void begin() {
	}

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
