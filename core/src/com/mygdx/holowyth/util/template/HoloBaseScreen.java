package com.mygdx.holowyth.util.template;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.Cameras;
import com.mygdx.holowyth.util.ShapeDrawerPlus;

/**
 * Holds cameras, rendering equipment and common scene2d fields.
 * 
 * Also acts as an adapter to the Screen and InputProcessor classes, providing empty implementations for most of the methods. Override just what you
 * need.
 *
 */
public abstract class HoloBaseScreen implements Screen, InputProcessor {

	protected final Holowyth game;
	protected final OrthographicCamera camera; // Used for rendering world objects (that use world coordinates)
	protected final OrthographicCamera fixedCam; // Used for rendering objects on the screen coordinate.
	private final Cameras cameras;

	protected Stage stage;
	protected Table root;

	protected Skin skin;

	public final SpriteBatch batch;
	public final ShapeRenderer shapeRenderer;
	public final ShapeDrawerPlus shapeDrawer;

	public final AssetManager assets;

	public HoloBaseScreen(final Holowyth game) {
		this.game = game;
		batch = game.batch;
		shapeRenderer = game.shapeRenderer;
		shapeDrawer = game.shapeDrawer;
		assets = game.assets;
		skin = game.skin;

		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		fixedCam = new OrthographicCamera();
		fixedCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		cameras = new Cameras(camera, fixedCam);

		createStage();
	}

	@Override
	public abstract void render(float delta);
	
	/**
	 * If you are using an input multiplexer you should set inputProcessor to multiplexer instead and not call super.show()
	 */
	@Override
	public void show() {
		Gdx.input.setInputProcessor(this);
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
		Gdx.input.setInputProcessor(null);
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

	public Cameras getCameras() {
		return cameras;
	}
	
	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

}
