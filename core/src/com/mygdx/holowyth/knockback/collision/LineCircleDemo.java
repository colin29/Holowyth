package com.mygdx.holowyth.knockback.collision;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.util.MiscUtil;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;
import com.mygdx.holowyth.util.template.BaseScreen;
import com.mygdx.holowyth.util.template.adapters.InputProcessorAdapter;

public class LineCircleDemo extends BaseScreen {

	Renderer renderer;
	World world = new World();

	private final Color PALE_GREEN = HoloGL.rbg(229, 255, 216);
	private final float CIRCLE_RADIUS = 100;

	// Input
	private InputMultiplexer multiplexer = new InputMultiplexer();

	public LineCircleDemo(final Holowyth game) {
		super(game);

		renderer = new Renderer(game, camera, stage, world);
		renderer.setClearColor(PALE_GREEN);

		multiplexer.addProcessor(myInputProcessor);

		Circle keyCircle = new Circle(300, 500, CIRCLE_RADIUS);
		world.getCircles().add(keyCircle);
		world.setKeyCircle(keyCircle);

		world.setSegment(100, 400, 200, 200);
	}

	@Override
	public void render(float delta) {
		renderer.render(delta);
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(multiplexer);
	}

	InputProcessor myInputProcessor = new InputProcessorAdapter() {
		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			setSegmentEndToMousePos();
			return false;
		}
	};

	private void setSegmentEndToMousePos() {
		Point mousePos = MiscUtil.getCursorInWorldCoords(camera);
		Segment current = world.getSegment();
		world.setSegment(current.x1, current.y1, mousePos.x, mousePos.y);
	}

}
