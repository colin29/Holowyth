package com.mygdx.holowyth.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.polygon.Polygons;
import com.mygdx.holowyth.util.data.Point;
import com.mygdx.holowyth.util.data.Segment;

/**
 * Utility class with various rendering/visualization functions.
 *
 */
public class HoloGL {

	public static void renderSegment(Segment s, ShapeRenderer shapeRenderer, Color color) {
		if (s != null) {
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(color);
			shapeRenderer.rectLine(s.sx, s.sy, s.dx, s.dy, 1.5f);
			shapeRenderer.end();
		}
	}

	public static void renderPolygons(Polygons polys, ShapeRenderer shapeRenderer, Color color) {
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(color);
		for (Polygon p : polys) {
			shapeRenderer.polygon(p.floats, 0, p.count);
		}
		shapeRenderer.end();
	}

	/**
	 * Make sure to set the renderer's matrixes before calling these and similar methods.
	 */
	public static void renderPolygons(Polygons polys, ShapeRenderer shapeRenderer) {
		shapeRenderer.begin(ShapeType.Line);
		for (Polygon p : polys) {
			shapeRenderer.polygon(p.floats, 0, p.count);
		}
		shapeRenderer.end();
	}

	public static void renderPolygon(Polygon poly, ShapeRenderer shapeRenderer, Color color) {
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(color);
		shapeRenderer.polygon(poly.floats, 0, poly.count);
		shapeRenderer.end();
	}

	public static void renderMapBoundaries(Field map, ShapeRenderer shapeRenderer) {
		shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1);
		shapeRenderer.begin(ShapeType.Line);

		Vector2 topRight = new Vector2(map.width(), map.height());
		Vector2 topLeft = new Vector2(0, map.height());
		Vector2 botRight = new Vector2(map.width(), 0);
		Vector2 botLeft = new Vector2(0, 0);
		shapeRenderer.line(topLeft, topRight);
		shapeRenderer.line(botLeft, botRight);
		shapeRenderer.line(topLeft, botLeft);
		shapeRenderer.line(topRight, botRight);

		shapeRenderer.end();
	}

	/**
	 * Often used function for debugging purposes.
	 */
	public static void renderCircle(float x, float y, ShapeRenderer shapeRenderer, Color color) {
		
		float pointSize = 3f;
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(color);
		shapeRenderer.circle(x, y, pointSize);
		shapeRenderer.end();

		//Draw outline
		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.circle(x, y, pointSize);
		shapeRenderer.end();
	}
	
	public static void renderCircleOutline(float x, float y, float radius, ShapeRenderer shapeRenderer, Color color) {
		
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(color);
		shapeRenderer.circle(x, y, radius);
		shapeRenderer.end();
	}

}
