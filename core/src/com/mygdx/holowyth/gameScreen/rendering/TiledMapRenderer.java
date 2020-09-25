package com.mygdx.holowyth.gameScreen.rendering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class TiledMapRenderer extends SubRenderer {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private OrthogonalTiledMapRenderer tiledMapRenderer;

	TiledMap map;

	public TiledMapRenderer(GameScreenRenderer renderer) {
		super(renderer);
	}

	public void renderMap() {
		if (map != null) {
			tiledMapRenderer.setView((OrthographicCamera) worldCamera);
			tiledMapRenderer.render();
		}
	}

	public void setMap(TiledMap map) {
		this.map = map;
		if (map != null) {
			tiledMapRenderer = new OrthogonalTiledMapRenderer(map);
			logger.debug("Set new map.");
		}
	}

	public boolean isMapLoaded() {
		return map != null;
	}
}
