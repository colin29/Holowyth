package com.mygdx.holowyth.graphics.effects.animated;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction;
import com.mygdx.holowyth.game.MapInstanceInfo;
import com.mygdx.holowyth.unit.sprite.AnimatedEffectSprite;
import com.mygdx.holowyth.util.ShapeDrawerPlus;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.tools.Timer;

@NonNullByDefault
public class AnimEffectOnPos extends AnimatedEffect {

	final AnimatedEffectSprite effect;
	private Timer timer = new Timer();

	int width, height;
	float alpha = 1;
	float rotation = 0; // in degrees
	public boolean loop;
	
	float delaySeconds;
	
	PosSource pos;
	Point offset = new Point();

	public AnimEffectOnPos(PosSource posSource, String animName, MapInstanceInfo mapInstance) {
		super(mapInstance, mapInstance.getAnimations());
		this.effect = animations.getEffect(animName);
		
		pos = posSource;

		width = effect.anim.getKeyFrames()[0].getRegionWidth();
		height = effect.anim.getKeyFrames()[0].getRegionHeight();
	}

	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	@Override
	public void begin() {
		timer.start(delaySeconds);
	}

	@Override
	public void tick() {
	}
	
	public void pauseAnimation() {
		timer.pause();
	}
	public void resumeAnimation() {
		timer.resume();
	}


	@Override
	public void render(float delta, SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
		
		timer.update(delta);
		if(timer.delayStillActive()) {
			return;
		}
		
		int origSrc, origDest;
		origSrc = batch.getBlendSrcFunc();
		origDest = batch.getBlendDstFunc();
		if(effect.additiveBlending){
			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);	
		}
		batch.begin();
		batch.setColor(1, 1, 1, alpha);
		float centerX =pos.getX() + offset.x - width / 2;
		float centerY = pos.getY() + offset.y - height / 2;
//		batch.draw(effect.anim.getKeyFrame(timer.getTimeElapsedSeconds()), pos.getX() - width / 2, pos.getY() - height / 2,
//				width, height);
		batch.draw(effect.anim.getKeyFrame(timer.getTimeElapsedSeconds()), centerX, centerY, width/2, height/2, width, height, 1, 1, rotation);
		batch.setColor(1, 1, 1, 1f);
		batch.end();
		batch.flush();
		batch.setBlendFunction(origSrc, origDest);
		if(effect.anim.isAnimationFinished(timer.getTimeElapsedSeconds())) {
			if(loop) {
				timer.restart();
			}else {
				markAsComplete();
			}
		}
		
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}
	public void setDelay(float gameFrames) {
		if(timer.isStarted()) {
			logger.warn("Should set delay before animEffect is added to mapInstance");
			return;
		}
		delaySeconds = gameFrames / 60;
	}
	public void offset(float x, float y) {
		offset.x += x;
		offset.y += y;
	}
}
