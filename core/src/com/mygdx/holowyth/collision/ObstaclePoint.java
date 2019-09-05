package com.mygdx.holowyth.collision;

import com.mygdx.holowyth.util.dataobjects.Point;

public class ObstaclePoint implements CircleCBInfo {

	final Point pos;

	public ObstaclePoint(float x, float y) {
		pos = new Point(x, y);
	}

	@Override
	public float getX() {
		return pos.x;
	}

	@Override
	public float getY() {
		return pos.y;
	}

	@Override
	public float getVx() {
		return 0;

	}

	@Override
	public float getVy() {
		return 0;
	}

	@Override
	public float getRadius() {
		return 0;
	}
}
