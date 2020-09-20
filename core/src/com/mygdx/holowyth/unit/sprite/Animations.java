package com.mygdx.holowyth.unit.sprite;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public Animations() {
		loadAnimatedSprites();
	}

	private void loadAnimatedSprites() {

		loadAnimatedSprite("img/sprites/pipo/pipo-charachip001b.png");
		loadAnimatedSprite("img/sprites/pipo/pipo-charachip017c.png");
		loadAnimatedSprite("img/sprites/pipo/pipo-charachip028d.png");
		loadAnimatedSprite("img/sprites/pipo/pipo-charachip030e.png");

		loadAnimatedSprite("img/sprites/pipo/goblin1.png");

	}

	private void loadAnimatedSprite(String path) {
		AnimatedSprite sprite = new AnimatedSprite(path);
		var parts = path.split("/");
		String name = parts[parts.length - 1];
		logger.debug("Added sprite with name {}", name);
		sprites.put(name, sprite);

	}

	/**
	 * Throws HoloResourceNotFoundException if sprite not found
	 * @return
	 */
	public AnimatedSprite get(String name) {
		AnimatedSprite value = sprites.get(name);
		if (value == null) {
			throw new HoloResourceNotFoundException("Resource '" + name + "' not found");
		} else {
			return value;
		}

	}

}
