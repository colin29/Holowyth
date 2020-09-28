package com.mygdx.holowyth.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.map.trigger.region.RectRegion;
import com.mygdx.holowyth.map.trigger.region.Region;

import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Renders the features of a maps (like locations, regions, etc, unit markers, etc)
 * @author Colin
 */
public class GameMapRenderer {

	
	public static Color regionDisplayColor = HoloGL.rgb(219, 224, 72, 0.4f);
	public static Color locDisplayColor = HoloGL.rgb(219, 224, 72, 1f);
	
	/**
	 * Batch should be set to using the world camera.
	 */
	public static void renderMapRegions(BitmapFont font,GameMap map, ShapeDrawer shapeDrawer, SpriteBatch batch){
		batch.begin();
		for(Region region : map.getRegions()) {
			if(region instanceof RectRegion) {
				RectRegion r = (RectRegion) region;
				
				shapeDrawer.filledRectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight(), regionDisplayColor);
				
				font.draw(batch, r.getName(), r.getX(), r.getY() + r.getHeight());
			}
		}
		
		
		batch.end();
	}
	
	public static void renderEntrances(BitmapFont font,GameMap map, ShapeDrawer shapeDrawer, SpriteBatch batch){
		batch.begin();
		for(Location loc : map.getEntrances()) {
			shapeDrawer.setColor(locDisplayColor);
			shapeDrawer.circle(loc.pos.x, loc.pos.y, 15);
				
			font.draw(batch, loc.name, loc.pos.x, loc.pos.y+font.getCapHeight());
		}
		batch.end();
	}
	
	public static void renderLocations(BitmapFont font,GameMap map, ShapeDrawer shapeDrawer, SpriteBatch batch){
		batch.begin();
		for(Location loc : map.getLocations().values()) {
				
			shapeDrawer.setColor(locDisplayColor);
			shapeDrawer.circle(loc.pos.x, loc.pos.y, 15);
				
			font.draw(batch, loc.name, loc.pos.x, loc.pos.y+font.getCapHeight());
		}
		batch.end();
	}
}
