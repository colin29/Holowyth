package com.mygdx.holowyth.collision.collisiondemo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.util.template.SimpleMapDemoScreen;
import com.mygdx.holowyth.util.tools.TaskTimer;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;

public class CollisionDemo extends SimpleMapDemoScreen {

	private DebugStore debugStore = new DebugStore();

	TaskTimer timer = new TaskTimer();
	Color backgroundColor = HoloGL.rgb(179, 221, 166);
	InputMultiplexer multiplexer = new InputMultiplexer();

	// Components
	private Renderer renderer;
	private CollisionDemoUI knockBackDemoUI;
	private CollisionSimulation knockbackSim = new CollisionSimulation(debugStore);

	public CollisionDemo(final Holowyth game) {
		super(game);
		renderer = new Renderer(game, camera, stage);
		renderer.setKnockBackSimulation(knockbackSim);
		renderer.setClearColor(backgroundColor);

		knockBackDemoUI = new CollisionDemoUI(stage, debugStore, game.skin, camera, knockbackSim);

		addInitialObjects();

		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(knockBackDemoUI.getInputProcessor());

	}

	private void addInitialObjects() {
		knockbackSim.addCircleObject(440, 300).setVelocity(1f, 0.30f);
		knockbackSim.addCircleObject(550, 300);
	}

	@Override
	public void render(float delta) {
		renderer.render(delta);

		knockBackDemoUI.onRender();
		ifTimeElapsedTickSimulation();
	}

	private void ifTimeElapsedTickSimulation() {
		if (timer.taskReady()) {
			knockbackSim.tick();
		}
	}

	@Override
	protected void mapStartup() {
	}

	@Override
	protected void mapShutdown() {
	}

	@Override
	public void show() {
		timer.start(1000 / 60);
		Gdx.input.setInputProcessor(multiplexer);
	}

}
