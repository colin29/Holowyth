package com.mygdx.holowyth.unit;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * 
 * Holds the animation information of 1 animated sprite. Multiple units can refer to and use the same animated sprite.
 * 
 * Supports loading and storing state of its child animations.
 * 
 * @author Colin Ta
 *
 */
public class AnimatedSprite {

	private final Animation<TextureRegion> down;
	private final Animation<TextureRegion> left;
	private final Animation<TextureRegion> right;
	private final Animation<TextureRegion> up;

	private Texture walkSheet;

	public AnimatedSprite(String path) {
		walkSheet = new Texture(Gdx.files.internal(path));
		TextureRegion[][] tmp = TextureRegion.split(walkSheet,
				32,
				32);

		final int FRAME_ROWS = 4;
		final int FRAME_COLS = 3;
		final int NUM_ANIMATIONS = FRAME_ROWS;

		// Place the regions into a 1D array in the correct order, starting from the top
		// left, going across first. The Animation constructor requires a 1D array.

		ArrayList<TextureRegion[]> keyFrames = new ArrayList<TextureRegion[]>();
		for (int i = 0; i < NUM_ANIMATIONS; i++) {
			keyFrames.add(new TextureRegion[FRAME_COLS]);
		}

		for (int i = 0; i < FRAME_ROWS; i++) {
			for (int j = 0; j < FRAME_COLS; j++) {
				keyFrames.get(i)[j] = tmp[i][j];
			}
		}

		float timePerFrame = 0.25f;

		down = new Animation<TextureRegion>(timePerFrame, keyFrames.get(0));
		left = new Animation<TextureRegion>(timePerFrame, keyFrames.get(1));
		right = new Animation<TextureRegion>(timePerFrame, keyFrames.get(2));
		up = new Animation<TextureRegion>(timePerFrame, keyFrames.get(3));
	}

	public Animation<TextureRegion> getDown() {
		return down;
	}

	public Animation<TextureRegion> getLeft() {
		return left;
	}

	public Animation<TextureRegion> getRight() {
		return right;
	}

	public Animation<TextureRegion> getUp() {
		return up;
	}
}
