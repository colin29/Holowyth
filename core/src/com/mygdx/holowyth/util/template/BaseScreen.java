package com.mygdx.holowyth.util.template;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.holowyth.Holowyth;

public abstract class BaseScreen implements Screen {

	protected final Holowyth game;
	protected OrthographicCamera camera; // Used for rendering world objects (that use world coordinates)
	protected OrthographicCamera fixedCam; // Used for rendering objects on the screen coordinate.

	protected Stage stage;
	protected Table root;
	protected SpriteBatch batch;

	protected ShapeRenderer shapeRenderer;

	public BaseScreen(final Holowyth game) {
		this.game = game;
		this.batch = game.batch;
		this.shapeRenderer = game.shapeRenderer;

		this.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.fixedCam = new OrthographicCamera();
		fixedCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		createStage();
	}

	@Override
	public abstract void render(float delta);

	@Override
	public void show() {
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
	public void hide() {
	}

	@Override
	public void dispose() {
	}

	private void createStage() {
		stage = new Stage(new ScreenViewport());

		root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		root.top().left();
	}
}
