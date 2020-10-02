package com.mygdx.holowyth.game.rendering.aiminggraphic;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.game.MapInstanceInfo;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.ShapeDrawerPlus;

public abstract class AimingGraphic {

	final protected MapInstanceInfo mapInstance;

	public AimingGraphic(MapInstanceInfo mapInstance) {
		this.mapInstance = mapInstance;
	}

	/**
	 * 
	 * @param cursorPos
	 *            in world coordinates
	 */
	public abstract void render(Vector2 cursorPos, UnitInfo caster, MapInstanceInfo world, SpriteBatch batch, ShapeDrawerPlus shapeDrawer,
			AssetManager assets);
}
