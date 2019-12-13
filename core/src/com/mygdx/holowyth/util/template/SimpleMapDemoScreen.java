package com.mygdx.holowyth.util.template;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloIO;
import com.mygdx.holowyth.util.exception.ErrorCode;
import com.mygdx.holowyth.util.exception.HoloException;

/**
 * Works with the simple kind of map, which is used in older demos which have no need to load TMX map
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
public abstract class SimpleMapDemoScreen extends HoloBaseScreen implements InputProcessor {

	/**
	 * The currently loaded map
	 */
	protected Field map;

	protected SimpleMapDemoScreen(Holowyth game) {
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

				loadMapFromDisk(file.get(0).file().getAbsolutePath());
			}
		});

		game.fileChooser.setDirectory(Holo.simpleMapsDirectory);
	}

	/**
	 * Gets a map from disk, then loads it.
	 */
	protected void loadMapFromDisk(String pathname) {
		try {
			Field loadedMap = HoloIO.getMapFromDisk(pathname);
			loadMap(loadedMap);
		} catch (HoloException e) {
			if (e.code == ErrorCode.IO_EXCEPTION) {
				System.out.println("IO Error, map not loaded");
				return;
			}
		}

	}

	protected void loadMap(Field newMap) {

		if (this.map != null) {
			mapShutdown();
			this.map = null;
		} else

		if (newMap == null) {
			System.out.println("Error: new map was null. New map not loaded");
			return;
		}

		this.map = newMap;
		newMap.hasUnsavedChanges = false;
		camera.position.set(newMap.width() / 2, newMap.height() / 2, 0);

		System.out.println("New map loaded");

		mapStartup();
	}

	protected void updateTitleBarInformation() {
		if (this.map == null) {
			Gdx.graphics.setTitle(Holo.titleName + " --- " + "No map loaded");
		} else {
			String starText;
			starText = (this.map.hasUnsavedChanges) ? "*" : "";
			Gdx.graphics.setTitle(
					Holo.titleName + " --- " + map.name + " [" + map.width() + "x" + map.height() + "] " + starText);
		}
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
