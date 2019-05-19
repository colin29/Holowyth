package com.mygdx.holowyth.knockback.collision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;

public class LineCircleDemo extends BaseScreen {

	Renderer renderer;

	private final Color PALE_GREEN = HoloGL.rbg(229, 255, 216);
	private final Color backgroundColor = Color.WHITE;

	private final float CIRCLE_RADIUS = 100;

	// Input
	private InputMultiplexer multiplexer = new InputMultiplexer();

	DebugStore debugStore = new DebugStore();
	DebugStoreUI debugStoreUI;

	World world = new World(debugStore);

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public LineCircleDemo(final Holowyth game) {
		super(game);

		renderer = new Renderer(game, camera, stage, world);
		renderer.setClearColor(backgroundColor);

		multiplexer.addProcessor(myInputProcessor);

		Circle keyCircle = new Circle(300, 500, CIRCLE_RADIUS);
		world.getCircles().add(keyCircle);
		world.setKeyCircle(keyCircle);

		world.setSegment(100, 400, 200, 200);

		debugStoreUI = new DebugStoreUI(stage, debugStore);
		debugStoreUI.populateDebugValueDisplay();

		root.add(debugStoreUI.getDebugInfo());

		root.pack();

	}

	@Override
	public void render(float delta) {
		renderer.render(delta);

		debugStoreUI.updateDebugValueDisplay();
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
