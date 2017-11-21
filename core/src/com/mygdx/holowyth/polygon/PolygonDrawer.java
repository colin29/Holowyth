package com.mygdx.holowyth.polygon;

import java.util.ArrayList;
import java.util.Arrays;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.holowyth.util.data.Point;
import com.mygdx.holowyth.util.data.Segment;

//Called by a parent screen to allow the user to draw Polygons for other use, before returning to previous Screen
public class PolygonDrawer implements InputProcessor {
	
	public Camera parentCamera;

	// Polygons
	public Polygons polys;

	public PolygonDrawer(Camera parentCamera) {
		//this.polys = polys;
		
		this.parentCamera = parentCamera;
	}

	/* vvvvvvv User Methods vvvvvvv */
	
	/**
	 * Renders the polygonDrawer (partial polygons only). Drawing is done as is using world coordinates, so usually you'd want to set the batch matrix
	 * to the main camera
	 * @param shapeRenderer shapeRenderer to use. Batch shouldn't be active when calling.
	 */
	public void render(ShapeRenderer shapeRenderer) {
		shapeRenderer.setColor(0, 0, 0, 1);

//		shapeRenderer.begin(ShapeType.Line);
//		for (Polygon p : polys) {
//			shapeRenderer.polygon(p.vertexes, 0, p.count);
//		}
//		shapeRenderer.end();

		
		shapeRenderer.begin(ShapeType.Line);
		for (Segment s : segs) { // Draw in progress line-segments
			shapeRenderer.line(s.sx, s.sy, s.dx, s.dy);
		}
		shapeRenderer.end();

		shapeRenderer.begin(ShapeType.Filled);
		for (Point p : points) {
			shapeRenderer.circle(p.x, p.y, 2);
		}
		shapeRenderer.end();

	}

	ArrayList<Segment> segs = new ArrayList<Segment>();
	ArrayList<Point> points = new ArrayList<Point>();
	Vector3 vec = new Vector3();
	float completionLeniency = 20;

	float[] vertexes = new float[100];
	int vertexCount = 0;

	private void addVertex(float x, float y) {

		// If new vertex is close to the initial point, finish the polygon
		// without adding anymore points
		if (vertexCount >= 4) {
			float ix = vertexes[0];
			float iy = vertexes[1];
			float dx = x;
			float dy = y;
			float distToFirst = (float) Math.sqrt((dx - ix) * (dx - ix) + (dy - iy) * (dy - iy));
			if (distToFirst < completionLeniency) {
				completePolygon();
				return;
			}
		}

		vertexes[vertexCount] = x;
		vertexes[vertexCount + 1] = y;
		vertexCount += 2;

		if (vertexCount >= 4) {
			Segment s = new Segment(0, 0, 0, 0);
			s.sx = vertexes[vertexCount - 4];
			s.sy = vertexes[vertexCount - 3];
			s.dx = vertexes[vertexCount - 2];
			s.dy = vertexes[vertexCount - 1];
			segs.add(s);
		}
		points.add(new Point(x, y));
	}

	protected void completePolygon() {
		if (vertexCount >= 6) {
			Polygon p = new Polygon(vertexes, vertexCount);
			polys.add(p);
		} else {
			System.out.println("Less than 3 edges in polygon");
		}
		vertexCount = 0;
		Arrays.fill(vertexes, 0);
		segs.clear();
		points.clear();
	}
	
	public void clearPartiallyDrawnPolygons(){
		vertexCount = 0;
		Arrays.fill(vertexes, 0);
		segs.clear();
		points.clear();
	}

	/* ^^^^^^ End of User Methods ^^^^^^ */

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.SPACE) {
			completePolygon();
			return true;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		System.out.println(screenX + " " + screenY);
		if (button == Input.Buttons.LEFT && pointer == 0) {
			vec = parentCamera.unproject(vec.set(screenX, screenY, 0));
			
			System.out.println(vec.x + " " + vec.y);
			addVertex(vec.x, vec.y);
			return true;
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
