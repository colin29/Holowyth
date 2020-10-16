package com.mygdx.holowyth.unit.sprite;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.holowyth.util.Holo;
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

	private final Map<String, AnimatedUnitSprite> sprites = new LinkedHashMap<>();
	private final Map<String,  AnimatedEffectSprite> effects = new LinkedHashMap<>();

	public Animations() {
		loadAnimatedSprites();
		loadEffects();
	}

	private static String WALKING_DIR = "img/sprites/walking/";
	
	private void loadAnimatedSprites() {

		loadAnimatedSprite(WALKING_DIR + "lecia.png");
		loadAnimatedSprite(WALKING_DIR + "elvin.png");
		loadAnimatedSprite(WALKING_DIR + "sonia.png");
		loadAnimatedSprite(WALKING_DIR + "renee.png");
		loadAnimatedSprite(WALKING_DIR + "seth.png");
		loadAnimatedSprite(WALKING_DIR + "mikal.png");
		
		loadAnimatedSprite(WALKING_DIR + "sally.png"); // unused
		
		loadAnimatedSprite(WALKING_DIR + "goblin1.png");
	}
	
	private void loadEffects() {
		loadEffect("img/effects/dark_spike.png", 192, 192, 0.10f);
		loadEffect("img/effects/holy_cross.png", 192, 192, 0.10f);
		loadEffect("img/effects/casting_glow.png", 120, 120, 0.10f, true);
		loadEffect("img/effects/lightning_ball.png", 192, 192, 0.10f);
		
		loadAllImagesInDirectoryAsEffects(new File(Holo.ASSETS_DISK_PATH + "img/effects/rixu/"), 64, 64, 0.02f);
	}
	
	
	private AnimatedEffectSprite loadEffect(String path, int frameWidth, int frameHeight, float timePerFrame) {
		return loadEffect(path, frameWidth, frameHeight, timePerFrame, false);
	}
	
	private AnimatedEffectSprite loadEffect(String path, int frameWidth, int frameHeight, float timePerFrame, boolean additiveBlending) {
		var anim = fetchEffect(path, frameWidth, frameHeight, timePerFrame);
		anim.setPlayMode(PlayMode.NORMAL);   // Default, can change
		var parts = path.split("/");
		String name = parts[parts.length - 1];
		logger.debug("Added effect with name {}", name);
		
		var effect = new AnimatedEffectSprite(anim);
		effect.additiveBlending = additiveBlending;
		effects.put(name, effect);
		return effect;
	}
	
	private @NonNull Animation<@NonNull TextureRegion> fetchEffect(String path, int frameWidth, int frameHeight, float timePerFrame) {
		return new Animation<@NonNull TextureRegion>(timePerFrame, getKeyFrames(path, frameWidth, frameHeight));
	}

	private void loadAnimatedSprite(String path) {
		AnimatedUnitSprite sprite = new AnimatedUnitSprite(path);
		var parts = path.split("/");
		String name = parts[parts.length - 1];
		logger.debug("Added sprite with name {}", name);
		sprites.put(name, sprite);
	}
	

	/**
	 * Throws HoloResourceNotFoundException if sprite not found
	 * @return
	 */
	public AnimatedUnitSprite getSprite(String name) {
		AnimatedUnitSprite value = sprites.get(name);
		if (value == null) {
			throw new HoloResourceNotFoundException("Sprite '" + name + "' not found");
		} else {
			return value;
		}

	}
	public @NonNull AnimatedEffectSprite getEffect(String name){
		AnimatedEffectSprite value = effects.get(name);
		if (value == null) {
			throw new HoloResourceNotFoundException("Effect '" + name + "' not found");
		} else {
			return value;
		}
	}
	
	private static @NonNull TextureRegion[] getKeyFrames(String path, int frameWidth, int frameHeight) {
		
		Texture origTexture  = new Texture(Gdx.files.internal(Holo.ASSETS_DISK_PATH + path));
		
		@SuppressWarnings("null")
		@NonNull TextureRegion[][] tex = TextureRegion.split(origTexture,
				frameWidth,
				frameHeight);
		
		if(tex.length == 0) {
			throw new HoloIllegalArgumentsException("TextureRegion.split produced 0 tiles");
		}

		int FRAME_ROWS = tex.length;
		int FRAME_COLS = tex[0].length;
		
		// Place the regions into a 1D array in the correct order, starting from the top
		// left, going across first. The Animation constructor requires a 1D array.

		@NonNull TextureRegion[] keyFrames = new @NonNull TextureRegion[FRAME_ROWS*FRAME_COLS];

		for (int i = 0; i < FRAME_ROWS; i++) {
			for (int j = 0; j < FRAME_COLS; j++) {
				keyFrames[i*FRAME_COLS + j] = tex[i][j];
			}
		}
		
		return keyFrames;
	}
	
	private void loadAllImagesInDirectoryAsEffects(File dir, int imageWidth, int imageHeight, float secPerFrame) {
		
		 logger.debug("Loaded images in directory: {}" , StringUtils.removeStart(dir.getPath().replace("\\",
		 "/"), Holo.ASSETS_DISK_PATH));

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

			String dirPath = StringUtils.removeStart(dir.getPath().replace("\\", "/"), Holo.ASSETS_DISK_PATH);
			 System.out.println("loaded: " + dirPath + "/" + image.getName());
//			game.assets.load(dirPath + "/" + image.getName(), Texture.class, param);
			loadEffect(dirPath + "/" + image.getName(), imageWidth, imageHeight, secPerFrame);
		}
		

		File[] subDirs = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		for (File subDir : subDirs) {
			loadAllImagesInDirectoryAsEffects(subDir, imageHeight, imageHeight, secPerFrame);
		}
	}

}
