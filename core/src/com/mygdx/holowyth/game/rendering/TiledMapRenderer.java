package com.mygdx.holowyth.game.rendering;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.mygdx.holowyth.game.rendering.tiled.YSortedCell;
import com.mygdx.holowyth.game.rendering.tiled.YSortingTiledMapRenderer;

public class TiledMapRenderer extends SubRenderer {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private YSortingTiledMapRenderer mapRenderer;

	TiledMap map;

	public TiledMapRenderer(GameScreenRenderer renderer) {
		super(renderer);
	}

	public List<YSortedCell> getYSortedCells(){
		return mapRenderer.getYSortedTiles();
	}
	
	public void renderBaseLayers() {
		if (map != null) {
			mapRenderer.setView((OrthographicCamera) worldCamera);
			mapRenderer.renderBaseLayers();
		}
	}
	public void renderTreeLayersWithYIndex(int yIndex) {
		mapRenderer.renderTreeTilesWithYIndex(yIndex);
	}
	public void renderYSortedTile(int xIndex, int yIndex, TiledMapTileLayer layer) {
		mapRenderer.renderTreeTile(xIndex, yIndex, layer);
	}

	public void setMap(TiledMap map) {
		this.map = map;
		if (map != null) {
			mapRenderer = new YSortingTiledMapRenderer(map);
		}
	}

	public boolean isMapLoaded() {
		return map != null;
	}
}
