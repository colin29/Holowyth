package com.mygdx.holowyth.util.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.holowyth.Holowyth;

public class BaseHoloRenderer {

	private Camera worldCamera;

	protected ShapeRenderer shapeRenderer;
	protected SpriteBatch batch;

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private Color clearColor = Color.BLACK;

	public BaseHoloRenderer(Holowyth game, Camera worldCamera) {

		batch = game.batch;
		shapeRenderer = game.shapeRenderer;

		this.worldCamera = worldCamera;
	}

	/**
	 * Does boilerplate things for rendering
	 */
	public void render(float delta) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		Gdx.gl.glEnable(GL20.GL_BLEND);

		worldCamera.update();
		batch.setProjectionMatrix(worldCamera.combined);
		shapeRenderer.setProjectionMatrix(worldCamera.combined);

	}

	public void setClearColor(Color color) {
		if (color == null) {
			logger.warn("Color given was null");
			return;
		}
		clearColor = color;
	}

}
