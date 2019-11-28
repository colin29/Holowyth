package com.mygdx.holowyth.skill.skillsandeffects.projectiles;

/**
 * May not cover all of the projectile features but the common ones
 */
public interface Projectile {
	void tick();

	float getX();

	float getY();

	boolean isExpired();

	boolean isCollided();
}
