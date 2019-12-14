package com.mygdx.holowyth.unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
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

	private AnimatedSprite animatedSprite; // can be null

	private Animation<TextureRegion> activeAnimation;
	private Animation<TextureRegion> prevAnimation;

	private TextureRegion currentFrame;

	UnitGraphics(Unit parent) {
		this.self = parent;
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

		if (self.motion.isBeingKnockedBack())
			return;
		if (self.motion.getVelocity().isZero() && !self.isAttacking())
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

		if (activeAnimation == prevAnimation && !self.motion.isBeingKnockedBack() && !self.isAttacking()) {
			stateTime += delta;
		}

	}

	private float getUnitAngle() {
		if (self.isAttacking()) {
			var enemy = self.getAttacking();
			return Point.getAngleInDegrees(self.getPos(), enemy.getPos());
		} else {
			Vector2 vel = self.motion.getVelocityRegardlessOfMode();
			return vel.angle(); // angle between [0, 360)
		}
	}

	private void setActiveAnimation(Animation<TextureRegion> newState) {
		if (activeAnimation != newState) {
			activeAnimation = newState;
			stateTime = 0;
		}
	}

	public void setAnimatedSprite(AnimatedSprite sprite) {
		animatedSprite = sprite;
		if (animatedSprite != null) {
			activeAnimation = animatedSprite.getDown();
			prevAnimation = activeAnimation;
		}
	}

}
