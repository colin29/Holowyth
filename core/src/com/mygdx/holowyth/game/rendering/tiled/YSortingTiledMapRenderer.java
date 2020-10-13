package com.mygdx.holowyth.game.rendering.tiled;

import static com.badlogic.gdx.graphics.g2d.Batch.C1;
import static com.badlogic.gdx.graphics.g2d.Batch.C2;
import static com.badlogic.gdx.graphics.g2d.Batch.C3;
import static com.badlogic.gdx.graphics.g2d.Batch.C4;
import static com.badlogic.gdx.graphics.g2d.Batch.U1;
import static com.badlogic.gdx.graphics.g2d.Batch.U2;
import static com.badlogic.gdx.graphics.g2d.Batch.U3;
import static com.badlogic.gdx.graphics.g2d.Batch.U4;
import static com.badlogic.gdx.graphics.g2d.Batch.V1;
import static com.badlogic.gdx.graphics.g2d.Batch.V2;
import static com.badlogic.gdx.graphics.g2d.Batch.V3;
import static com.badlogic.gdx.graphics.g2d.Batch.V4;
import static com.badlogic.gdx.graphics.g2d.Batch.X1;
import static com.badlogic.gdx.graphics.g2d.Batch.X2;
import static com.badlogic.gdx.graphics.g2d.Batch.X3;
import static com.badlogic.gdx.graphics.g2d.Batch.X4;
import static com.badlogic.gdx.graphics.g2d.Batch.Y1;
import static com.badlogic.gdx.graphics.g2d.Batch.Y2;
import static com.badlogic.gdx.graphics.g2d.Batch.Y3;
import static com.badlogic.gdx.graphics.g2d.Batch.Y4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapGroupLayer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class YSortingTiledMapRenderer extends OrthogonalTiledMapRenderer {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final TiledMap map;
	private final int mapWidth;
	private final int mapHeight;
	private final int tileWidth;
	private final int tileHeight;
	/**
	 * Layers which to render using cell-specific y sorting
	 */
	List<TiledMapTileLayer> ySortedLayers = new ArrayList<>();
	List<@NonNull YSortedCell> ySortedCells = new ArrayList<>();
	Map<TiledMapTileLayer, YSortedCell[][]> ySortedGrid = new LinkedHashMap<>();
	List<@NonNull TileObject> tileObjects = new ArrayList<>();

	public YSortingTiledMapRenderer(TiledMap map) {
		super(map);
		this.map = map;
		
		mapWidth = map.getProperties().get("width", Integer.class);
		mapHeight = map.getProperties().get("height", Integer.class);
		tileWidth = map.getProperties().get("tileheight", Integer.class);
		tileHeight = map.getProperties().get("tileheight", Integer.class);

		fetchYSortedLayers();
		initYSortedCells();
		initYSortedGrid();
		calculateAndSortYCells();
		generateAndSetCellParentObjects();
		calculateAndSortTileObjectsByBaseYIndex();
		calculateTileObjectBoundingBoxes();
	}
	
	private void initYSortedGrid() {
		for (var layer : ySortedLayers) {
			var gridLayer = new YSortedCell[mapHeight][];
			for(int i=0;i<mapHeight;i++) {
				gridLayer[i] = new YSortedCell[mapWidth];
			}
			ySortedGrid.put(layer, gridLayer);
		}
		for(YSortedCell cell : ySortedCells) {
			ySortedGrid.get(cell.layer)[cell.yIndex][cell.xIndex] = cell;
		}
	}		

	private void initYSortedCells() {
		ySortedCells.clear();
		// First, get all non-null tree tiles.
		for (var layer : ySortedLayers) {
			for (int x = 0; x < layer.getWidth(); x++) {
				for (int y = 0; y < layer.getHeight(); y++) {
					if (layer.getCell(x, y) != null && layer.getCell(x, y).getTile() != null) {
						YSortedCell cell = new YSortedCell(x, y, layer);
						ySortedCells.add(cell);
					}
				}
			}
		}
	}
	
	private void calculateAndSortTileObjectsByBaseYIndex() {
		for(var tileObjects : tileObjects) {
			tileObjects.calculateBaseYIndex();
		}
		tileObjects.sort((o1, o2) -> o2.baseYIndex - o1.baseYIndex); //  in decreasing order
	}
	private void calculateTileObjectBoundingBoxes() {
		for(var tileObjects : tileObjects) {
			tileObjects.calculateBoundingBox();
		}
	}
	
	
	private void calculateAndSortYCells() {
		for (var cell : ySortedCells) {
			// baseIndexY is the yIndex of the bottom-most consecutive non-empty cell, starting from here
			while (cell.baseYIndex > 0 && cell.layer.getCell(cell.xIndex, cell.baseYIndex - 1) != null) {
				cell.baseYIndex -= 1;
			}
		}
		ySortedCells.sort((c1, c2) -> c2.baseYIndex - c1.baseYIndex);
	}
	private void generateAndSetCellParentObjects() {
		tileObjects.clear();
		for (var gridLayer : ySortedGrid.values()) {
			for(int y = 0; y<mapHeight;y++) {
				for(int x= 0; x<mapWidth;x++) {
					setAllConnectedCellsToSameParent(x, y, gridLayer, null, true);
				}
			}
		}
	}
	/**
	 * Look at adjacent tiles for tells
	 */
	private void setAllConnectedCellsToSameParent(int x, int y, YSortedCell[][]gridLayer, @Nullable TileObject existingParent, boolean warnOnBounds) {
		
		
		if(x < 0 || x>=mapWidth) {
			if(warnOnBounds)
				logger.warn("X {} is outside grid bounds {}x{}", x, mapWidth, mapHeight);
			return;
		}
		if(y < 0 || y>=mapHeight) {
			if(warnOnBounds)
				logger.warn("Y {} is outside grid bounds {}x{}", x, mapWidth, mapHeight);
			return;
		}
		
		var cell = gridLayer[y][x];
		if(cell==null)
			return;
		if(cell.getParent() != null) { // already handled
			return; 
		}
		if(existingParent != null) {
			cell.setParent(existingParent);
		}else {
			var newObject = new TileObject(tileWidth, tileHeight, mapHeight);
			cell.setParent(newObject);
			tileObjects.add(newObject);
		}
		setAllConnectedCellsToSameParent(x+1, y, gridLayer, cell.getParent(), false);
		setAllConnectedCellsToSameParent(x-1, y, gridLayer, cell.getParent(), false);
		setAllConnectedCellsToSameParent(x, y+1, gridLayer, cell.getParent(), false);
		setAllConnectedCellsToSameParent(x, y-1, gridLayer, cell.getParent(), false);
	}

//	public List<YSortedCell> getYSortedCells() {
//		return Collections.unmodifiableList(ySortedCells);
//	}

	public void renderBaseLayers() {
		List<TiledMapTileLayer> layers = getAllTileLayersInMap(map);
		layers.removeIf((layer) -> ySortedLayers.contains(layer));
		beginRender();
		for (TiledMapTileLayer layer : layers) {
			renderMapLayer(layer);
		}
		endRender();
	}

	private void fetchYSortedLayers() {
		for (MapLayer layer : map.getLayers()) {
			if (layer instanceof MapGroupLayer) {
				var group = (MapGroupLayer) layer;
				var name = group.getName().toLowerCase();
				if (name.startsWith("trees") || name.startsWith("raised objects") || name.startsWith("buildings")) {
					ySortedLayers.addAll(getAllTileLayersIn(group));
				}
			}
		}
	}

	private static List<TiledMapTileLayer> getAllTileLayersInMap(TiledMap map) {
		List<TiledMapTileLayer> layers = new ArrayList<>();
		for (MapLayer layer : map.getLayers()) {
			if (layer instanceof MapGroupLayer) {
				var group = (MapGroupLayer) layer;
				layers.addAll(getAllTileLayersIn(group));
			} else if (layer instanceof TiledMapTileLayer) {
				layers.add((TiledMapTileLayer) layer);
			}
		}
		return layers;
	}

	private static List<TiledMapTileLayer> getAllTileLayersIn(MapGroupLayer group) {
		List<TiledMapTileLayer> layers = new ArrayList<>();
		accumulateAllTileLayersIn(group, layers);
		return layers;
	}

	private static void accumulateAllTileLayersIn(MapGroupLayer group, @NonNull List<TiledMapTileLayer> layers) {
		for (var layer : group.getLayers()) {
			if (layer instanceof MapGroupLayer) {
				accumulateAllTileLayersIn((MapGroupLayer) layer, layers);
			} else if (layer instanceof TiledMapTileLayer) {
				layers.add((TiledMapTileLayer) layer);
			}
		}
	}

	
	public void renderTileObject(TileObject tileObject, float opacity) {
		for(var cell : tileObject.cells) {
			renderCell(cell.xIndex, cell.yIndex, opacity, cell.layer);
		}
	}
	
	private void renderCell(int xIndex, int yIndex, float opacity, TiledMapTileLayer layer) {

		final Color batchColor = batch.getColor();
		final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b,
				batchColor.a * layer.getOpacity() * opacity);

		final int layerWidth = layer.getWidth();
		final int layerHeight = layer.getHeight();

		final float layerTileWidth = layer.getTileWidth() * unitScale;
		final float layerTileHeight = layer.getTileHeight() * unitScale;

		final float layerOffsetX = layer.getRenderOffsetX() * unitScale;
		// offset in tiled is y down, so we flip it
		final float layerOffsetY = -layer.getRenderOffsetY() * unitScale;

		final int col1 = Math.max(0, (int) ((viewBounds.x - layerOffsetX) / layerTileWidth));
		@SuppressWarnings("unused")
		final int col2 = Math.min(layerWidth,
				(int) ((viewBounds.x + viewBounds.width + layerTileWidth - layerOffsetX) / layerTileWidth));

		@SuppressWarnings("unused")
		final int row1 = Math.max(0, (int) ((viewBounds.y - layerOffsetY) / layerTileHeight));
		final int row2 = Math.min(layerHeight,
				(int) ((viewBounds.y + viewBounds.height + layerTileHeight - layerOffsetY) / layerTileHeight));

		float y = row2 * layerTileHeight + layerOffsetY;
		@SuppressWarnings("unused")
		float xStart = col1 * layerTileWidth + layerOffsetX;
		final float[] vertices = this.vertices;
		y = yIndex * layerTileHeight;
		// can do: Optimization: only render the row if yIndex is between row1 and row2

		final TiledMapTileLayer.Cell cell = layer.getCell(xIndex, yIndex);

		final TiledMapTile tile = cell.getTile();

		if (tile != null) {
			final boolean flipX = cell.getFlipHorizontally();
			final boolean flipY = cell.getFlipVertically();
			final int rotations = cell.getRotation();

			TextureRegion region = tile.getTextureRegion();

			float x1 = xIndex * layerTileWidth + tile.getOffsetX() * unitScale;
			float y1 = y + tile.getOffsetY() * unitScale;
			float x2 = x1 + region.getRegionWidth() * unitScale;
			float y2 = y1 + region.getRegionHeight() * unitScale;

			float u1 = region.getU();
			float v1 = region.getV2();
			float u2 = region.getU2();
			float v2 = region.getV();

			vertices[X1] = x1;
			vertices[Y1] = y1;
			vertices[C1] = color;
			vertices[U1] = u1;
			vertices[V1] = v1;

			vertices[X2] = x1;
			vertices[Y2] = y2;
			vertices[C2] = color;
			vertices[U2] = u1;
			vertices[V2] = v2;

			vertices[X3] = x2;
			vertices[Y3] = y2;
			vertices[C3] = color;
			vertices[U3] = u2;
			vertices[V3] = v2;

			vertices[X4] = x2;
			vertices[Y4] = y1;
			vertices[C4] = color;
			vertices[U4] = u2;
			vertices[V4] = v1;

			if (flipX) {
				float temp = vertices[U1];
				vertices[U1] = vertices[U3];
				vertices[U3] = temp;
				temp = vertices[U2];
				vertices[U2] = vertices[U4];
				vertices[U4] = temp;
			}
			if (flipY) {
				float temp = vertices[V1];
				vertices[V1] = vertices[V3];
				vertices[V3] = temp;
				temp = vertices[V2];
				vertices[V2] = vertices[V4];
				vertices[V4] = temp;
			}
			if (rotations != 0) {
				switch (rotations) {
				case Cell.ROTATE_90: {
					float tempV = vertices[V1];
					vertices[V1] = vertices[V2];
					vertices[V2] = vertices[V3];
					vertices[V3] = vertices[V4];
					vertices[V4] = tempV;

					float tempU = vertices[U1];
					vertices[U1] = vertices[U2];
					vertices[U2] = vertices[U3];
					vertices[U3] = vertices[U4];
					vertices[U4] = tempU;
					break;
				}
				case Cell.ROTATE_180: {
					float tempU = vertices[U1];
					vertices[U1] = vertices[U3];
					vertices[U3] = tempU;
					tempU = vertices[U2];
					vertices[U2] = vertices[U4];
					vertices[U4] = tempU;
					float tempV = vertices[V1];
					vertices[V1] = vertices[V3];
					vertices[V3] = tempV;
					tempV = vertices[V2];
					vertices[V2] = vertices[V4];
					vertices[V4] = tempV;
					break;
				}
				case Cell.ROTATE_270: {
					float tempV = vertices[V1];
					vertices[V1] = vertices[V4];
					vertices[V4] = vertices[V3];
					vertices[V3] = vertices[V2];
					vertices[V2] = tempV;

					float tempU = vertices[U1];
					vertices[U1] = vertices[U4];
					vertices[U4] = vertices[U3];
					vertices[U3] = vertices[U2];
					vertices[U2] = tempU;
					break;
				}
				}
			}
			batch.begin();
			batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
			batch.end();
		}

	}

	public List<@NonNull TileObject> getTileObjects() {
		return Collections.unmodifiableList(tileObjects);
	}

}
