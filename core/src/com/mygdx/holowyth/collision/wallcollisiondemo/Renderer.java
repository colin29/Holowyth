package com.mygdx.holowyth.collision.wallcollisiondemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.HoloGL;

public class Renderer {

	private Camera worldCamera;
	private ShapeRenderer shapeRenderer;
	private SpriteBatch batch;

	private WallCollisionSimulation simulation;

	private Color wallOutlineColor = Color.BLACK;
	private Color objectMotionColor = Color.CORAL;

	Logger logger = LoggerFactory.getLogger(this.getClass());
	private Color clearColor = Color.BLACK;

	public Renderer(Holowyth game, Camera worldCamera) {

		batch = game.batch;
		shapeRenderer = game.shapeRenderer;

		this.worldCamera = worldCamera;
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
			renderObstaclePolygons();
			renderMotionSegment();
		}

	}

	private void renderObstaclePolygons() {

		for (var poly : simulation.getObstaclePolygonsOriented()) {

			if (poly.isClockwise()) {
				HoloGL.renderPolygon(poly.toRawPolygon(), Color.PURPLE);
			} else {
				HoloGL.renderPolygon(poly.toRawPolygon(), wallOutlineColor);
			}

			for (var seg : poly.segments) {
				HoloGL.renderSegment(seg.getOutwardlyDisplacedSegment(simulation.getBodyRadius()), Color.CORAL);
			}

		}
	}

	private void renderMotionSegment() {
		HoloGL.renderArrow(simulation.getMotionSegment(), Color.CYAN);
	}

	public void setClearColor(Color color) {
		if (color == null) {
			logger.warn("Color given was null");
			return;
		}
		clearColor = color;
	}

	public void setSimulation(WallCollisionSimulation src) {
		simulation = src;
	}

}
