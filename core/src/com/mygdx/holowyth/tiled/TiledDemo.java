package com.mygdx.holowyth.tiled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
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

		handleMousePanning(delta);

		camera.update();
		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();

	}

	/**
	 * Pan the view if the mouse is near the edge of the screen
	 */
	private void handleMousePanning(float delta) {

		int x = Gdx.input.getX();
		int y = Gdx.input.getY();

		final int screenHeight = Gdx.graphics.getHeight();
		final int screenWidth = Gdx.graphics.getWidth();

		float scrollMargin = 40f;
		float scrollSpeed = 300 * delta; // pixels per second

		float origX = camera.position.x;
		float origY = camera.position.y;

		if (y > screenHeight - scrollMargin)
			camera.translate(0, -scrollSpeed + snapLeftoverY);
		if (y < scrollMargin)
			camera.translate(0, scrollSpeed + snapLeftoverY);

		if (x > screenWidth - scrollMargin)
			camera.translate(scrollSpeed + snapLeftoverX, 0);
		if (x < scrollMargin)
			camera.translate(-scrollSpeed + snapLeftoverX, 0);

		System.out.println("Actual movespeed: " + (camera.position.x - origX - snapLeftoverX) / delta); // should be averaging 300

		snapCameraAndSaveRemainder();

		System.out.println(camera.position.x - origX);

		snapLeftoverX = 0;
		snapLeftoverY = 0;

	}

	private float snapLeftoverX;
	private float snapLeftoverY;

	/*
	 * Accumulate the leftovers and apply them to later movement, in order to prevent panning slow-down due to repeated rounding. Matters only with
	 * slow panning or high frame rate
	 */
	private void snapCameraAndSaveRemainder() {

		float dx = Math.round(camera.position.x) - camera.position.x;
		float dy = Math.round(camera.position.y) - camera.position.y;

		camera.position.set(Math.round(camera.position.x), Math.round(camera.position.y), 0);

		snapLeftoverX -= dx;
		snapLeftoverY -= dy;
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

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

}
