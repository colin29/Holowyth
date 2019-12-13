package com.mygdx.holowyth.test.camera;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class TestNoViewPort extends InputAdapter implements ApplicationListener {

	private OrthographicCamera camera;

	private ShapeRenderer shapeRenderer;

	private static final int WORLD_WIDTH = 960;
	private static final int WORLD_HEIGHT = 640;

	private float shapeWidth = 100;
	private float shapeHeight = 100;
	private float shapeX = 100;
	private float shapeY = 100;

	@Override
	public void create() {
		camera = new OrthographicCamera();
		camera.setToOrtho(false);
		camera.update();
		shapeRenderer = new ShapeRenderer();
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// camera.update();

		// Draw a rectangle covering the whole visible world
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(Color.DARK_GRAY);
		shapeRenderer.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
		shapeRenderer.end();

		// Draw our clickable shape
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(Color.WHITE);
		shapeRenderer.rect(shapeX, shapeY, shapeWidth, shapeHeight);
		shapeRenderer.end();
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {

		// Gdx.app.log("TestViewPoint", "Screen coords: " + screenX + " " + (Gdx.graphics.getHeight() - screenY));

		Vector3 click = camera.unproject(new Vector3(screenX, screenY, 0)); // get click in world coordinates

		Gdx.app.log("TestViewPoint", "World coords: " + click.x + " " + click.y);

		Rectangle rect = new Rectangle(shapeX, shapeY, shapeWidth, shapeHeight);
		if (rect.contains(click.x, click.y)) {
			Gdx.app.log("TestViewPoint", "Click inside shape detected!");
		}

		return false;
	}

	public static void main(String args[]) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "ViewPoint Test";
		config.width = WORLD_WIDTH;
		config.height = WORLD_HEIGHT;
		config.samples = 5;
		config.vSyncEnabled = false;
		config.foregroundFPS = 0;
		new LwjglApplication(new TestNoViewPort(), config);
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	}

}
