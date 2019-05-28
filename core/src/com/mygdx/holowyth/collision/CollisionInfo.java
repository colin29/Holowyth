package com.mygdx.holowyth.collision;

/**
 * Carries info about a collision, including both colBody's involved
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
	public final CircleCBInfo other;

	// public final Vector2 collisionPoint;
	public final float pOfCollisionPoint;
	public final float collisionAngle;

	public CollisionInfo(CircleCBInfo curBody, CircleCBInfo other, float pOfCollisionPoint, float collisionAngle) {
		this.cur = curBody;
		this.other = other;
		// this.collisionPoint = new Vector2(x, y);
		this.pOfCollisionPoint = pOfCollisionPoint;
		this.collisionAngle = collisionAngle;

	}

}
