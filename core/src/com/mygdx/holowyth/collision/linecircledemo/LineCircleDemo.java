package com.mygdx.holowyth.collision.linecircledemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.MiscUtil;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.dataobjects.Segment;
import com.mygdx.holowyth.util.template.HoloBaseScreen;
import com.mygdx.holowyth.util.template.adapters.InputProcessorAdapter;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;
import com.mygdx.holowyth.util.tools.debugstore.DebugStoreUI;

public class LineCircleDemo extends HoloBaseScreen {

	Renderer renderer;

	private final Color backgroundColor = Color.WHITE;

	private final float CIRCLE_RADIUS = 100;

	// Input
	private InputMultiplexer multiplexer = new InputMultiplexer();

	DebugStore debugStore = new DebugStore();
	DebugStoreUI debugStoreUI = new DebugStoreUI(debugStore);

	World mapInstance = new World(debugStore);

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public LineCircleDemo(final Holowyth game) {
		super(game);

		renderer = new Renderer(game, camera, stage, mapInstance);
		renderer.setClearColor(backgroundColor);

		multiplexer.addProcessor(myInputProcessor);

		Circle keyCircle = new Circle(300, 500, CIRCLE_RADIUS);
		mapInstance.getCircles().add(keyCircle);
		mapInstance.setKeyCircle(keyCircle);

		mapInstance.setSegment(100, 400, 200, 200);

		debugStoreUI.populateDebugValueDisplay();

		root.add(debugStoreUI.getDebugValuesTable());
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
		Segment current = mapInstance.getSegment();
		mapInstance.setSegment(current.x1, current.y1, mousePos.x, mousePos.y);
	}

}
