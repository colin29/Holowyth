package com.mygdx.holowyth.util.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.tiled.MyAtlasTmxMapLoader;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.exceptions.HoloException;

/**
 * Works with a TMX map
 * 
 * Provides the common base functionality of a demo to be able to have a currently loaded map, and to get maps from disk
 * 
 * Also acts as an adapter to the Screen and InputProcessor classes, providing empty implementations for most of the methods. Override just what you
 * need.
 * 
 * 
 * @author Colin Ta
 *
 */
public abstract class DemoScreen extends HoloBaseScreen implements InputProcessor {

	Logger logger = LoggerFactory.getLogger(DemoScreen.class);

	/**
	 * The currently loaded map
	 */
	protected TiledMap map;

	protected DemoScreen(Holowyth game) {
		super(game);
	}

	@SuppressWarnings("unused")
	private void openFileChooserToLoadMap() {
		System.out.println("Opening Load Dialog");
		stage.addActor(game.fileChooser);

		game.fileChooser.setMode(Mode.OPEN);
		game.fileChooser.setSelectionMode(SelectionMode.FILES);
		game.fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected(Array<FileHandle> file) {
				System.out.println("Selected file: " + file.get(0).file().getAbsolutePath());

				loadMapFromDisk(file.get(0).file().getPath()); // tmx map loader uses relative path
			}
		});

		game.fileChooser.setDirectory(Holo.simpleMapsDirectory);
	}

	/**
	 * Gets a map from disk, then loads it.
	 */
	protected void loadMapFromDisk(String pathname) {
		logger.debug("pathname: " + pathname);
		TiledMap loadedMap = new MyAtlasTmxMapLoader().load(pathname);
		logger.debug(pathname);
		loadMap(loadedMap);
	}

	protected void loadMap(TiledMap newMap) {

		if (this.map != null) {
			mapShutdown();
			this.map = null;
		} else

		if (newMap == null) {
			System.out.println("Error: new map was null. New map not loaded");
			return;
		}

		try {
			MapProperties prop = newMap.getProperties();
			int tileWidth = (Integer) prop.get("tilewidth");
			int tileHeight = (Integer) prop.get("tileheight");

			int width = (Integer) prop.get("width");
			int height = (Integer) prop.get("height");

			prop.put("widthPixels", Integer.valueOf(tileWidth * width)); // use camel case for custom properties
			prop.put("heightPixels", Integer.valueOf(tileHeight * height));

			// logger.debug("{} {})", tileWidth, tileHeight);
		} catch (NullPointerException e) {
			throw new HoloException("Map load failed, missing properties", e);
		}

		this.map = newMap;

		// width="50" height="50" tilewidth="24" tileheight="24"

		// camera.position.set(newMap.width() / 2, newMap.height() / 2, 0);

		System.out.println("New map loaded");

		mapStartup();
	}

	protected void updateTitleBarInformation() {
		// TODO for TMX map
	}

	protected abstract void mapStartup();

	protected abstract void mapShutdown();

	// Cursor Related
	protected void renderCursor() {
		batch.setProjectionMatrix(fixedCam.combined);
		if (true) {
			batch.begin();
			Texture cursorImg = game.assets.get("icons/cursors/cursor.png", Texture.class);

			batch.draw(cursorImg, Gdx.input.getX(),
					Gdx.graphics.getHeight() - Gdx.input.getY() - cursorImg.getHeight());

			batch.end();
		}
	}

	/* Inherited methods */

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
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
