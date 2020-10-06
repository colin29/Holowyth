package com.mygdx.holowyth.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.holowyth.unit.sprite.Animations;
import com.mygdx.holowyth.util.tools.Timer;

public class GfxTest {

	
	private SpriteBatch batch;
	private Timer timer;
	
	Animation<TextureRegion> darkSpike;
	
	public GfxTest(SpriteBatch batch, Animations animations) {
		this.batch = batch;
		timer = new Timer();
		timer.start(0);
		
		darkSpike = animations.getEffect("dark_spike.png");
	}
	
	public void render() {
		batch.begin();
		batch.draw(darkSpike.getKeyFrame(timer.getTimeElapsedSeconds(), true), 200, 200);
		batch.end();
	}
}
