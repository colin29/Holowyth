package com.mygdx.holowyth.knockback.collision;

import com.mygdx.holowyth.knockback.CircleCB;

/**
 * Carries info about a collision, including the other colBody involved
 * 
 * @author Colin Ta
 *
 */
public class CollisionInfo {
	/**
	 * The other colBody <br>
	 * Original reference, user may modify
	 */
	public final CircleCB circleCB;
	// public final Vector2 collisionPoint;
	public final float pOfCollisionPoint;
	public final float collisionAngle;

	public CollisionInfo(CircleCB circleCB, float pOfCollisionPoint, float collisionAngle) {
		this.circleCB = circleCB;
		// this.collisionPoint = new Vector2(x, y);
		this.pOfCollisionPoint = pOfCollisionPoint;
		this.collisionAngle = collisionAngle;
	}

}
