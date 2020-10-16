package com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.test;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * Has the concept of a basic project object: position, speed/direction, duration
 * @author Colin
 *
 */
@NonNullByDefault
public class ProjectileMotion {
	final Point pos;
	
	private float speed;
	private float direction;
	private final Vector2 velocity = new Vector2();
	
	public ProjectileMotion(float speed, float direction, Projectile self) {
		pos =  self.pos;
		this.speed = speed;
		this.direction = direction;
		
	}

	protected void tick(){
		move();
	}
	
/**
 * Moves the projectile for one frame's motion according to its velocity
 */
protected final void move() {
	computeVelocity();
	pos.x += velocity.x;
	pos.y += velocity.y;
}

private void computeVelocity() {
	velocity.set(speed, 0);
	velocity.rotate(direction);
}

protected final void setSpeed(float speed) {
	if (speed < 0) {
		throw new HoloIllegalArgumentsException("Speed must be non-negative");
	}
	this.speed = speed;
	computeVelocity();
}

/**
 * Normalizes and sets angle
 */
protected final void setDirection(float angle) {
	this.direction = normalizeAngle(angle);
	computeVelocity();
}

/**
 * @return value in the range [0, 360)
 */
protected final static float normalizeAngle(float angle) {
	return (angle %= 360) > 0 ? angle : (angle + 360);
}

public final  float getVx() {
	return getVelocity().x;
}

public final float getVy() {
	return getVelocity().y;
}

public final float getSpeed() {
	return speed;
}

/**
 * @return angle in the range [0, 360)
 */
public final float getRotation() {
	return direction;
}

/**
 * Returns a new vector
 */
public final Vector2 getVelocity() {
	return new Vector2(velocity);
}



}

