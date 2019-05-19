package com.mygdx.holowyth.knockback.collision;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.template.BaseRenderer;

public class Renderer extends BaseRenderer {

	public Renderer(Holowyth game, Camera worldCamera, Stage stage) {
		super(game, worldCamera, stage);
	}

	@Override
	public void render(float delta) {
		super.render(delta);

		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.begin(ShapeType.Filled);
		// shapeRenderer.rect
		shapeRenderer.line(0, 0, 400, 450);
		shapeRenderer.end();

		renderCircleOutline(200, 300, 50, Color.BLACK);

	}

	private void renderCircleOutline(float x, float y, float radius, Color color) {
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(color);
		shapeRenderer.circle(x, y, radius);
		shapeRenderer.end();
	}

}
