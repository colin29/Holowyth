package com.mygdx.holowyth.graphics.effects.animated;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.holowyth.game.MapInstance;
import com.mygdx.holowyth.game.MapInstanceInfo;
import com.mygdx.holowyth.unit.sprite.Animations;
import com.mygdx.holowyth.util.ShapeDrawerPlus;

/**
 * This class is basically identical to #Effect in structure.
 */
@NonNullByDefault
public abstract class GraphicEffect {

	/**
	 * The default marker for complete status. A subclass can override isComplete and define their own
	 * definition
	 */
	private boolean markedAsComplete = false;
	private String name = "Untitled Effect";

	protected final MapInstanceInfo mapInstance;
	protected final Animations animations;

	public GraphicEffect(MapInstanceInfo world, Animations animations) {
		this.mapInstance = world;
		this.animations = animations;
	}

	public String getName() {
		return name;
	}

	public void begin() {
	}

	public abstract void tick();

	/**
	 * An effect that has isComplete() true after tick() can rely on being removed without another
	 * tick() being called.
	 */
	public boolean isComplete() {
		return markedAsComplete;
	}

	protected void markAsComplete() {
		markedAsComplete = true;
	}

	public abstract void render(SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets);

}