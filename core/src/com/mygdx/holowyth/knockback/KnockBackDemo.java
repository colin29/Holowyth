package com.mygdx.holowyth.knockback;

import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.util.template.DemoScreen;
import com.mygdx.holowyth.util.tools.Timer;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;

public class KnockBackDemo extends DemoScreen {

	private Renderer renderer;
	DebugStore debugStore = new DebugStore();
	DebugStoreUI debugStoreUI = new DebugStoreUI(stage, debugStore);

	KnockBackSimulation knockbackSim = new KnockBackSimulation(debugStore);

	Timer timer = new Timer();

	Color backgroundColor = HoloGL.rbg(179, 221, 166);

	public KnockBackDemo(final Holowyth game) {
		super(game);
		renderer = new Renderer(game, camera, stage);
		renderer.setKnockBackSimulation(knockbackSim);
		renderer.setClearColor(backgroundColor);

		debugStoreUI.populateDebugValueDisplay();
		root.add(debugStoreUI.getDebugInfo());
		root.pack();

		addInitialObjects();
	}

	private void addInitialObjects() {
		knockbackSim.addCircleObject(440, 300).setVelocity(1f, 0.30f);
		knockbackSim.addCircleObject(550, 300);
	}

	@Override
	public void render(float delta) {
		renderer.render(delta);

		debugStoreUI.updateDebugValueDisplay();
		ifTimeElapsedTickSimulation();
	}

	private void ifTimeElapsedTickSimulation() {
		if (timer.taskReady()) {
			knockbackSim.tick();
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
