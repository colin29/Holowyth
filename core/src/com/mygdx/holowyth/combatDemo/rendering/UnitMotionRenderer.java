package com.mygdx.holowyth.combatDemo.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.dataobjects.Point;

class UnitMotionRenderer extends SubRenderer {

	public UnitMotionRenderer(Renderer renderer) {
		super(renderer);
	}

	@SuppressWarnings("unused")
	void renderPlayerUnreachedWaypoints(Color color) {
		for (Unit unit : getWorld().getUnits()) {
			if (unit.isAPlayerCharacter() && unit.motion.getPath() != null) {
				Path path = unit.motion.getPath();
				for (int i = unit.motion.getWayPointIndex(); i < path.size(); i++) {
					Point waypoint = path.get(i);
					shapeRenderer.begin(ShapeType.Filled);
					shapeRenderer.setColor(color);
					shapeRenderer.circle(waypoint.x, waypoint.y, 4f);
					shapeRenderer.end();

					shapeRenderer.begin(ShapeType.Line);
					shapeRenderer.setColor(Color.BLACK);
					shapeRenderer.circle(waypoint.x, waypoint.y, 4f);
					shapeRenderer.end();
				}
			}
		}
	}

	void renderPlayerVelocityArrow() {
		getWorld().doIfTrueForAllUnits(
				(UnitInfo u) -> (u.isAPlayerCharacter() && u.getMotion().getVelocityMagnitude() > 0.01f),
				(UnitInfo u) -> {
					float scale = 15;
					HoloGL.renderArrow(u.getPos(), u.getMotion().getVelocity().setLength(scale), Color.GREEN);
				});
	}

	void renderUnitDestinations(Color color) {

		for (Unit unit : getWorld().getUnits()) {
			if (unit.isAPlayerCharacter() && unit.motion.getPath() != null) {

				Path path = unit.motion.getPath();
				Point finalPoint = path.get(path.size() - 1);
				shapeRenderer.begin(ShapeType.Filled);
				shapeRenderer.setColor(color);
				shapeRenderer.circle(finalPoint.x, finalPoint.y, 4f);
				shapeRenderer.end();

				shapeRenderer.begin(ShapeType.Line);
				shapeRenderer.setColor(Color.BLACK);
				shapeRenderer.circle(finalPoint.x, finalPoint.y, 4f);
				shapeRenderer.end();
			}

		}
	}

}
