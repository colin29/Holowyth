package com.mygdx.holowyth.knockback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.holowyth.Holowyth;

public class Renderer {

	private Camera worldCamera;
	private Stage stage;
	private Holowyth game;
	private ShapeRenderer shapeRenderer;
	private SpriteBatch batch;

	private KnockBackSimulation simulation;

	Logger logger = LoggerFactory.getLogger(this.getClass());
	private Color clearColor = Color.BLACK;

	public Renderer(Holowyth game, Camera worldCamera, Stage stage) {

		batch = game.batch;
		shapeRenderer = game.shapeRenderer;

		this.game = game;

		this.worldCamera = worldCamera;
		this.stage = stage;
	}

	public void render(float delta) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		Gdx.gl.glEnable(GL20.GL_BLEND);

		worldCamera.update();
		batch.setProjectionMatrix(worldCamera.combined);

		shapeRenderer.setProjectionMatrix(worldCamera.combined);

		if (this.simulation != null) {
			renderSimulationObjects();
			renderObjectIds();
		}

	}

	private Color circleRenderColor = Color.CORAL;
	private Color circleOutlineColor = Color.BLACK;

	private void renderSimulationObjects() {

		shapeRenderer.setColor(circleRenderColor);
		for (CircleObject o : simulation.getCircleObjects()) {
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.circle(o.getX(), o.getY(), simulation.COLLISION_BODY_RADIUS);
			shapeRenderer.end();
		}

		for (CircleObject o : simulation.getCircleObjects()) {
			renderCircleOutline(o.getX(), o.getY(), simulation.COLLISION_BODY_RADIUS, circleOutlineColor);
		}

	}

	private void renderObjectIds() {
		batch.begin();
		for (CircleObject o : simulation.getCircleObjects()) {
			game.borderedDebugFont.draw(batch, String.valueOf(o.id), o.getX(), o.getY());
		}
		batch.end();
	}

	public void setKnockBackSimulation(KnockBackSimulation simulation) {
		this.simulation = simulation;
	}

	private void renderCircleOutline(float x, float y, float radius, Color color) {
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(color);
		shapeRenderer.circle(x, y, radius);
		shapeRenderer.end();
	}

	public void setClearColor(Color color) {
		if (color == null) {
			logger.warn("Color given was null");
			return;
		}
		clearColor = color;
	}

}
