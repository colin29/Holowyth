package com.mygdx.holowyth.combatDemo.rendering;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.mygdx.holowyth.tiled.MyAtlasTmxMapLoader;

public class TiledMapRenderer extends SubRenderer {

	public TiledMapRenderer(Renderer renderer) {
		super(renderer);
		loadMap();
	}

	private TiledMap map;
	private OrthogonalTiledMapRenderer tiledMapRenderer;

	public void loadMap() {
		map = new MyAtlasTmxMapLoader().load("assets/maps/forest1.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(map);
	}

	public void render() {
		tiledMapRenderer.setView((OrthographicCamera) worldCamera);
		tiledMapRenderer.render();
	}
}
