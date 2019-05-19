package com.mygdx.holowyth.knockback.collision;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.dataobjects.Segment;
import com.mygdx.holowyth.util.template.BaseRenderer;

public class Renderer extends BaseRenderer {

	World world;
	private final Color CIRCLE_COLOR = Color.RED;

	public Renderer(Holowyth game, Camera worldCamera, Stage stage, World world) {
		super(game, worldCamera, stage);
		this.world = world;
	}

	@Override
	public void render(float delta) {
		super.render(delta);

		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.begin(ShapeType.Filled);

		Segment seg = world.getSegment();
		shapeRenderer.line(seg.x1, seg.y1, seg.x2, seg.y2);
		shapeRenderer.end();

		for (Circle circle : world.getCircles()) {
			renderCircleOutline(circle.x, circle.y, circle.getRadius(), CIRCLE_COLOR);
		}

	}

	private void renderCircleOutline(float x, float y, float radius, Color color) {
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(color);
		shapeRenderer.circle(x, y, radius);
		shapeRenderer.end();
	}

}
