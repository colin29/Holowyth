package com.mygdx.holowyth.editor.polydrawer;

import java.util.ArrayList;
import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.polygon.Polygons;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;

//Called by a parent screen to allow the user to draw Polygons for other use, before returning to previous Screen
public class PolygonDrawerScreen implements Screen, InputProcessor {

	final Holowyth game;

	// Rendering and pipeline variables
	OrthographicCamera camera;
	ShapeRenderer shapeRenderer;
	SpriteBatch batch;

	// App Fields
	Screen parentScreen;

	// Polygons
	public Polygons polys = new Polygons();

	public PolygonDrawerScreen(final Holowyth game, Screen parentScreen) {
		this.game = game;

		// create a camera for this screen.
		camera = new OrthographicCamera();
		camera.setToOrtho(false, game.resX, game.resY);

		shapeRenderer = game.shapeRenderer;

		batch = game.batch;

		this.parentScreen = parentScreen;
	}

	@Override
	public void render(float delta) {

		// Clear the screen
		Gdx.gl.glClearColor(0.8f, 1f, 0.8f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));

		// Tell the camera to update its matrices.
		camera.update();

		// Tell the SpriteBatch to render in the coordinate system specified by
		// the camera.
		batch.setProjectionMatrix(camera.combined);
		shapeRenderer.setProjectionMatrix(camera.combined);

		renderPolygonDemo();

	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		System.out.println("Entered Polygon Drawer");
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
	}

	/* vvvvvvv User Methods vvvvvvv */

	private void renderPolygonDemo() {
		shapeRenderer.setColor(0, 0, 0, 1);

		shapeRenderer.begin(ShapeType.Line);
		for (Polygon p : polys) {
			shapeRenderer.polygon(p.floats, 0, p.count);
		}
		shapeRenderer.end();

		
		shapeRenderer.begin(ShapeType.Line);
		for (Segment s : segs) { // Draw in progress line-segments
			shapeRenderer.line(s.x1, s.y1, s.x2, s.y2);
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
			s.x1 = vertexes[vertexCount - 4];
			s.y1 = vertexes[vertexCount - 3];
			s.x2 = vertexes[vertexCount - 2];
			s.y2 = vertexes[vertexCount - 1];
			segs.add(s);
		}
		points.add(new Point(x, y));
	}

	private void completePolygon() {
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

	/* ^^^^^^ End of User Methods ^^^^^^ */

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.SPACE) {
			completePolygon();
		}
		if (keycode == Keys.D) {
			game.setScreen(parentScreen);
			System.out.println("Exited Polygon Drawer");
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
			vec = camera.unproject(vec.set(screenX, screenY, 0));
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
