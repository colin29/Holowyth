package com.mygdx.holowyth.game.rendering.tiled;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;

@NonNullByDefault
public class YSortedCell {
	
	@SuppressWarnings("null")
	static Logger logger = LoggerFactory.getLogger(YSortedCell.class);
	
	public int baseYIndex;
	public final int xIndex, yIndex;
	public final TiledMapTileLayer layer;
	
	//extra info
	public final int tileWidth, tileHeight;
	
	@Nullable
	private TileObject parent;
	
	public YSortedCell(int xIndex, int yIndex, TiledMapTileLayer layer) {
		this.xIndex = xIndex;
		this.yIndex = yIndex;
		this.baseYIndex = yIndex;
		this.layer = layer;
		
		tileWidth = layer.getTileWidth();
		tileHeight = layer.getTileHeight();
		
		
		// TODO: confirm underlying cell is not null for sanity
	}
	
	@SuppressWarnings("null")
	public Cell getCell() {
		return layer.getCell(xIndex, yIndex);
	}
	
	/**
	 * The center X position
	 */
	public float getX() {
		return xIndex * tileWidth + tileWidth/2;
	}
	public float getY() {
		return yIndex * tileHeight + tileHeight/2;
	}

	public @Nullable TileObject getParent() {
		return parent;
	}

	@SuppressWarnings("null")
	public void setParent(TileObject newParent) {
		if(parent!=null) {
			logger.info("Re-setting the parent of ysorted cell is unusual, but okay");
			parent.cells.remove(this);
		}
		parent = newParent;
		newParent.add(this);
	}
}
