package com.mygdx.holowyth.collision.linecircledemo;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;
import com.mygdx.holowyth.util.template.BaseHoloRenderer;

class Renderer extends BaseHoloRenderer {

	World world;
	private final Color CIRCLE_COLOR = Color.BLACK;
	private Stage stage;

	public Renderer(Holowyth game, Camera worldCamera, Stage stage, World world) {
		super(game, worldCamera);
		this.world = world;
		this.stage = stage;
	}

	@Override
	public void render(float delta) {
		super.render(delta);

		renderLineSeg(world.getSegment(), Color.BLACK);
		renderLineSeg(world.getInitialToCircleCenter(), Color.BROWN);
		renderLineSeg(world.getInitialToClosestPoint(), Color.RED);

		renderPoint(world.getIntersectPoint(), Color.BLACK, null);

		for (Circle circle : world.getCircles()) {
			renderCircleOutline(circle.x, circle.y, circle.getRadius(), CIRCLE_COLOR);
		}

		stage.draw();

	}

	private void renderLineSeg(Segment seg, Color color) {
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(color);
		shapeRenderer.line(seg.x1, seg.y1, seg.x2, seg.y2);
		shapeRenderer.end();
	}

	private void renderCircleOutline(float x, float y, float radius, Color color) {
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(color);
		shapeRenderer.circle(x, y, radius);
		shapeRenderer.end();
	}

	private final static float pointSize = 3f;

	/**
	 * @param outLineColor
	 *            use null for no outline.
	 */
	public void renderPoint(Point point, Color color, Color outLineColor) {
		renderPoint(point.x, point.y, color, outLineColor);
	}

	/**
	 * @param outLineColor
	 *            use null for no outline.
	 */
	public void renderPoint(float x, float y, Color color, Color outLineColor) {
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(color);
		shapeRenderer.circle(x, y, pointSize);
		shapeRenderer.end();

		if (outLineColor != null) {
			shapeRenderer.setColor(outLineColor);
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.circle(x, y, pointSize);
			shapeRenderer.end();
		}

	}

}
