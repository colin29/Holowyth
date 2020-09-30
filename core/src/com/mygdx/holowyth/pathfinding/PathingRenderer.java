package com.mygdx.holowyth.pathfinding;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.mygdx.holowyth.pathfinding.PathSmoother.PathsInfo;
import com.mygdx.holowyth.util.Holo;

public class PathingRenderer {
	
	private final PathingModule model;
	private final ShapeRenderer shapeRenderer;
	
	private final int CELL_SIZE = Holo.CELL_SIZE;
	
	public PathingRenderer(PathingModule model, ShapeRenderer shapeRenderer) {
		this.model = model;
		this.shapeRenderer = shapeRenderer;
	}

	public void renderGraph(boolean renderEdges) {

		if (renderEdges) {
			// Draw Edges
			shapeRenderer.setColor(Color.CORAL);
			shapeRenderer.begin(ShapeType.Line);
			for (int y = 0; y < model.graphHeight; y++) {
				for (int x = 0; x < model.graphWidth; x++) {
					Vertex v = model.graph[y][x];
					if (v.N)
						drawLine(x, y, x, y + 1);
					if (v.S)
						drawLine(x, y, x, y - 1);
					if (v.W)
						drawLine(x, y, x - 1, y);
					if (v.E)
						drawLine(x, y, x + 1, y);

					if (v.NW)
						drawLine(x, y, x - 1, y + 1);
					if (v.NE)
						drawLine(x, y, x + 1, y + 1);
					if (v.SW)
						drawLine(x, y, x - 1, y - 1);
					if (v.SE)
						drawLine(x, y, x + 1, y - 1);
				}
			}
			shapeRenderer.end();
		}

		// Draw vertexes as points
		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.begin(ShapeType.Filled);

		for (int y = 0; y < model.graphHeight; y++) {
			for (int x = 0; x < model.graphWidth; x++) {
				if (model.graph[y][x].reachable) {
					shapeRenderer.circle(x * CELL_SIZE, y * CELL_SIZE, 1f);
				}

			}
		}
		shapeRenderer.end();

	}
	

	public void renderDynamicGraph(boolean renderEdges) {

		if (renderEdges) {
			// Draw Edges
			shapeRenderer.setColor(Color.CORAL);
			shapeRenderer.begin(ShapeType.Line);
			for (int y = 0; y < model.graphHeight; y++) {
				for (int x = 0; x < model.graphWidth; x++) {
					Vertex v = model.dynamicGraph[y][x];
					if (v.N)
						drawLine(x, y, x, y + 1);
					if (v.S)
						drawLine(x, y, x, y - 1);
					if (v.W)
						drawLine(x, y, x - 1, y);
					if (v.E)
						drawLine(x, y, x + 1, y);

					if (v.NW)
						drawLine(x, y, x - 1, y + 1);
					if (v.NE)
						drawLine(x, y, x + 1, y + 1);
					if (v.SW)
						drawLine(x, y, x - 1, y - 1);
					if (v.SE)
						drawLine(x, y, x + 1, y - 1);
				}
			}
			shapeRenderer.end();
		}

		// Draw vertexes as points
		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.begin(ShapeType.Filled);

		for (int y = 0; y < model.graphHeight; y++) {
			for (int x = 0; x < model.graphWidth; x++) {
				if (model.dynamicGraph[y][x].reachable) {
					shapeRenderer.circle(x * CELL_SIZE, y * CELL_SIZE, 1f);
				}

			}
		}
		shapeRenderer.end();

	}
	
	private void drawLine(int ix, int iy, int ix2, int iy2) {
		shapeRenderer.line(ix * CELL_SIZE, iy * CELL_SIZE, 0, ix2 * CELL_SIZE, iy2 * CELL_SIZE, 0);
	}
	
	/**
	 * Render intermediate paths for all units in the list
	 */
	public void renderIntermediateAndFinalPaths(List<@NonNull ? extends UnitPFWithPath> units) {
		for (UnitPFWithPath unit : units) {
			PathsInfo info = model.intermediatePaths.get(unit);
			if (info != null && (unit.getPath() != null || Holo.continueShowingPathAfterArrival)) {
				if (info.finalPath != null) {
					renderPath(info.pathSmoothed0, Color.PINK, false);
					renderPath(info.pathSmoothed1, Color.FIREBRICK, true);
					renderPath(info.finalPath, Color.BLUE, false);
				}
			}
		}
	}


	private float pathThickness = 2f;
	private void renderPath(Path path, Color color, boolean renderPoints) {
		HoloPF.renderPath(path, color, renderPoints, pathThickness, shapeRenderer);
	}
	
}
