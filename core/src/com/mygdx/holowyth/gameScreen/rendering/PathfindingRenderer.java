package com.mygdx.holowyth.gameScreen.rendering;

import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.pathfinding.HoloPF;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.Holo;

class PathfindingRenderer extends SubRenderer {

	private PathingModule pathingModule;
	private float pathThickness = 2f;

	public PathfindingRenderer(Renderer renderer, PathingModule pathingModule) {
		super(renderer);
		this.pathingModule = pathingModule;
	}

	void renderUnitExpandedHitBodies() {
		getWorld().doForAllUnits(
				(UnitInfo u) -> {
					HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + Holo.UNIT_RADIUS, Color.GRAY);
				});

	}

	@SuppressWarnings("unused")
	void renderPaths(boolean renderIntermediatePaths) {
		if (renderIntermediatePaths) {
			pathingModule.renderIntermediateAndFinalPaths(getWorld().getUnits());
		} else {
			for (Unit unit : getWorld().getUnits()) {
				if (unit.getSide().isEnemy() && !Holo.debugRenderEnemyPath)
					continue;
				if (unit.motion.getPath() != null) {
					renderPath(unit.motion.getPath(), Color.GRAY, false);
				}
			}
		}

		for (Unit u : getWorld().getUnits()) {
			u.motion.renderNextWayPoint();
		}

	}

	private void renderPath(Path path, Color color, boolean renderPoints) {
		HoloPF.renderPath(path, color, renderPoints, pathThickness, shapeRenderer);
	}

}
