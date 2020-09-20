package com.mygdx.holowyth.tiled;

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
import com.mygdx.holowyth.map.GameMap;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.exceptions.HoloException;
import com.mygdx.holowyth.util.template.HoloBaseScreen;

/**
 * A Screen that can load a GameMap (which contains a tmx map and more)
 * 
 * @author Colin Ta
 *
 */
public abstract class GameMapLoadingScreen extends HoloBaseScreen {

	Logger logger = LoggerFactory.getLogger(MapLoadingScreen.class);

	/**
	 * The currently loaded game map
	 */
	protected GameMap map;

	private final TiledMapLoader tiledMapLoader;

	protected GameMapLoadingScreen(Holowyth game) {
		super(game);

		tiledMapLoader = new TiledMapLoader(stage, game.fileChooser);
	}

	/**
	 * Loads a GameMap by name. (Some of the information is simply being stored in
	 * the program atm, in a gameMap repository)
	 */
	protected void loadGameMap(String mapName) {

		// TODO: stub
		// if map exists, construct gamemap and add data, then load it.

		final String pathName = ""; // stub: query from GameMapRepository
		GameMap newMap = new GameMap(tiledMapLoader.getTiledMapFromTMXFile(pathName));
		// add data

		// otherwise throw exception

		// actually load the map
		loadMap(newMap);
	}

	/**
	 * Creates a GameMap and loads it. The other fields besides gameMap.tiledMap are
	 * empty.
	 */
	protected void loadTiledMapAsGameMap(String pathname) {
		loadMap(new GameMap(tiledMapLoader.getTiledMapFromTMXFile(pathname)));
	}

	protected void loadMap(GameMap newMap) {
		if (this.map != null) {
			mapShutdown();
			this.map = null;
		} else

		if (newMap == null) {
			logger.error("New map was null. New map not loaded");
			return;
		}
		addCustomPropertiesToTiledMap(newMap.getTilemap());

		this.map = newMap;
		logger.info("Map loaded");
		mapStartup();
	}

	private void addCustomPropertiesToTiledMap(TiledMap tiledMap) {
		try {
			MapProperties prop = tiledMap.getProperties();
			int tileWidth = (Integer) prop.get("tilewidth");
			int tileHeight = (Integer) prop.get("tileheight");

			int width = (Integer) prop.get("width");
			int height = (Integer) prop.get("height");

			prop.put("widthPixels", Integer.valueOf(tileWidth * width)); // use camel case for custom properties
			prop.put("heightPixels", Integer.valueOf(tileHeight * height));

		} catch (NullPointerException e) {
			throw new HoloException("Map load failed, missing properties", e);
		}
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

				loadGameMap(file.get(0).file().getPath()); // tmx map loader uses relative path
			}
		});

		game.fileChooser.setDirectory(Holo.simpleMapsDirectory);
	}

	protected abstract void mapStartup();

	protected abstract void mapShutdown();

}
