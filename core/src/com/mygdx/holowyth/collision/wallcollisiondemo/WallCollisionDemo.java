package com.mygdx.holowyth.collision.wallcollisiondemo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.template.DemoScreen;

public class WallCollisionDemo extends DemoScreen {

	// Components
	Renderer renderer;
	WallCollisionSimulation simulation = new WallCollisionSimulation();

	Color backgroundColor = HoloGL.rgb(179, 221, 166);

	public WallCollisionDemo(Holowyth game) {
		super(game);
		renderer = new Renderer(game, camera);
		renderer.setSimulation(simulation);
		renderer.setClearColor(backgroundColor);

		loadMapFromDisk(Holo.mapsDirectory + Holo.editorInitialMap);

		Gdx.graphics.setTitle("Wall Collision Demo");
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

}
