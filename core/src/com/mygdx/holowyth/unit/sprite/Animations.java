package com.mygdx.holowyth.unit.sprite;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.exceptions.HoloResourceNotFoundException;

/**
 * A simple resource holder class which units can select animated sprites from
 * 
 * @author Colin Ta
 *
 */
public class Animations {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<String, AnimatedSprite> sprites = new LinkedHashMap<String, AnimatedSprite>();

	private final String ANIMATIONS_DIR = Holowyth.ASSETS_PATH + "sprites/pipo/";

	public Animations() {
		loadAnimatedSprites();
	}

	private void loadAnimatedSprites() {

		File dir = new File(ANIMATIONS_DIR);

		// logger.debug("Loaded images in directory: " + StringUtils.removeStart(dir.getPath().replace("\\", "/"), Holowyth.ASSETS_PATH));

		File[] images = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".png");
			}
		});

		for (File img : images) {
			TextureParameter param = new TextureParameter();
			param.genMipMaps = true;

			// String dirPath = StringUtils.removeStart(dir.getPath().replace("\\", "/"), Holowyth.ASSETS_PATH);

			logger.debug("loaded: " + dir.getPath() + "/" + img.getName());

			AnimatedSprite sprite = new AnimatedSprite(dir.getPath() + "/" + img.getName());
			logger.debug("Added sprite with name {}", img.getName());
			sprites.put(img.getName(), sprite);
		}
	}

	public AnimatedSprite get(String name) {
		AnimatedSprite value = sprites.get(name);
		if (value == null) {
			throw new HoloResourceNotFoundException("Resource '" + name + "' not found");
		} else {
			return value;
		}

	}

}
