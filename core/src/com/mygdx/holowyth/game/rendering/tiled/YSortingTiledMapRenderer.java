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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapGroupLayer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class YSortingTiledMapRenderer extends OrthogonalTiledMapRenderer {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final TiledMap map;
	ArrayList<TiledMapTileLayer> treeLayers = new ArrayList<>();
	ArrayList<YSortedCell> ySortedCells = new ArrayList<>();
	
	public YSortingTiledMapRenderer(TiledMap map) {
		super(map);
		this.map = map;

		collectTreeLayers();
		calculateAndSortYTiles();
	}
	
	private void calculateAndSortYTiles() {
		ySortedCells.clear();
		// First, get all non-null tree tiles.
		for(var layer : treeLayers) {
			for(int x=0;x<layer.getWidth();x++) {
				for(int y=0;y<layer.getHeight();y++) {
					if(layer.getCell(x, y) != null && layer.getCell(x, y).getTile() != null) {
						ySortedCells.add(new YSortedCell(x, y, layer));
					}
				}
			}
		}
		
		// Calculate baseYIndex values
		
		for(var cell : ySortedCells) {
			// baseIndexY is the yIndex of the bottom-most consecutive non-empty cell, starting from here
			while(cell.baseYIndex>0 && cell.layer.getCell(cell.xIndex, cell.baseYIndex-1) != null) {
				cell.baseYIndex -=1;
			}
		}
		
		//sort the list in decreasing baseYIndex 
		ySortedCells.sort((c1, c2) -> c2.baseYIndex - c1.baseYIndex);
	}
	public List<YSortedCell> getYSortedTiles() {
		return Collections.unmodifiableList(ySortedCells);
	}

	public void renderBaseLayers() {
		// Delegate to TiledMapRenderer
		ArrayList<Integer> indexes = new ArrayList<>();
		for (MapLayer layer : map.getLayers()) {
			if (!layer.getName().toLowerCase().startsWith("trees")) { // this just excludes via root level groups and layers
				indexes.add(map.getLayers().getIndex(layer));
			}
		}
		// Convert to array
		int[] baseLayerIndexes = new int[indexes.size()];
		for (int i = 0; i < indexes.size(); i++)
			baseLayerIndexes[i] = indexes.get(i);
		render(baseLayerIndexes);
	}
	private void collectTreeLayers() {
		treeLayers.clear();
		for (MapLayer layer : map.getLayers()) {
			collectTreeLayers(layer);
		}
	}

	private void collectTreeLayers(MapLayer layer) {
		if (layer instanceof MapGroupLayer) {
			var group = (MapGroupLayer) layer;
			for (var subLayer : group.getLayers()) {
				collectTreeLayers(subLayer);
			}
		} else if (layer instanceof TiledMapTileLayer) {
			var tileLayer = (TiledMapTileLayer) layer;
			if (tileLayer.getName().toLowerCase().startsWith("trees")) { // this just excludes via root level groups and
																			// layers
				treeLayers.add(tileLayer);
			}
		}
	}
	public List<TiledMapTileLayer> getTreeLayers() {
		return Collections.unmodifiableList(treeLayers);
	}



	/**
	 * Test method
	 */
	public void renderAllTreeTiles() {
		for (int yIndex = 0; yIndex < map.getProperties().get("height", Integer.class); yIndex++) {
			renderTreeTilesWithYIndex(yIndex);
		}
	}

	public void renderTreeTilesWithYIndex(int yIndex) {
		for (var layer : treeLayers) {
			renderAllTreeTilesWithYIndex(layer, yIndex);
		}
	}

	public void renderTreeTile(int xIndex, int yIndex, TiledMapTileLayer layer) {

		final Color batchColor = batch.getColor();
		final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b,
				batchColor.a * layer.getOpacity());

		final int layerWidth = layer.getWidth();
		final int layerHeight = layer.getHeight();

		final float layerTileWidth = layer.getTileWidth() * unitScale;
		final float layerTileHeight = layer.getTileHeight() * unitScale;

		final float layerOffsetX = layer.getRenderOffsetX() * unitScale;
		// offset in tiled is y down, so we flip it
		final float layerOffsetY = -layer.getRenderOffsetY() * unitScale;

		final int col1 = Math.max(0, (int) ((viewBounds.x - layerOffsetX) / layerTileWidth));
		final int col2 = Math.min(layerWidth,
				(int) ((viewBounds.x + viewBounds.width + layerTileWidth - layerOffsetX) / layerTileWidth));

		@SuppressWarnings("unused")
		final int row1 = Math.max(0, (int) ((viewBounds.y - layerOffsetY) / layerTileHeight));
		final int row2 = Math.min(layerHeight,
				(int) ((viewBounds.y + viewBounds.height + layerTileHeight - layerOffsetY) / layerTileHeight));

		float y = row2 * layerTileHeight + layerOffsetY;
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

	// Used for simple interspersed rendering (final won't use this)
	private void renderAllTreeTilesWithYIndex(TiledMapTileLayer layer, int yIndex) {
		// just render everything, even if it's outside of view....

		final Color batchColor = batch.getColor();
		final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b,
				batchColor.a * layer.getOpacity());

		final int layerWidth = layer.getWidth();
		final int layerHeight = layer.getHeight();

		final float layerTileWidth = layer.getTileWidth() * unitScale;
		final float layerTileHeight = layer.getTileHeight() * unitScale;

		final float layerOffsetX = layer.getRenderOffsetX() * unitScale;
		// offset in tiled is y down, so we flip it
		final float layerOffsetY = -layer.getRenderOffsetY() * unitScale;

		final int col1 = Math.max(0, (int) ((viewBounds.x - layerOffsetX) / layerTileWidth));
		final int col2 = Math.min(layerWidth,
				(int) ((viewBounds.x + viewBounds.width + layerTileWidth - layerOffsetX) / layerTileWidth));

		@SuppressWarnings("unused")
		final int row1 = Math.max(0, (int) ((viewBounds.y - layerOffsetY) / layerTileHeight));
		final int row2 = Math.min(layerHeight,
				(int) ((viewBounds.y + viewBounds.height + layerTileHeight - layerOffsetY) / layerTileHeight));

		float y = row2 * layerTileHeight + layerOffsetY;
		float xStart = col1 * layerTileWidth + layerOffsetX;
		final float[] vertices = this.vertices;
		y = yIndex * layerTileHeight;
		// can do: Optimization: only render the row if yIndex is between row1 and row2

		float x = xStart;
		for (int col = col1; col < col2; col++) {
			final TiledMapTileLayer.Cell cell = layer.getCell(col, yIndex);
			if (cell == null) {
				x += layerTileWidth;
				continue;
			}
			final TiledMapTile tile = cell.getTile();

			if (tile != null) {
				final boolean flipX = cell.getFlipHorizontally();
				final boolean flipY = cell.getFlipVertically();
				final int rotations = cell.getRotation();

				TextureRegion region = tile.getTextureRegion();

				float x1 = x + tile.getOffsetX() * unitScale;
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
	}
}
