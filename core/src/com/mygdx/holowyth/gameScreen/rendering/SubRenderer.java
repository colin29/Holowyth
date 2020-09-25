package com.mygdx.holowyth.gameScreen.rendering;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.gameScreen.WorldInfo;

/**
 * Contains no mutable state
 */
public abstract class SubRenderer {
	protected final Holowyth game;

	protected final Camera worldCamera;
	protected final SpriteBatch batch;
	protected final ShapeRenderer shapeRenderer;
	protected final GameScreenRenderer renderer;

	public SubRenderer(GameScreenRenderer renderer) {
		this.renderer = renderer;

		game = renderer.game;

		worldCamera = renderer.worldCamera;
		batch = renderer.batch;
		shapeRenderer = renderer.shapeRenderer;

	}

	protected WorldInfo getWorld() {
		return renderer.getWorld();
	}
}
