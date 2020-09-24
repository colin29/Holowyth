package com.mygdx.holowyth.graphics;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.polygon.Polygons;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;

/**
 * Utility class with various basic rendering/visualization functions. One should set shapeRenderer's projection matrix before calling functions.
 *
 */
public class HoloGL {

	static ShapeRenderer shapeRenderer;

	public static void renderSegment(Segment s, Color color) {
		if (s != null) {
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(color);
			shapeRenderer.rectLine(s.x1, s.y1, s.x2, s.y2, 1.5f);
			shapeRenderer.end();
		}
	}

	public static void renderSegment(Segment s, Color color, boolean renderEndpoints) {
		renderSegment(s, color);
		if (renderEndpoints) {
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(Color.BLACK);
			shapeRenderer.circle(s.x1, s.y1, 2);
			shapeRenderer.circle(s.x2, s.y2, 2);
			shapeRenderer.end();
		}
	}

	public static void renderPolygons(Polygons polys, Color color) {
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
	public static void renderPolygons(List<Polygon> polys) {
		shapeRenderer.begin(ShapeType.Line);
		for (Polygon p : polys) {
			shapeRenderer.polygon(p.floats, 0, p.count);
		}
		shapeRenderer.end();
	}

	public static void renderPoints(List<Point> points, Color color) {
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(color);
		for (var point : points) {
			shapeRenderer.point(point.x, point.y, 0);
		}
		shapeRenderer.end();
	}

	public static <S extends Segment> void renderSegs(List<S> segs, Color color) {
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(color);
		for (S seg : segs) {
			shapeRenderer.line(seg.x1, seg.y1, seg.x2, seg.y2);
		}
		shapeRenderer.end();
	}

	public static void renderPolygon(Polygon poly, Color color) {
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(color);
		shapeRenderer.polygon(poly.floats, 0, poly.count);
		shapeRenderer.end();
	}

	/**
	 * Often used function for debugging purposes.
	 */
	public static void renderCircle(float x, float y, Color color) {

		float pointSize = 3f;
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(color);
		shapeRenderer.circle(x, y, pointSize);
		shapeRenderer.end();

		// Draw outline
		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.circle(x, y, pointSize);
		shapeRenderer.end();
	}

	public static void renderCircleOutline(float x, float y, float radius, Color color) {
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(color);
		shapeRenderer.circle(x, y, radius);
		shapeRenderer.end();
	}

	public static void renderEllipseOutline(float x, float y, float height, float width, Color color) {
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(color);
		shapeRenderer.ellipse(x, y, height, width, 0f);
		shapeRenderer.end();
	}

	public static void renderMapBoundaries(Field map) {
		renderMapBoundaries(map.width(), map.height());
	}

	public static void renderMapBoundaries(int mapWidth, int mapHeight) {
		shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1);
		shapeRenderer.begin(ShapeType.Line);

		Vector2 topRight = new Vector2(mapWidth, mapHeight);
		Vector2 topLeft = new Vector2(0, mapHeight);
		Vector2 botRight = new Vector2(mapWidth, 0);
		Vector2 botLeft = new Vector2(0, 0);
		shapeRenderer.line(topLeft, topRight);
		shapeRenderer.line(botLeft, botRight);
		shapeRenderer.line(topLeft, botLeft);
		shapeRenderer.line(topRight, botRight);

		shapeRenderer.end();
	}

	/**
	 * Renders an arrow pointing from unit a to unit b
	 */
	public static void renderArrow(UnitInfo a, UnitInfo b, Color arrowColor) {

		Segment seg = new Segment(a.getPos(), b.getPos());
		float len = seg.getLength();

		float dx = seg.x2 - seg.x1;
		float dy = seg.y2 - seg.y1;

		float newLen = len - b.getRadius() * 0.35f; // have the arrow end somewhere short of the center of unit b
		float ratio = newLen / len;

		float nx = dx * ratio;
		float ny = dy * ratio;

		Point arrowTip = new Point(seg.x1 + nx, seg.y1 + ny);

		renderArrow(a.getPos(), arrowTip, arrowColor);

	}

	public static void renderArrow(Point start, Vector2 vector, Color arrowColor) {
		Point end = new Point(start.x + vector.x, start.y + vector.y);
		renderArrow(start, end, arrowColor);
	}

	public static void renderArrow(Segment seg, Color arrowColor) {
		renderArrow(seg.startPoint(), seg.endPoint(), arrowColor);
	}

	public static void renderArrow(Point start, Point end, Color arrowColor) {

		final float wingLength = 8f;
		final float arrowAngle = 30f;

		Segment s = new Segment(start, end);

		float dx = s.x2 - s.x1;
		float dy = s.y2 - s.y1;

		// Draw the arrow wings

		HoloGL.renderSegment(new Segment(start, end), arrowColor);

		// calculate angle of the main arrow line
		float angle = (float) Math.acos(dx / s.getLength());
		if (dy < 0) {
			angle = (float) (2 * Math.PI - angle);
		}

		float backwardsAngle = (float) (angle + Math.PI);

		// draw a line in the +x direction, then rotate it and transform it as needed.

		Segment wingSeg = new Segment(0, 0, wingLength, 0); // create the base wing segment

		shapeRenderer.identity();
		shapeRenderer.translate(end.x, end.y, 0);
		shapeRenderer.rotate(0, 0, 1, (float) Math.toDegrees(backwardsAngle) + arrowAngle);

		HoloGL.renderSegment(wingSeg, arrowColor);

		shapeRenderer.identity();
		shapeRenderer.translate(end.x, end.y, 0);
		shapeRenderer.rotate(0, 0, 1, (float) Math.toDegrees(backwardsAngle) - arrowAngle);

		HoloGL.renderSegment(wingSeg, arrowColor);
		shapeRenderer.identity();
	}

	/**
	 * Convenience function to make new colors from a RBG tuple
	 * 
	 * @return
	 */
	public static Color rgb(int r, int g, int b) {
		return new Color((float) r / 255, (float) g / 255, (float) b / 255, 1);
	}

	/**
	 * rgb span from 0-255, float from 0-1
	 */
	public static Color rgb(int r, int g, int b, float a) {
		return new Color((float) r / 255, (float) g / 255, (float) b / 255, a);
	}

	public static ShapeRenderer getShapeRenderer() {
		return shapeRenderer;
	}

	public static void setShapeRenderer(ShapeRenderer shapeRenderer) {
		HoloGL.shapeRenderer = shapeRenderer;
	}
}
