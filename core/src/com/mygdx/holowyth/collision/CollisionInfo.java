package com.mygdx.holowyth.collision;

/**
 * Carries info about a collision sufficient to calculate the result. This includes both colBody's involved
 * 
 * @author Colin Ta
 *
 */
public class CollisionInfo {
	/**
	 * The current colBody that is being processed. <br>
	 * Original reference, user may modify
	 */
	public final CircleCBInfo cur;
	public final Collidable other;

	// public final Vector2 collisionPoint;
	public final float pOfCollisionPoint;
	public final float collisionSurfaceNormalAngle; // this is the normal angle of the other object, at the collision point

	public CollisionInfo(CircleCBInfo curBody, Collidable other, float pOfCollisionPoint, float collisionSurfaceNormalAngle) {
		this.cur = curBody;
		this.other = other;
		// this.collisionPoint = new Vector2(x, y);
		this.pOfCollisionPoint = pOfCollisionPoint;
		this.collisionSurfaceNormalAngle = collisionSurfaceNormalAngle;

	}

}