package com.mygdx.holowyth.tiled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.template.BaseHoloScreen;

public class TiledDemo extends BaseHoloScreen implements InputProcessor {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private OrthogonalTiledMapRenderer tiledMapRenderer;
	private TiledMap map;

	public TiledDemo(Holowyth game) {
		super(game);
		loadMap();
		Gdx.input.setInputProcessor(this);
	}

	public void loadMap() {
		map = new MyAtlasTmxMapLoader().load("assets/maps/forest1.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(map);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();

	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Input.Keys.A)
			camera.translate(-32, 0);
		if (keycode == Input.Keys.D)
			camera.translate(32, 0);
		if (keycode == Input.Keys.W)
			camera.translate(0, 32);
		if (keycode == Input.Keys.S)
			camera.translate(0, -32);

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
