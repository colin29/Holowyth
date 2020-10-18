package com.mygdx.holowyth.unit.sprite;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * The sprite shares the same semantic xy position as its parent unit
 * 
 * @author Colin Ta
 *
 */
public class UnitGraphics {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private float stateTime;

	private Unit self;

	private final int SPRITE_WIDTH = 32;
	private final int SPRITE_HEIGHT = 32;

	private AnimatedUnitSprite animatedSprite; // can be null
	private @Nullable String headSpriteName;

	private Animation<TextureRegion> activeAnimation;
	private Animation<TextureRegion> prevAnimation;

	private TextureRegion currentFrame;
	
	boolean isPaused;

	public UnitGraphics(Unit self) {
		this.self = self;
	}

	public void clearMapLifetimeData() {
		stateTime = 0;
	}
	
	public void updateAndRender(float delta, SpriteBatch batch) {
		if (animatedSprite == null)
			return;

		updateActiveAnimation(delta);

		currentFrame = activeAnimation.getKeyFrame(stateTime, true);

		batch.begin();
		batch.draw(currentFrame, self.getX() - SPRITE_WIDTH / 2, self.getY() - SPRITE_HEIGHT / 2);
		batch.end();
	}
	
	private void updateActiveAnimation(float delta) {

		if (self.getMotion().isBeingKnockedBack())
			return;
		if (self.getMotion().getVelocity().isZero() && !self.isAttacking())
			return;

		float angle = getUnitAngle();

		float horizontalAngleSize = 45;

		if (angle < horizontalAngleSize || angle >= 360 - horizontalAngleSize) {
			setActiveAnimation(animatedSprite.getRight());
		} else if (angle < 180 - horizontalAngleSize) {
			setActiveAnimation(animatedSprite.getUp());
		} else if (angle < 180 + horizontalAngleSize) {
			setActiveAnimation(animatedSprite.getLeft());
		} else if (angle < 360 - horizontalAngleSize) {
			setActiveAnimation(animatedSprite.getDown());
		}

		if (activeAnimation == prevAnimation && !isPaused &&  !self.getMotion().isBeingKnockedBack() && !self.isAttacking()) {
			stateTime += delta;
		}
		prevAnimation = activeAnimation;

	}

	private float getUnitAngle() {
		if (self.isAttacking()) {
			UnitOrderable enemy = self.getAttacking();
			return Point.getAngleInDegrees(self.getPos(), enemy.getPos());
		} else {
			Vector2 vel = self.getMotion().getVelocityRegardlessOfMode();
			return vel.angle(); // angle between [0, 360)
		}
	}

	public void pauseAnimation() {
		isPaused = true;
	}
	public void resumeAnimation() {
		isPaused = false;
	}
	
	
	private void setActiveAnimation(Animation<TextureRegion> newState) {
		if (activeAnimation != newState) {
			activeAnimation = newState;
			stateTime = 0;
		}
	}

	public void setAnimatedSprite(AnimatedUnitSprite sprite) {
		animatedSprite = sprite;
		if (animatedSprite != null) {
			activeAnimation = animatedSprite.getDown();
			prevAnimation = activeAnimation;
		}
	}

	public AnimatedUnitSprite getAnimatedSprite() {
		return animatedSprite;
	}

	public String getHeadSpriteName() {
		return headSpriteName;
	}

	public void setHeadSpriteName(String headSpriteName) {
		this.headSpriteName = headSpriteName;
	}

	public Image getHeadSprite() {
		if(self.getMapInstance().getAssets().isLoaded(headSpriteName)) { // isLoaded will return false if null
			return  getImage(headSpriteName);
		}else {
			return getImage("img/sprites/head/Default.png");
		}
	}
	private Image getImage(String path) {
		return new Image(self.getMapInstance().getAssets().get(path, Texture.class));
	}


}
