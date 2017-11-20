package com.mygdx.holowyth.pathfinding;

import java.awt.geom.Line2D;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.util.data.Point;

/**
 * Contains helper functions related to pathfinding and geometry
 *
 */
public class HoloPF {

	public static boolean isEdgePathable(float x, float y, float x2, float y2, ArrayList<Polygon> polys) {
		boolean intersects = false;
		for (Polygon polygon : polys) {
			for (int i = 0; i <= polygon.count - 2; i += 2) { // for each polygon edge
				if (Line2D.linesIntersect(x, y, x2, y2, polygon.vertexes[i], polygon.vertexes[i + 1],
						polygon.vertexes[(i + 2) % polygon.count], polygon.vertexes[(i + 3) % polygon.count])) {
					intersects = true;
				}
			}
		}
		return !intersects;
	}
	
	public static void renderPath(Path path, Color color, boolean renderPoints, float thickness,  ShapeRenderer shapeRenderer) {
		if (path != null) {
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(color);
			for (int i = 0; i < path.size() - 1; i++) {

				if (i == 1) {
					shapeRenderer.setColor(color);
				}

				if (i == path.size() - 2) {
					shapeRenderer.setColor(color);
				}
				Point v = path.get(i);
				Point next = path.get(i + 1);
				
				shapeRenderer.rectLine(v.x, v.y, next.x, next.y, thickness);

			}
			shapeRenderer.end();

			// Draw points
			float pointSize = 4f;
			shapeRenderer.setColor(Color.GREEN);
			shapeRenderer.begin(ShapeType.Filled);
			if (renderPoints) {
				for (Point p : path) {

					shapeRenderer.circle(p.x, p.y, pointSize);

				}
			}
			shapeRenderer.end();
			shapeRenderer.setColor(Color.BLACK);
			shapeRenderer.begin(ShapeType.Line);
			if (renderPoints) {
				for (Point p : path) {

					shapeRenderer.circle(p.x, p.y, pointSize);

				}
			}
			shapeRenderer.end();

		}
	}

}
