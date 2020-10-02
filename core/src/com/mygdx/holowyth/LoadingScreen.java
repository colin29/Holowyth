package com.mygdx.holowyth;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.mygdx.holowyth.test.demos.combatdemo.CombatDemo;
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
		loadTexture("img/bg/shop.jpg");
		
		loadAllImagesInDirectory("img/sprites/");
	}

	@SuppressWarnings("null")
	private void loadTexture(String path) {
		TextureParameter param = new TextureParameter();
		param.genMipMaps = true;
		game.assets.load(path, Texture.class, param);
	}

	private void loadAllImagesInDirectory(String dirPath) {
//		listFiles(Holowyth.ASSETS_DISK_PATH + dirPath);
		loadAllImagesInDirectory(new File(Holowyth.ASSETS_DISK_PATH + dirPath));
	}
	private void listFiles(String dirPath){
		FileHandle[] files = Gdx.files.internal(dirPath).list();
		logger.debug("Found {} files in {}", files.length, dirPath);
		 for (FileHandle file : files) {
		 logger.debug("files in {}: {}", dirPath, file.name());
		 }
	}

	private void loadAllImagesInDirectory(File dir) {
		
		 logger.debug("Loaded images in directory: {}" , StringUtils.removeStart(dir.getPath().replace("\\",
		 "/"), Holowyth.ASSETS_DISK_PATH));

		File[] images = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".png");
			}
		});

		var dirHandle = Gdx.files.internal(dir.getPath());
		for (FileHandle file : dirHandle.list()) {
			logger.debug("Found file: " + file.name());
		}

		for (File image : images) {
			TextureParameter param = new TextureParameter();
			param.genMipMaps = true;

			String dirPath = StringUtils.removeStart(dir.getPath().replace("\\", "/"), Holowyth.ASSETS_DISK_PATH);
			// System.out.println("loaded: " + dirPath + "/" + image.getName());
			game.assets.load(dirPath + "/" + image.getName(), Texture.class, param);
		}

		File[] subDirs = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		for (File subDir : subDirs) {
			loadAllImagesInDirectory(subDir);
		}
	}
}
