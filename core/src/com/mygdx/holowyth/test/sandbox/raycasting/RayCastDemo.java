package com.mygdx.holowyth.test.sandbox.raycasting;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.HoloGL;
import com.mygdx.holowyth.util.data.Segment;
import com.mygdx.holowyth.util.template.DemoScreen;

public class RayCastDemo extends DemoScreen {

	public RayCastDemo(Holowyth game) {
		super(game);
		Gdx.input.setInputProcessor(this);

		// Set up draggable points
		draggablePoints.addAll(DraggablePoint.getDraggablePointsFrom(unitMotion));
		draggablePoints.addAll(DraggablePoint.getDraggablePointsFrom(wallSegment));
	}

	Color clearColor = HoloGL.rbg(255, 236, 179); // HoloGL.rbg(79, 121, 66);

	public final float pointSelectLeniency = 10;

	Segment unitMotion = new Segment(200, 200, 300, 300);
	Segment wallSegment = new Segment(400, 400, 300, 400);

	ArrayList<DraggablePoint> draggablePoints = new ArrayList<>();

	DraggablePoint draggedPoint = null;

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		Gdx.gl.glEnable(GL20.GL_BLEND);

		// Render an arrow for unit motion, and a segment for line segment.

		HoloGL.renderSegment(wallSegment, Color.RED, true);
		HoloGL.renderArrow(unitMotion.startPoint(), unitMotion.endPoint(), Color.GREEN);
	}

	@Override
	public void show() {
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
