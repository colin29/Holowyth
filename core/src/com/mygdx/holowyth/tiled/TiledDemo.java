package com.mygdx.holowyth.tiled;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.ShapeDrawerPlus;
import com.mygdx.holowyth.util.dataobjects.Segment;
import com.mygdx.holowyth.util.template.HoloBaseScreen;

public class TiledDemo extends HoloBaseScreen implements InputProcessor {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private OrthogonalTiledMapRenderer tiledMapRenderer;
	private TiledMap map;

	private ShapeDrawerPlus shapeDrawer;

	public TiledDemo(Holowyth game) {
		super(game);
		loadMap();
		Gdx.input.setInputProcessor(this);
		shapeDrawer = game.shapeDrawer;
	}

	public void loadMap() {
		map = new MyAtlasTmxMapLoader().load("assets/maps/forest1.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(map);

		readCollisionObjectsFromMap();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		handleMousePanning(delta);

		camera.update();
		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();

		renderSegs();

	}

	private void renderSegs() {
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		for (Segment seg : segs) {
			shapeDrawer.line(seg.x1, seg.y1, seg.x2, seg.y2);
		}
		batch.end();

	}

	List<Segment> segs = new ArrayList<Segment>();

	private void readCollisionObjectsFromMap() {

		var collisionLayer = map.getLayers().get("Collision");

		MapObjects objects = collisionLayer.getObjects();

		// there are several other types, Rectangle is probably the most common one
		for (PolylineMapObject object : objects.getByType(PolylineMapObject.class)) {

			Polyline polyline = object.getPolyline();
			float[] vertices = polyline.getTransformedVertices();

			Vector2 end = new Vector2(vertices[0], vertices[1]);
			Vector2 start = new Vector2();

			// i represent the current line segment (for example with 6 floats, there are 3 points, and 2 total line segments)
			for (int i = 1; i < vertices.length / 2; i += 1) {
				start.set(end);
				end.set(vertices[i * 2], vertices[i * 2 + 1]);

				segs.add(new Segment(start.x, start.y, end.x, end.y));
			}

		}

	}

	private float snapLeftoverX;
	private float snapLeftoverY;

	/**
	 * Pan the view if the mouse is near the edge of the screen
	 */
	private void handleMousePanning(float delta) {

		final int mouseX = Gdx.input.getX();
		final int mouseY = Gdx.input.getY();

		final int screenHeight = Gdx.graphics.getHeight();
		final int screenWidth = Gdx.graphics.getWidth();

		final float scrollMargin = 40f;
		final float scrollSpeed = 300 * delta; // do X pixels per second

		if (mouseY > screenHeight - scrollMargin)
			camera.translate(0, -scrollSpeed + snapLeftoverY);
		if (mouseY < scrollMargin)
			camera.translate(0, scrollSpeed + snapLeftoverY);

		if (mouseX > screenWidth - scrollMargin)
			camera.translate(scrollSpeed + snapLeftoverX, 0);
		if (mouseX < scrollMargin)
			camera.translate(-scrollSpeed + snapLeftoverX, 0);

		snapLeftoverX = 0;
		snapLeftoverY = 0;

		snapCameraAndSaveRemainder();
	}

	/*
	 * Accumulate the leftovers and apply them to later movement, in order to prevent slow-down or inconsistencies due to repeated rounding.
	 */
	private void snapCameraAndSaveRemainder() {

		float dx = Math.round(camera.position.x) - camera.position.x;
		float dy = Math.round(camera.position.y) - camera.position.y;

		camera.position.set(Math.round(camera.position.x), Math.round(camera.position.y), 0);

		snapLeftoverX = -1 * dx;
		snapLeftoverY = -1 * dy;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
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

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

}
