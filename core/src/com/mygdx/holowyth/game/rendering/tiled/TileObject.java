package com.mygdx.holowyth.game.rendering.tiled;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class TileObject {
	
	public final int tileHeight;
	private final int mapHeight;
	
	@SuppressWarnings("null")
	static Logger logger = LoggerFactory.getLogger(TileObject.class);
	
	public final int id = getNextId();
	public int baseYIndex = 0;

	
	
	public TileObject(int tileHeight, int mapHeight) {
		this.tileHeight = tileHeight;
		this.mapHeight = mapHeight;
	}
	
	public final List<@NonNull YSortedCell> cells = new ArrayList<>();
	
	public void add(YSortedCell cell) {
		cells.add(cell);
	}
	public boolean remove(YSortedCell cell) {
		return cells.remove(cell);
	}
	
	private static int nextId = 0;
	private static int getNextId() {
		logger.debug("Tile object created id= {}", nextId);
		return nextId++;
	}
	public float getBaseY() {
		return baseYIndex * tileHeight + tileHeight * 0.5f;
	}
	public void calculateBaseYIndex() {
		int yIndexMin = mapHeight;
		for(var cell : cells) {
			yIndexMin = Math.min(yIndexMin, cell.yIndex);
		}
		baseYIndex = yIndexMin;
	}
}
