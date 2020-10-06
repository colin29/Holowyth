package com.mygdx.holowyth.graphics.effects.animated;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.holowyth.game.MapInstanceInfo;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.sprite.Animations;
import com.mygdx.holowyth.util.ShapeDrawerPlus;
import com.mygdx.holowyth.util.tools.Timer;

@NonNullByDefault
public class EffectCenteredOnUnit extends GraphicalEffect {

	UnitInfo unit;
	Animation<TextureRegion> anim;
	private Timer timer = new Timer();

	int width, height;
	float alpha = 1;

	public EffectCenteredOnUnit(UnitInfo unit, String animName, MapInstanceInfo world, Animations animations) {
		super(world, animations);
		this.unit = unit;
		this.anim = animations.getEffect(animName);

		width = anim.getKeyFrames()[0].getRegionWidth();
		height = anim.getKeyFrames()[0].getRegionHeight();
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
		anim.setPlayMode(PlayMode.LOOP);
	}

	@Override
	public void tick() {
	}

	@Override
	public void render(SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
		batch.begin();
		batch.setColor(1, 1, 1, alpha);
		batch.draw(anim.getKeyFrame(timer.getTimeElapsedSeconds()), unit.getX() - width / 2, unit.getY() - height / 2,
				width, height);
		batch.setColor(1, 1, 1, 1f);
		batch.end();
//		if(anim.isAnimationFinished(timer.getTimeElapsedSeconds())) {
//			markAsComplete();
//		}
	}

}
