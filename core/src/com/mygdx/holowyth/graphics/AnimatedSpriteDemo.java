package com.mygdx.holowyth.graphics;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.template.SimpleMapDemoScreen;

public class AnimatedSpriteDemo extends SimpleMapDemoScreen {

	private Texture walkSheet;
	private Animation<TextureRegion> down;
	private Animation<TextureRegion> left;
	private Animation<TextureRegion> right;
	private Animation<TextureRegion> up;
	private float stateTime;

	public AnimatedSpriteDemo(Holowyth game) {
		super(game);

		initCharAnimations();
	}

	private void initCharAnimations() {
		walkSheet = new Texture(Gdx.files.internal("assets/sprites/pipo-charachip028d.png"));
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

		activeAnimation = down;
	}

	@Override
	protected void mapStartup() {

	}

	@Override
	protected void mapShutdown() {
	}

	Animation<TextureRegion> activeAnimation;

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		boolean wasAnyKeyPressed = true;

		// Favour the sideways animations
		if (Gdx.input.isKeyPressed(Keys.LEFT)) {

			setActiveAnimation(left);
		} else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			setActiveAnimation(right);
		} else if (Gdx.input.isKeyPressed(Keys.DOWN)) {
			setActiveAnimation(down);
		} else if (Gdx.input.isKeyPressed(Keys.UP)) {
			setActiveAnimation(up);
		} else {
			wasAnyKeyPressed = false;
		}

		TextureRegion currentFrame = activeAnimation.getKeyFrame(stateTime, true);

		batch.begin();
		batch.draw(currentFrame, 50, 50);
		batch.end();

		if (wasAnyKeyPressed) {
			stateTime += delta;
		}

	}

	private void setActiveAnimation(Animation<TextureRegion> newState) {
		if (activeAnimation != newState) {
			activeAnimation = newState;
			stateTime = 0;
		}
	}

}
