package com.mygdx.holowyth.unit.sprite;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;
import com.mygdx.holowyth.util.exceptions.HoloResourceNotFoundException;

/**
 * A simple resource holder class which units can get player sprites or visual gfx from
 * 
 * @author Colin Ta
 *
 */
public class Animations {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<String, AnimatedSprite> sprites = new LinkedHashMap<>();
	private final Map<String, Animation<TextureRegion>> effects = new LinkedHashMap<>();

	public Animations() {
		loadAnimatedSprites();
		loadEffects();
	}

	private void loadAnimatedSprites() {

		loadAnimatedSprite("img/sprites/pipo/pipo-charachip001b.png");
		loadAnimatedSprite("img/sprites/pipo/pipo-charachip017c.png");
		loadAnimatedSprite("img/sprites/pipo/pipo-charachip028d.png");
		loadAnimatedSprite("img/sprites/pipo/pipo-charachip030e.png");

		loadAnimatedSprite("img/sprites/pipo/goblin1.png");
	}
	
	private void loadEffects() {
		loadEffect("img/effects/dark_spike.png", 192, 192, 0.10f);
	}
	
	private void loadEffect(String path, int frameWidth, int frameHeight, float timePerFrame) {
		var effect = fetchEffect(path, frameWidth, frameHeight, timePerFrame);
		var parts = path.split("/");
		String name = parts[parts.length - 1];
		logger.debug("Added effect with name {}", name);
		effects.put(name, effect);
	}
	
	private Animation<TextureRegion> fetchEffect(String path, int frameWidth, int frameHeight, float timePerFrame) {
		return new Animation<TextureRegion>(timePerFrame, getKeyFrames(path, frameWidth, frameHeight));
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
	public AnimatedSprite getSprite(String name) {
		AnimatedSprite value = sprites.get(name);
		if (value == null) {
			throw new HoloResourceNotFoundException("Sprite '" + name + "' not found");
		} else {
			return value;
		}

	}
	public Animation<TextureRegion> getEffect(String name){
		Animation<TextureRegion> value = effects.get(name);
		if (value == null) {
			throw new HoloResourceNotFoundException("Effect '" + name + "' not found");
		} else {
			return value;
		}
	}
	
	private static TextureRegion[] getKeyFrames(String path, int frameWidth, int frameHeight) {
		
		Texture origTexture  = new Texture(Gdx.files.internal(Holowyth.ASSETS_DISK_PATH + path));
		
		TextureRegion[][] tex = TextureRegion.split(origTexture,
				frameWidth,
				frameHeight);
		
		if(tex.length == 0) {
			throw new HoloIllegalArgumentsException("TextureRegion.split produced 0 tiles");
		}

		int FRAME_ROWS = tex.length;
		int FRAME_COLS = tex[0].length;
		
		// Place the regions into a 1D array in the correct order, starting from the top
		// left, going across first. The Animation constructor requires a 1D array.

		TextureRegion[] keyFrames = new TextureRegion[FRAME_ROWS*FRAME_COLS];

		for (int i = 0; i < FRAME_ROWS; i++) {
			for (int j = 0; j < FRAME_COLS; j++) {
				keyFrames[i*FRAME_COLS + j] = tex[i][j];
			}
		}
		
		return keyFrames;
	}

}
