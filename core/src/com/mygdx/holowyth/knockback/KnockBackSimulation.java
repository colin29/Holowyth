package com.mygdx.holowyth.knockback;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KnockBackSimulation {

	Logger logger = LoggerFactory.getLogger(KnockBackSimulation.class);

	List<CircleObject> circleObjects = new ArrayList<CircleObject>();

	public final float COLLISION_BODY_RADIUS = 13;

	public void tick() {
		logger.debug("ticked");

		for (CircleObject o : circleObjects) {
			float x, y, vx, vy;

			x = o.getX();
			y = o.getY();
			vx = o.getVx();
			vy = o.getVy();

			x += vx;
			y += vy;

			o.setPosition(x, y);

		}
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
