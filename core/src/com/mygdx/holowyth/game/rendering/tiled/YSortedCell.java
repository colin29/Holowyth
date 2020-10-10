package com.mygdx.holowyth.game.rendering.tiled;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;

@NonNullByDefault
public class YSortedCell {
	public int baseYIndex;
	public int xIndex, yIndex;
	public final TiledMapTileLayer layer;
	
	public YSortedCell(int xIndex, int yIndex, TiledMapTileLayer layer) {
		this.xIndex = xIndex;
		this.yIndex = yIndex;
		this.baseYIndex = yIndex;
		this.layer = layer;
		
		// TODO: confirm underlying cell is not null for sanity
	}
	
	@SuppressWarnings("null")
	public Cell getCell() {
		return layer.getCell(xIndex, yIndex);
	}
}
