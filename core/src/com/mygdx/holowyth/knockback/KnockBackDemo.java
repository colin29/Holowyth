package com.mygdx.holowyth.knockback;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.util.template.DemoScreen;
import com.mygdx.holowyth.util.tools.Timer;

public class KnockBackDemo extends DemoScreen {

	KnockBackSimulation knockbackSimulation = new KnockBackSimulation();
	Timer timer = new Timer();

	public KnockBackDemo(final Holowyth game) {
		super(game);
	}

	Color backgroundColor = HoloGL.rbg(79, 121, 66);

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		Gdx.gl.glEnable(GL20.GL_BLEND);

		camera.update();
		batch.setProjectionMatrix(camera.combined);

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
