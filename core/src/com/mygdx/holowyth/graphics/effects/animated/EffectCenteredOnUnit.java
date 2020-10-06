package com.mygdx.holowyth.graphics.effects.animated;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.holowyth.game.MapInstanceInfo;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.sprite.AnimatedEffect;
import com.mygdx.holowyth.unit.sprite.Animations;
import com.mygdx.holowyth.util.ShapeDrawerPlus;
import com.mygdx.holowyth.util.tools.Timer;

@NonNullByDefault
public class EffectCenteredOnUnit extends GraphicEffect {

	UnitInfo unit;
	final AnimatedEffect effect;
	private Timer timer = new Timer();

	int width, height;
	float alpha = 1;

	public EffectCenteredOnUnit(UnitInfo unit, String animName, MapInstanceInfo world, Animations animations) {
		super(world, animations);
		this.unit = unit;
		this.effect = animations.getEffect(animName);

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
		timer.start(0);
	}

	@Override
	public void tick() {
	}

	@Override
	public void render(SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
		
		int origSrc, origDest;
		origSrc = batch.getBlendSrcFunc();
		origDest = batch.getBlendDstFunc();
		if(effect.additiveBlending){
			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);	
		}
		batch.begin();
		batch.setColor(1, 1, 1, alpha);
		batch.draw(effect.anim.getKeyFrame(timer.getTimeElapsedSeconds()), unit.getX() - width / 2, unit.getY() - height / 2,
				width, height);
		batch.setColor(1, 1, 1, 1f);
		batch.end();
		batch.flush();
		batch.setBlendFunction(origSrc, origDest);
		
		if(effect.anim.isAnimationFinished(timer.getTimeElapsedSeconds())) {
			markAsComplete();
		}
		
	}

}
