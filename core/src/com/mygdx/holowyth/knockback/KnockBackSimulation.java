package com.mygdx.holowyth.knockback;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KnockBackSimulation {

	Logger logger = LoggerFactory.getLogger(KnockBackSimulation.class);

	List<CircleObject> circleObjects = new ArrayList<CircleObject>();

	public final float COLLISION_BODY_RADIUS = 13;

	public void tick() {
		logger.debug("ticked");

		for (CircleObject o : circleObjects) {
			if (o.getColBody().radius != COLLISION_BODY_RADIUS) {
				logger.warn("Multiple unit radiuses not supported [id={}]", o.id);
			}
		}

		Map<CircleCB, CircleObject> bodyToObject = new HashMap<CircleCB, CircleObject>();
		for (CircleObject o : circleObjects) {
			bodyToObject.put(o.getColBody(), o);
		}

		// Collision resolution is run once for every unit. It is possible that a later unit's velocity was modified
		// earlier this tick.
		for (CircleObject thisObject : circleObjects) {

			float x, y, vx, vy;

			x = thisObject.getX();
			y = thisObject.getY();
			vx = thisObject.getVx();
			vy = thisObject.getVy();

			List<CircleCB> allOtherBodies = new ArrayList<CircleCB>();
			for (CircleObject o : circleObjects)
				allOtherBodies.add(o.getColBody());
			allOtherBodies.remove(thisObject.getColBody());

			// 0. Determine all collisions over the course of this one circle moving.

			List<CircleCB> collisions = getCollisionsAlongLineSegment(x, y, x + vx, y + vy,
					thisObject.getColBody().radius, allOtherBodies);

			for (CircleCB colidee : collisions) {
				logger.debug("Collision between units id [{} {}]", thisObject.id, bodyToObject.get(colidee).id);
			}

			// Determine the first collision

			// 1a. If zeros collision, then just move normally

			x += vx;
			y += vy;

			thisObject.setPosition(x, y);
			continue;

			// 1b. If collision, find the first circle collision

			// 2. Resolve that collision, adjusting velocity.

			// Done.

		}
	}

	public static ArrayList<CircleCB> getCollisionsAlongLineSegment(float ix, float iy, float dx, float dy,
			float unitRadius,
			List<CircleCB> cbs) {
		ArrayList<CircleCB> collisions = new ArrayList<CircleCB>();
		for (CircleCB cb : cbs) {
			if (Line2D.ptSegDistSq(ix, iy, dx, dy, cb.pos.x, cb.pos.y) < (cb.radius + unitRadius)
					* (cb.radius + unitRadius)) {
				collisions.add(cb);
			}
		}
		return collisions;
	}

	public void addInitialObjects() {
		addCircleObject(500, 300).setVelocity(0.35f, 0.10f);
		addCircleObject(550, 300);
	}

	public CircleObject addCircleObject(float x, float y) {
		CircleObject o = new CircleObject(x, y, COLLISION_BODY_RADIUS);
		circleObjects.add(o);
		return o;
	}

	public void addCircleObject(float x, float y, float vx, float vy) {
		circleObjects.add(new CircleObject(x, y, COLLISION_BODY_RADIUS));
	}

	public List<CircleObject> getCircleObjects() {
		return circleObjects;
	}

}
