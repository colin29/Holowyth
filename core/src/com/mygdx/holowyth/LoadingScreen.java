package com.mygdx.holowyth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.mygdx.holowyth.gameScreen.combatDemo.CombatDemo;
import com.mygdx.holowyth.util.template.HoloBaseScreen;

public class LoadingScreen extends HoloBaseScreen {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public LoadingScreen(final Holowyth game) {
		super(game);
		queueAssets();
	}

	@Override
	public void show() {
		super.show();
		System.out.println("LOADING");
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.5f, 0.8f, 1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		camera.update();
		batch.setProjectionMatrix(camera.combined);

		update(delta);
	}

	@Override
	public void dispose() {
	}

	private void update(float delta) {
		if (game.assets.update()) {
			game.setScreen(new CombatDemo(game));
		}
	}

	private void queueAssets() {

		loadTexture("img/effects/magicCircle_blue.png");

		loadTexture("img/cursors/AbilityCursor.png");
		loadTexture("img/cursors/AttackCursor.png");
		loadTexture("img/cursors/cursor.png");
		loadTexture("img/cursors/MagicCursor.png");
		loadTexture("img/cursors/RetreatCursor.png");

	}

	@SuppressWarnings("null")
	private void loadTexture(String path) {
		TextureParameter  param = new TextureParameter();
		param.genMipMaps = true;
		game.assets.load(path, Texture.class, param);
	}
}
