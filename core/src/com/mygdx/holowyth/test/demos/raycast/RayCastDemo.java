package com.mygdx.holowyth.test.demos.raycast;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;
import com.mygdx.holowyth.util.template.DemoScreen;

public class RayCastDemo extends DemoScreen {

	public RayCastDemo(Holowyth game) {
		super(game);
		Gdx.input.setInputProcessor(this);

		// Set up draggable points
		draggablePoints.addAll(DraggablePoint.getDraggablePointsFrom(unitMotion));
		draggablePoints.addAll(DraggablePoint.getDraggablePointsFrom(wallSegment));
	}

	Color clearColor = HoloGL.rbg(200, 180, 120); // HoloGL.rbg(79, 121, 66);

	public final float pointSelectLeniency = 10;

	Segment unitMotion = new Segment(200, 200, 300, 300);
	Segment wallSegment = new Segment(400, 400, 300, 400);

	ArrayList<DraggablePoint> draggablePoints = new ArrayList<>();

	DraggablePoint draggedPoint = null;

	Point collision = new Point(200, 600);

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		Gdx.gl.glEnable(GL20.GL_BLEND);

		// Render an arrow for unit motion, and a segment for line segment.

		HoloGL.renderSegment(wallSegment, Color.RED, true);
		HoloGL.renderArrow(unitMotion.startPoint(), unitMotion.endPoint(), Color.GREEN);

		calcCollision();
		if (collision != null) {
			HoloGL.renderCircleOutline(collision.x, collision.y, 6, Color.CYAN);
		}
	}

	private void calcCollision() {
		collision = lineSegsIntersect(unitMotion, wallSegment);
	}

	public Point lineSegsIntersect(Segment s1, Segment s2) {

		float a1, a2, b1, b2, c1, c2;

		a1 = s1.y2 - s1.y1;
		b1 = s1.x1 - s1.x2;
		c1 = a1 * s1.x1 + b1 * s1.y1;

		a2 = s2.y2 - s2.y1;
		b2 = s2.x1 - s2.x2;
		c2 = a2 * s2.x1 + b2 * s2.y1;

		float det = a1 * b2 - a2 * b1;
		float x, y;
		if (det == 0) {
		} else {
			x = (b2 * c1 - b1 * c2) / det;
			y = (a1 * c2 - a2 * c1) / det;

			// Check if the point is on both line segments
			float EPS = 0.001f; // tolerance (From brief testing I saw roundings errors of 0.000031 for x = 400, 30
								// times less than this. However rounding errors are proportional to size of x, thus
								// the large safety factor)

			// System.out.printf("%f, %f, %f %n", Math.min(s2.y1, s2.y2) - EPS, y, Math.max(s2.y1, s2.y2) + EPS);

			if (Math.min(s1.x1, s1.x2) - EPS <= x && x <= Math.max(s1.x1, s1.x2) + EPS
					&& Math.min(s1.y1, s1.y2) - EPS <= y && y <= Math.max(s1.y1, s1.y2 + EPS)
					&& Math.min(s2.x1, s2.x2) - EPS <= x && x <= Math.max(s2.x1, s2.x2) + EPS
					&& Math.min(s2.y1, s2.y2) - EPS <= y && y <= Math.max(s2.y1, s2.y2) + EPS) {
				return new Point(x, y);
			}
			return null;
		}
		return null;
	}

	@Override
	public void show() {
	}

	@Override
	protected void mapStartup() {
	}

	@Override
	protected void mapShutdown() {
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (pointer == 0) {
			draggedPoint = null;
		}
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (pointer == 0) {
			Vector3 vec = new Vector3(); // obtain window coordinates of the click.
			vec = fixedCam.unproject(vec.set(screenX, screenY, 0));

			// If click is on a draggable point, select the first one

			for (DraggablePoint point : draggablePoints) {
				Segment dist = new Segment(point.x, point.y, vec.x, vec.y);
				if (dist.getLength() < pointSelectLeniency) {
					draggedPoint = point;
					break;
				}
			}
		}
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (pointer == 0) {
			Vector3 vec = new Vector3(); // obtain window coordinates of the click.
			vec = fixedCam.unproject(vec.set(screenX, screenY, 0));
			if (draggedPoint != null) {
				draggedPoint.dragged(vec.x, vec.y);
			}
		}
		return false;
	}

}
