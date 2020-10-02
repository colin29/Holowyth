package com.mygdx.holowyth.game.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.files.FileHandle;
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
import com.mygdx.holowyth.util.template.HoloBaseScreen;

/**
 * A Screen that can load Tiled map (NOT a full GameMap!)
 * This is being transitioned out, it's just used for older demos.
 * 
 * Provides the common base to be able to have a currently loaded Tiled map, and to get maps from disk
 * 
 * @author Colin Ta
 *
 */
public abstract class TiledMapLoadingScreen extends HoloBaseScreen  {

	Logger logger = LoggerFactory.getLogger(TiledMapLoadingScreen.class);

	/**
	 * The currently loaded map
	 */
	protected TiledMap map;

	protected TiledMapLoadingScreen(Holowyth game) {
		super(game);
	}

	/**
	 * Gets a map from disk, then loads it.
	 * 
	 * Warning: do not call loadMap() from a sub-class's constructor, because these call mapStartup() which is overrided.
	 * Though, if you call from a leaf class constructor, and mark mapStartup() as final it is okay.
	 */
	protected void loadMapFromDisk(String pathname) {
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
			logger.error("New map was null. New map not loaded");
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

		} catch (NullPointerException e) {
			throw new HoloException("Map load failed, missing properties", e);
		}

		this.map = newMap;
		logger.info("Map loaded");
		mapStartup();
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
				logger.debug("Selected file: {}", file.get(0).file().getAbsolutePath());
	
				loadMapFromDisk(file.get(0).file().getPath()); // tmx map loader uses relative path
			}
		});
	
		game.fileChooser.setDirectory(Holo.simpleMapsDirectory);
	}
	
	protected abstract void mapStartup();

	protected abstract void mapShutdown();

}
