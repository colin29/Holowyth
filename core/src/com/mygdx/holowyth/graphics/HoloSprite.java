package com.mygdx.holowyth.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class HoloSprite {

	TextureRegion tex;
	public float x, y;
	public float scale;
	public float offsetX, offsetY;

	public float alphaScaling = 1f;;

	public float rotation;

	/**
	 * My implementation of a sprite. Stores positioning info that lets you center and offset stuff.
	 */

	public HoloSprite(TextureRegion textureRegion, float x, float y) {
		this(textureRegion, x, y, 0);
	}

	public HoloSprite(TextureRegion textureRegion, float x, float y, float targetWidth) {
		this.x = x;
		this.y = y;
		this.tex = textureRegion;

		if (targetWidth == 0) {
			scale = 1;
		} else {
			scale = targetWidth / textureRegion.getRegionWidth();
		}

	}

	public HoloSprite(TextureRegion textureRegion, float x, float y, float targetWidth, float offsetX, float offsetY) {
		this(textureRegion, x, y, targetWidth);
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	public void draw(SpriteBatch batch) {
		float width = tex.getRegionWidth();
		float height = tex.getRegionHeight();

		batch.setColor(1, 1, 1, alphaScaling);

		batch.begin();
		batch.enableBlending();

		batch.draw(tex, x - width / 2.0f + offsetX, y - height / 2.0f + offsetY, width / 2.0f, height / 2.0f, width, height, scale, scale, rotation);
		batch.end();

		batch.setColor(Color.WHITE);
	}

	public TextureRegion getTexture() {
		return tex;
	}

}