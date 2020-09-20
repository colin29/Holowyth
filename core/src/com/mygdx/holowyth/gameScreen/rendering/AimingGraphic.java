package com.mygdx.holowyth.gameScreen.rendering;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.gameScreen.WorldInfo;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.ShapeDrawerPlus;

public abstract class AimingGraphic {

	final protected WorldInfo world;

	public AimingGraphic(WorldInfo world) {
		this.world = world;
	}

	/**
	 * 
	 * @param cursorPos
	 *            in world coordinates
	 */
	public abstract void render(Vector2 cursorPos, UnitInfo caster, WorldInfo world, SpriteBatch batch, ShapeDrawerPlus shapeDrawer,
			AssetManager assets);
}
