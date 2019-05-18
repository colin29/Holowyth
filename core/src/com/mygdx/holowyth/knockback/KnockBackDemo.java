package com.mygdx.holowyth.knockback;

import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.util.template.DemoScreen;
import com.mygdx.holowyth.util.tools.Timer;

public class KnockBackDemo extends DemoScreen {

	KnockBackSimulation knockbackSimulation = new KnockBackSimulation();
	Timer timer = new Timer();
	private Renderer renderer;

	Color backgroundColor = HoloGL.rbg(179, 221, 166);

	public KnockBackDemo(final Holowyth game) {
		super(game);
		renderer = new Renderer(game, camera, stage);
		renderer.setKnockBackSimulation(knockbackSimulation);
		renderer.setClearColor(backgroundColor);

		knockbackSimulation.addInitialObjects();
	}

	@Override
	public void render(float delta) {
		renderer.render(delta);

		ifTimeElapsedTickSimulation();
	}

	private void ifTimeElapsedTickSimulation() {
		if (timer.taskReady()) {
			knockbackSimulation.tick();
		}
	}

	@Override
	protected void mapShutdown() {
	}

	@Override
	public void show() {
		timer.start(1000 / 60);
	}

}
