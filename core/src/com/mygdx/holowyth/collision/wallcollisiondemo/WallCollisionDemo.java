package com.mygdx.holowyth.collision.wallcollisiondemo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.MiscUtil;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.template.DemoScreen;
import com.mygdx.holowyth.util.template.adapters.InputProcessorAdapter;

public class WallCollisionDemo extends DemoScreen {

	// Components
	Renderer renderer;
	WallCollisionSimulation simulation = new WallCollisionSimulation();

	Color backgroundColor = HoloGL.rgb(179, 221, 166);

	InputMultiplexer multiplexer = new InputMultiplexer();

	public WallCollisionDemo(Holowyth game) {
		super(game);
		renderer = new Renderer(game, camera);
		renderer.setSimulation(simulation);
		renderer.setClearColor(backgroundColor);

		loadMapFromDisk(Holo.mapsDirectory + Holo.editorInitialMap);

		multiplexer.addProcessor(myInputProcessor);

		Gdx.graphics.setTitle("Wall Collision Demo");
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(multiplexer);
	}

	@Override
	protected void mapStartup() {
		simulation.setObstaclePolygons(map.polys);
	}

	@Override
	protected void mapShutdown() {
	}

	@Override
	public void render(float delta) {
		renderer.render(delta);

	}

	InputProcessor myInputProcessor = new InputProcessorAdapter() {
		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			setMotionSegmentEndToMousePos();
			return false;
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			if (button == Input.Buttons.RIGHT && pointer == 0) {
				setMotionSegmentStartToMousePos();
			}
			return false;
		}

	};

	private void setMotionSegmentStartToMousePos() {
		Point mousePos = MiscUtil.getCursorInWorldCoords(camera);
		simulation.setMotionStart(mousePos.x, mousePos.y);
	}

	private void setMotionSegmentEndToMousePos() {
		Point mousePos = MiscUtil.getCursorInWorldCoords(camera);
		simulation.setMotionEnd(mousePos.x, mousePos.y);
	}

}
