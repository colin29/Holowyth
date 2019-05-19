package com.mygdx.holowyth.knockback.collision;

import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.util.template.BaseScreen;

public class LineCircleDemo extends BaseScreen {

	Renderer renderer;

	private final Color PALE_GREEN = HoloGL.rbg(229, 255, 216);

	public LineCircleDemo(final Holowyth game) {
		super(game);

		renderer = new Renderer(game, camera, stage);
		renderer.setClearColor(PALE_GREEN);
	}

	@Override
	public void render(float delta) {
		renderer.render(delta);
	}

}
