package com.mygdx.holowyth.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.util.dataobjects.Point;

class UnitMotionRenderer extends SubRenderer {

	public UnitMotionRenderer(GameScreenRenderer renderer) {
		super(renderer);
	}

	void renderPlayerUnreachedWaypoints(Color color) {
		for (UnitOrderable unit : getMapInstance().getUnits()) {
			if (unit.isAPlayerCharacter() && unit.getMotion().getPath() != null) {
				Path path = unit.getMotion().getPath();
				for (int i = unit.getMotion().getWayPointIndex(); i < path.size(); i++) {
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
		getMapInstance().doIfTrueForAllUnits(
				(UnitInfo u) -> (u.isAPlayerCharacter() && u.getMotion().getVelocityMagnitude() > 0.01f),
				(UnitInfo u) -> {
					float scale = 15;
					HoloGL.renderArrow(u.getPos(), u.getMotion().getVelocity().setLength(scale), Color.GREEN);
				});
	}

	void renderUnitDestinations(Color color) {

		for (UnitOrderable unit : getMapInstance().getUnits()) {
			if (unit.isAPlayerCharacter() && unit.getMotion().getPath() != null) {

				Path path = unit.getMotion().getPath();
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
