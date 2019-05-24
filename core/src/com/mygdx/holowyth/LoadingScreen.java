package com.mygdx.holowyth;

import java.io.File;
import java.io.FilenameFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.holowyth.editor.PolyMapEditor;

public class LoadingScreen implements Screen {

	private final Holowyth game;
	private Camera camera;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public LoadingScreen(final Holowyth game) {
		this.game = game;
		this.camera = new OrthographicCamera();

		queueAssets();
	}

	@Override
	public void show() {
		System.out.println("LOADING");
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.5f, 0.8f, 1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		game.batch.begin();
		game.font.draw(game.batch, "Screen: Loading", 20, 20);
		game.batch.end();

		update(delta);
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
	}

	private void update(float delta) {
		// When done, go to the polygon map editor (temp. changed to foo screen)
		if (game.assets.update()) {
			game.setScreen(new PolyMapEditor(game));
		}
	}

	public void queueAssets() {

		game.assets.load("ui/uiskin.atlas", TextureAtlas.class);
		game.assets.load("img/touhou_kagerou.jpg", Texture.class);
		game.assets.load("icons/cursors/cursor.png", Texture.class);

		// game.assets.load("img/igyo-boushi01.png", Texture.class);
		// game.assets.load("img/witch.png", Texture.class);
		// game.assets.load("img/witchresized.png", Texture.class);

		// Load all png files in this directory
		File dir = new File(game.ASSETS_PATH + "img/");

		File[] images = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".png");
			}
		});

		logger.debug("array of files " + images);

		for (File image : images) {
			System.out.println(image.getName());
			TextureParameter param = new TextureParameter();
			param.genMipMaps = true;
			game.assets.load("img/" + image.getName(), Texture.class, param);
		}

	}

}
