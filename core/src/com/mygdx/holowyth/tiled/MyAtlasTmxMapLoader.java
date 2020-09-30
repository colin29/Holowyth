package com.mygdx.holowyth.tiled;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.ImageResolver;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.AtlasTmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.XmlReader.Element;

public class MyAtlasTmxMapLoader extends AtlasTmxMapLoader {

	/**
	 * Same as AtlasTmxMapLoader, but fixed to get the firstid attribute from the tileset element in the TMX file, not tsx file.
	 */
	@Override
	protected void loadTileSet(Element mapElement, FileHandle tmxFile, ImageResolver imageResolver) {
		if (mapElement.getName().equals("tileset")) {
			String imageSource = "";
			int imageWidth = 0;
			int imageHeight = 0;
			FileHandle image = null;

			Element element = null;
			String source = mapElement.getAttribute("source", null);
			if (source != null) {
				FileHandle tsx = getRelativeFileHandle(tmxFile, source);
				try {
					element = xml.parse(tsx);
					Element imageElement = element.getChildByName("image");
					if (imageElement != null) {
						imageSource = imageElement.getAttribute("source");
						imageWidth = imageElement.getIntAttribute("width", 0);
						imageHeight = imageElement.getIntAttribute("height", 0);
						image = getRelativeFileHandle(tsx, imageSource);
					}
				} catch (SerializationException e) {
					throw new GdxRuntimeException("Error parsing external tileset.");
				}
			} else {
				Element imageElement = mapElement.getChildByName("image");
				if (imageElement != null) {
					imageSource = imageElement.getAttribute("source");
					imageWidth = imageElement.getIntAttribute("width", 0);
					imageHeight = imageElement.getIntAttribute("height", 0);
					image = getRelativeFileHandle(tmxFile, imageSource);
				}
			}

			@SuppressWarnings("null")
			String name = element.get("name", null);
			// Get the firstid attribute from the tileset element in the TMX file, not tsx file.
			int firstgid = mapElement.getIntAttribute("firstgid", 1); 
			int tilewidth = element.getIntAttribute("tilewidth", 0);
			int tileheight = element.getIntAttribute("tileheight", 0);
			int spacing = element.getIntAttribute("spacing", 0);
			int margin = element.getIntAttribute("margin", 0);

			Element offset = element.getChildByName("tileoffset");
			int offsetX = 0;
			int offsetY = 0;
			if (offset != null) {
				offsetX = offset.getIntAttribute("x", 0);
				offsetY = offset.getIntAttribute("y", 0);
			}

			TiledMapTileSet tileSet = new TiledMapTileSet();

			// TileSet
			tileSet.setName(name);
			final MapProperties tileSetProperties = tileSet.getProperties();
			Element properties = element.getChildByName("properties");
			if (properties != null) {
				loadProperties(tileSetProperties, properties);
			}
			tileSetProperties.put("firstgid", firstgid);

			// Tiles
			Array<Element> tileElements = element.getChildrenByName("tile");

			addStaticTiles(tmxFile, imageResolver, tileSet, element, tileElements, name, firstgid, tilewidth,
					tileheight, spacing, margin, source, offsetX, offsetY, imageSource, imageWidth, imageHeight, image);

			for (Element tileElement : tileElements) {
				int localtid = tileElement.getIntAttribute("id", 0);
				TiledMapTile tile = tileSet.getTile(firstgid + localtid);
				if (tile != null) {
					addTileProperties(tile, tileElement);
					addTileObjectGroup(tile, tileElement);
					addAnimatedTile(tileSet, tile, tileElement, firstgid);
				}
			}

			map.getTileSets().addTileSet(tileSet);
		}
	}

}
