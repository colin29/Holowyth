package com.mygdx.holowyth.game.rendering.tiled;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class TileObject {
	
	private final int tileHeight, tileWidth;
	private final int mapHeight;
	
	@SuppressWarnings("null")
	static Logger logger = LoggerFactory.getLogger(TileObject.class);
	
	public final int id = getNextId();
	public int baseYIndex = 0;

	
	public float opacity = 1;
	private float fadingOpacity = opacity;

	private static float fadeRate = 0.015f;
	
	// bounding-box coordinates
	public float x1, x2, y1, y2;
	
	
	
	public TileObject(int tileWidth, int tileHeight, int mapHeight) {
		this.tileWidth = tileWidth;
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
	
	public void calculateBoundingBox() {
		
		float minX, maxX, minY, maxY;
		if(cells.isEmpty()) {
			logger.info("Can't calculate bounding box, tileObject has no cells");
			return;
		}
		var first = cells.get(0);
		minX = first.xIndex;
		maxX = first.xIndex;
		minY = first.yIndex;
		maxY = first.yIndex;
		
		for(var cell : cells) {
			minX = Math.min(minX, cell.xIndex);
			minY = Math.min(minY, cell.yIndex);
			
			maxX = Math.max(maxX, cell.xIndex);
			maxY = Math.max(maxY, cell.yIndex);
		}
		
		x1 = minX * tileWidth;
		y1 = minY * tileHeight;
		
		x2 = maxX * tileWidth + tileWidth;
		y2 = maxY * tileHeight + tileHeight;
	}
	public void tickFade() {
		if(Math.abs(opacity-fadingOpacity) < fadeRate) {
			fadingOpacity = opacity;
		}else {
			if(fadingOpacity < opacity) {
				fadingOpacity += fadeRate;
			}else {
				fadingOpacity-= fadeRate;
			}
		}
	}
	public float getFadingOpacity() {
		return fadingOpacity;
	}
}
