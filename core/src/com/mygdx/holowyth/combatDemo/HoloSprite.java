package com.mygdx.holowyth.combatDemo;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class HoloSprite {

	TextureRegion tex;
	float x, y;
	float scale;
	float offsetX, offsetY;
	
	public HoloSprite(TextureRegion textureRegion, float x, float y){
		this(textureRegion, x, y, 0);
	}
	public HoloSprite(TextureRegion textureRegion, float x, float y, float targetWidth){
		this.x = x;
		this.y = y;
		this.tex = textureRegion;
		
		if(targetWidth == 0){
			scale = 1;
		}else{
			scale = targetWidth/textureRegion.getRegionWidth();
		}
		
	}
	public HoloSprite(TextureRegion textureRegion, float x, float y, float targetWidth, float offsetX, float offsetY){
		this(textureRegion, x, y, targetWidth);
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}
	
	public void draw(SpriteBatch batch){
		float width = tex.getRegionWidth();
		float height = tex.getRegionHeight();
		
		batch.begin();
		batch.draw(tex, x-width/2.0f + offsetX, y-height/2.0f + offsetY, width/2.0f, height/2.0f, width, height, scale, scale, 0);
		batch.end();
	}
	
}